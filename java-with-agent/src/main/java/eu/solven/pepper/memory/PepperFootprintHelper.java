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
package eu.solven.pepper.memory;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.IntPredicate;

import org.openjdk.jol.info.GraphLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class helps reference-sharing. JOL may request additional JVM properties like -Djol.magicFieldOffset=true or
 * -Djdk.attach.allowAttachSelf
 *
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings("PMD.GodClass")
public class PepperFootprintHelper implements IPepperMemoryConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperFootprintHelper.class);

	protected PepperFootprintHelper() {
		// hidden
	}

	@Deprecated
	public static long getStringMemory(CharSequence existingRef) {
		return PepperFootprintDeprecatedHelper.getStringMemory(existingRef);
	}

	/**
	 *
	 * @param object
	 *            the object to analyze
	 * @return the number of bytes consumed by given objects, taking in account the references objects
	 */
	public static long deepSize(Object object) {
		try {
			return GraphLayout.parseInstance(object).totalSize();
		} catch (RuntimeException e) {
			LOGGER.warn("Issue with JOL over {}", object, e);
			return -1;
		}
	}

	// "We now rely on JOL"
	@Deprecated
	public static long deepSize(Object object, IntPredicate identityPredicate) {
		return PepperFootprintDeprecatedHelper.deepSize(object, identityPredicate);
	}

	// "We now rely on JOL"
	@Deprecated
	public static long deepSizeWithBloomFilter(Object object, long expectedObjectCardinality) {
		return PepperFootprintDeprecatedHelper.deepSizeWithBloomFilter(object, expectedObjectCardinality);
	}

	// "We now rely on JOL"
	@Deprecated
	public static void deepSize(Instrumentation instrumentation,
			IntPredicate identities,
			LongAdder totalSize,
			Object object) {
		PepperFootprintDeprecatedHelper.deepSize(instrumentation, identities, totalSize, object);
	}

	// "We now rely on JOL"
	@Deprecated
	public static long getDoubleMemory() {
		return PepperFootprintDeprecatedHelper.getDoubleMemory();
	}

	// "We now rely on JOL"
	@Deprecated
	public static long getObjectArrayMemory(Object... asArray) {
		return PepperFootprintDeprecatedHelper.getObjectArrayMemory(asArray);
	}

	// "We now rely on JOL"
	@Deprecated
	public static long getObjectArrayMemory(Object asArray) {
		return PepperFootprintDeprecatedHelper.getObjectArrayMemory(asArray);
	}
}
