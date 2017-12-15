/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package cormoran.pepper.memory;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntPredicate;

import org.ehcache.sizeof.impl.ReflectionSizeOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import cormoran.pepper.agent.InstrumentationAgent;

/**
 * This class helps reference-sharing
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperMemoryHelper implements IPepperMemoryConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperMemoryHelper.class);

	protected PepperMemoryHelper() {
		// hidden
	}

	// http://java-performance.info/overview-of-memory-saving-techniques-java/
	public static final int JVM_MEMORY_CHUNK = 8;
	public static final int JVM_BYTES_PER_CHAR = 2;
	public static final int JVM_STRING_HEADER = 45;

	public static long getStringMemory(CharSequence existingRef) {
		// http://www.javamex.com/tutorials/memory/string_memory_usage.shtml
		// Object Header
		int nbChars = existingRef.length();

		// String are essentially char[], 8 for char
		long nbBits = (long) nbChars * JVM_BYTES_PER_CHAR + JVM_STRING_HEADER;
		return JVM_MEMORY_CHUNK * (nbBits / JVM_MEMORY_CHUNK);
	}

	/**
	 * @deprecated renamed to deepSize
	 */
	@Deprecated
	public static long recursiveSize(Object object) {
		return deepSize(object);
	}

	/**
	 * 
	 * @param object
	 *            the object to analyze
	 * @return the number of bytes consumed by given objects, taking in account the references objects
	 */
	public static long deepSize(Object object) {
		// http://stackoverflow.com/questions/1063068/how-does-the-jvm-ensure-that-system-identityhashcode-will-never-change
		return deepSizeWithBloomFilter(object, Integer.MAX_VALUE / 1024);
	}

	public static long recursiveSize(Object object, IntPredicate identityPredicate) {
		return deepSize(object, identityPredicate);
	}

	/**
	 * 
	 * @param object
	 * @param identityPredicate
	 *            a predicate returning true if it is the first encounter of given object. It may return false even if
	 *            an object has not been considered before, woult it be because the identity policy is not guaranteed
	 *            (e.g. we rely on a BloomFilter) or if we want to exclude some objects
	 * @return Long.MAX_VALUE if the Instrumentation agent is not available. Else an estimation of the memory
	 *         consumption.
	 */
	public static long deepSize(Object object, IntPredicate identityPredicate) {
		if (object == null) {
			return 0L;
		} else {
			Optional<Instrumentation> instrumentation = InstrumentationAgent.getInstrumentation();

			if (instrumentation.isPresent()) {
				LongAdder totalSize = new LongAdder();

				deepSize(instrumentation.get(), identityPredicate, totalSize, object);

				return totalSize.sum();
			} else {
				LOGGER.debug("Instrumentation is not available for {}", object);

				// We prefer to return MAX-VALUE to prevent the caller believing this object has no memory foot-print
				return Long.MAX_VALUE;
			}
		}
	}

	@Deprecated
	public static long recursiveSizeWithBloomFilter(Object object, long expectedObjectCardinality) {
		return deepSizeWithBloomFilter(object, expectedObjectCardinality);
	}

	public static long deepSizeWithBloomFilter(Object object, long expectedObjectCardinality) {
		BloomFilter<Integer> identities = BloomFilter.create(Funnels.integerFunnel(), expectedObjectCardinality);

		return recursiveSize(object, identities::put);
	}

	@Deprecated
	public static void recursiveSize(Instrumentation instrumentation,
			IntPredicate identities,
			LongAdder totalSize,
			Object object) {
		deepSize(instrumentation, identities, totalSize, object);
	}

	/**
	 * 
	 * @param instrumentation
	 *            an {@link Instrumentation} able to provide the memory weight of given object
	 * @param identities
	 *            an identityHashSet where to collect already handlded objects
	 * @param totalSize
	 *            the LongAdder where to accumulate the memory
	 * @param object
	 *            the object to analyse
	 */
	// see https://github.com/jbellis/jamm
	//
	public static void deepSize(Instrumentation instrumentation,
			IntPredicate identities,
			LongAdder totalSize,
			Object object) {
		Objects.requireNonNull(instrumentation);

		if (object == null) {
			return;
		} else {
			// http://stackoverflow.com/questions/4930781/how-do-hashcode-and-identityhashcode-work-at-the-back-end
			// see FastHashCode in
			// http://hg.openjdk.java.net/jdk6/jdk6/hotspot/file/tip/src/share/vm/runtime/synchronizer.cpp
			// -> Random value unrelated to memory address
			if (identities.test(System.identityHashCode(object))) {
				long currentSize = instrumentation.getObjectSize(object);
				totalSize.add(currentSize);

				if (object instanceof Object[]) {
					// For arrays, it would not work to iterate on its fields
					Arrays.stream((Object[]) object)
							.forEach(element -> recursiveSize(instrumentation, identities, totalSize, element));
				} else {
					ReflectionUtils.doWithFields(object.getClass(),
							field -> recursiveSize(instrumentation, identities, totalSize, field.get(object)),
							field -> {
								if (Modifier.isStatic(field.getModifiers())) {
									// Do not add static fields in memory graph
									return false;
								} else if (field.getType().isPrimitive()) {
									// Primitive fields has already been captured by Instrumentation.getObjectSize
									return false;
								} else {
									// Ensure the field is accessible as we are going to read it
									ReflectionUtils.makeAccessible(field);

									return true;
								}
							});
				}
			}
		}
	}

	private static final ReflectionSizeOf reflectionSizeOf = new ReflectionSizeOf();
	private static final Double autoboxedZero = new Double(0);

	@Deprecated
	public static long getDoubleMemory() {
		return reflectionSizeOf.deepSizeOf(autoboxedZero);
	}

	public static long getObjectArrayMemory(Object[] asArray) {
		if (asArray == null) {
			return 0L;
		}
		long footprint = OBJECT;

		// The weight of the array
		footprint += OBJECT * asArray.length;

		return footprint;
	}

	public static long getObjectArrayMemory(Object asArray) {
		if (asArray == null) {
			return 0L;
		}

		return reflectionSizeOf.deepSizeOf(asArray);
	}

	private static final long MASK = 0xFFFFFFFFL;
	private static final int SHIFT = 32;

	/**
	 * It might be useful to have an long<->(int,int) packing guaranteeing both integers to be positive if the long is
	 * positive
	 */
	public static final long positivePack(int i1, int i2) {
		long packed1 = (long) i1 << SHIFT;
		long packed2 = Integer.rotateLeft(i2, 1) & MASK;
		return Long.rotateRight(packed1 | packed2, 1);
	}

	public static final int positiveUnpack1(long packed) {
		// Move the higher bit as lower bit: if packed >= 0, we then are sure to have a 0 as first bit
		return (int) (Long.rotateLeft(packed, 1) >>> SHIFT);
	}

	public static final int positiveUnpack2(long packed) {
		// Move the higher bit as lower bit: if packed >= 0, we then are sure to have a 0 as first bit
		// Then, this 0 bit it put back as last bit: the integer is guaranteed to be positive
		return Integer.rotateRight((int) (Long.rotateLeft(packed, 1) & MASK), 1);
	}

	public static long memoryAsLong(String targetMax) {
		// https://stackoverflow.com/questions/1098488/jvm-heap-parameters

		if (targetMax.isEmpty()) {
			throw new UnsupportedOperationException("Can not be empty");
		}

		String digits;
		long multiplier;

		int lastDigit = targetMax.length();
		char lastChar = targetMax.charAt(lastDigit - 1);
		if (CharMatcher.javaDigit().matches(lastChar)) {
			multiplier = 1L;
			digits = targetMax;
		} else {
			lastDigit -= 1;

			if (lastChar == 'b' || lastChar == 'B') {
				if (targetMax.length() <= 1) {
					return 0;
				}
				lastChar = targetMax.charAt(targetMax.length() - 2);
				lastDigit -= 1;
			}

			if (lastChar == 'k' || lastChar == 'K') {
				multiplier = IPepperMemoryConstants.KB;
			} else if (lastChar == 'm' || lastChar == 'M') {
				multiplier = IPepperMemoryConstants.MB;
			} else if (lastChar == 'g' || lastChar == 'G') {
				multiplier = IPepperMemoryConstants.GB;
			} else {
				throw new IllegalArgumentException(
						"Can not parse " + targetMax + ". It should end by a digit or one of 'k', 'm','g'");
			}

			digits = targetMax.substring(0, lastDigit).trim();
		}

		// Something like "123,456" or ""123 456""
		if (digits.length() >= 5 && !CharMatcher.digit().matches(digits.charAt(digits.length() - 4))) {
			// Transform "123,456" to "123" ~ "456"
			digits = digits.substring(0, digits.length() - 4) + digits.substring(digits.length() - 3);
		}

		double parsedAsDouble = Double.parseDouble(digits);
		return (long) (parsedAsDouble * multiplier);
	}

	public static String memoryAsString(long bytes) {
		String string = "";

		int unitsDone = 0;
		if (unitsDone < 2) {
			long gb = bytes / IPepperMemoryConstants.GB;
			if (gb > 0) {
				unitsDone++;
				string += gb + "G";
				bytes -= gb * IPepperMemoryConstants.GB;
			}
		}

		if (unitsDone < 2) {
			long mb = bytes / IPepperMemoryConstants.MB;
			if (mb > 0) {
				unitsDone++;
				string += mb + "M";
				bytes -= mb * IPepperMemoryConstants.MB;
			}
		}
		if (unitsDone < 2) {
			long kb = bytes / IPepperMemoryConstants.KB;
			if (kb > 0) {
				unitsDone++;
				string += kb + "K";
				bytes -= kb * IPepperMemoryConstants.KB;
			}
		}

		if (unitsDone < 2) {
			if (bytes > 0) {
				string += bytes + "B";
			} else {
				LOGGER.trace("No more bytes");
			}
		}

		return string;
	}
}
