/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.memory;

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

import com.google.common.base.Optional;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import eu.solven.pepper.agent.InstrumentationAgent;

/**
 * This class analyses heap-consumption based on {@link Instrumentation}
 *
 * @author Benoit Lacelle
 * @deprecated We prefer relying on JOL
 */
@Deprecated
@SuppressWarnings("PMD.GodClass")
public class PepperFootprintDeprecatedHelper implements IPepperMemoryConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperFootprintDeprecatedHelper.class);

	private static final ReflectionSizeOf REFLECTION_SIZE_OF = new ReflectionSizeOf();
	private static final Double AUTOBOXED_ZERO = 0D;

	// http://java-performance.info/overview-of-memory-saving-techniques-java/
	public static final int JVM_MEMORY_CHUNK = 8;
	public static final int JVM_BYTES_PER_CHAR = 2;
	public static final int JVM_STRING_HEADER = 45;

	private static final int KB = 1024;

	protected PepperFootprintDeprecatedHelper() {
		// hidden
	}

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

	// "We now rely on JOL"
	@Deprecated
	public static long deepSize(Object object) {
		// http://stackoverflow.com/questions/1063068/how-does-the-jvm-ensure-that-system-identityhashcode-will-never-change
		return deepSizeWithBloomFilter(object, Integer.MAX_VALUE / KB);
	}

	// "We now rely on JOL"
	@Deprecated
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
	// "We now rely on JOL"
	@Deprecated
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

				// We prefer to return MAX-VALUE to prevent the caller believing this object has
				// no memory foot-print
				return Long.MAX_VALUE;
			}
		}
	}

	@Deprecated
	public static long recursiveSizeWithBloomFilter(Object object, long expectedObjectCardinality) {
		return deepSizeWithBloomFilter(object, expectedObjectCardinality);
	}

	// "We now rely on JOL"
	@Deprecated
	public static long deepSizeWithBloomFilter(Object object, long expectedObjectCardinality) {
		BloomFilter<Integer> identities = BloomFilter.create(Funnels.integerFunnel(), expectedObjectCardinality);

		return deepSize(object, identities::put);
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
			LOGGER.trace("null");
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
							.forEach(element -> deepSize(instrumentation, identities, totalSize, element));
				} else {
					ReflectionUtils.doWithFields(object.getClass(),
							field -> deepSize(instrumentation, identities, totalSize, field.get(object)),
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

	// "We now rely on JOL"
	@Deprecated
	public static long getDoubleMemory() {
		return REFLECTION_SIZE_OF.deepSizeOf(AUTOBOXED_ZERO);
	}

	// "We now rely on JOL"
	@Deprecated
	public static long getObjectArrayMemory(Object... asArray) {
		if (asArray == null) {
			return 0L;
		}
		long footprint = OBJECT;

		// The weight of the array
		footprint += OBJECT * asArray.length;

		return footprint;
	}

	// "We now rely on JOL"
	@Deprecated
	public static long getObjectArrayMemory(Object asArray) {
		if (asArray == null) {
			return 0L;
		}

		return REFLECTION_SIZE_OF.deepSizeOf(asArray);
	}
}
