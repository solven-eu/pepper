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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPepperMemoryHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TestPepperMemoryHelper.class);

	@Test
	public void testCtor() {
		Assertions.assertNotNull(new PepperMemoryHelper());
	}

	@Test
	public void testBitPacking() {
		// We use custom bit packing in order to produce positive integers if the input
		// long is positive
		// Consider positive longs, as they typically comes from dates
		for (long i = 0; i < Integer.MAX_VALUE * 1024L; i += Integer.MAX_VALUE / 10) {
			// We want to ensure the integers are positive: false as unpack2 will cover the
			// whole integer range:
			// MIN_VALUE -> MAX_VALUE
			LOGGER.trace("Testing bit-packing for {}", i);
			Assertions.assertEquals(i,
					PepperMemoryHelper.positivePack(PepperMemoryHelper.positiveUnpack1(i),
							PepperMemoryHelper.positiveUnpack2(i)));
		}
	}

	@Test
	public void testParseMemory() {
		Assertions.assertEquals(123, PepperMemoryHelper.memoryAsLong("123"));
		Assertions.assertEquals(123 * IPepperMemoryConstants.KB, PepperMemoryHelper.memoryAsLong("123k"));
		Assertions.assertEquals(123 * IPepperMemoryConstants.MB, PepperMemoryHelper.memoryAsLong("123M"));
		Assertions.assertEquals(123 * IPepperMemoryConstants.GB, PepperMemoryHelper.memoryAsLong("123g"));
	}

	@Test
	public void testParseMemory_EndsKB() {
		Assertions.assertEquals(123 * IPepperMemoryConstants.KB, PepperMemoryHelper.memoryAsLong("123kB"));
	}

	// We observe this in Windows10/FR
	@Test
	public void testParseMemory_EndsKo_WeirdEncoding() {
		Assertions.assertEquals(68_204 * IPepperMemoryConstants.KB, PepperMemoryHelper.memoryAsLong("68�204 Ko"));
	}

	@Test
	public void testParseMemory_Edge_B() {
		Assertions.assertEquals(0, PepperMemoryHelper.memoryAsLong("B"));
	}

	// Happens on vmmap|pmap. See ApexProcessHelper.getProcessResidentMemory(long)
	@Test
	public void testParseMemory_withDot() {
		Assertions.assertEquals((long) (1.2 * IPepperMemoryConstants.GB), PepperMemoryHelper.memoryAsLong("1.2G"));
	}

	// happens on tasklist.exe in Windows
	@Test
	public void testParseMemory_windows() {
		Assertions.assertEquals(107_940 * IPepperMemoryConstants.KB, PepperMemoryHelper.memoryAsLong("107,940 K"));
	}

	@Test
	public void testParseMemoryFailsOnUnknownEndChars() {
		Assertions.assertThrows(IllegalArgumentException.class, () -> PepperMemoryHelper.memoryAsLong("123A"));
	}

	@Test
	public void testParseMemoryFailsOnNotDigitsFirst() {
		Assertions.assertThrows(NumberFormatException.class, () -> PepperMemoryHelper.memoryAsLong("12a3m"));
	}

	@Test
	public void testMemoryToString() {
		Assertions.assertEquals("123B", PepperMemoryHelper.memoryAsString(123));
		Assertions.assertEquals("1K206B", PepperMemoryHelper.memoryAsString(1230));
		Assertions.assertEquals("1M177K", PepperMemoryHelper.memoryAsString(1_230_000));
		Assertions.assertEquals("1G149M", PepperMemoryHelper.memoryAsString(1_230_000_000));
	}
}
