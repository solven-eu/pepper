/**
 * The MIT License
 * Copyright (c) 2014-2024 Benoit Lacelle - SOLVEN
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;

/**
 * This class helps reference-sharing
 *
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings("PMD.GodClass")
public class PepperMemoryHelper implements IPepperMemoryConstants {
	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperMemoryHelper.class);

	// http://java-performance.info/overview-of-memory-saving-techniques-java/
	public static final int JVM_MEMORY_CHUNK = 8;
	public static final int JVM_BYTES_PER_CHAR = 2;
	public static final int JVM_STRING_HEADER = 45;

	private static final long MASK = 0xFFFFFFFFL;
	private static final int SHIFT = 32;

	protected PepperMemoryHelper() {
		// hidden
	}

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
		// Move the higher bit as lower bit: if packed >= 0, we then are sure to have a
		// 0 as first bit
		return (int) (Long.rotateLeft(packed, 1) >>> SHIFT);
	}

	public static final int positiveUnpack2(long packed) {
		// Move the higher bit as lower bit: if packed >= 0, we then are sure to have a
		// 0 as first bit
		// Then, this 0 bit it put back as last bit: the integer is guaranteed to be
		// positive
		return Integer.rotateRight((int) (Long.rotateLeft(packed, 1) & MASK), 1);
	}

	@SuppressWarnings("checkstyle:magicnumber")
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

			if (lastChar == 'b' || lastChar == 'B' || lastChar == 'o') {
				if (targetMax.length() <= 1) {
					return 0;
				}
				lastChar = targetMax.charAt(targetMax.length() - 2);
				lastDigit -= 1;
			}

			if (lastChar == 'k' || lastChar == 'K') {
				multiplier = KB;
			} else if (lastChar == 'm' || lastChar == 'M') {
				multiplier = MB;
			} else if (lastChar == 'g' || lastChar == 'G') {
				multiplier = GB;
			} else {
				throw new IllegalArgumentException(
						"Can not parse " + targetMax + ". It should end by a digit or one of 'k', 'm','g'");
			}

			digits = targetMax.substring(0, lastDigit).trim();
		}

		// Something like "123,456" or "123 456"
		if (digits.length() >= 5 && !CharMatcher.digit().matches(digits.charAt(digits.length() - 4))) {
			// Transform "123,456" to "123" ~ "456"
			digits = digits.substring(0, digits.length() - 4) + digits.substring(digits.length() - 3);
		}

		double parsedAsDouble = Double.parseDouble(digits);
		return (long) (parsedAsDouble * multiplier);
	}

	public static String memoryAsString(long initialBytes) {
		long leftBytes = initialBytes;
		String string = "";

		int unitsDone = 0;
		if (unitsDone < 2) {
			long gb = leftBytes / GB;
			if (gb > 0) {
				unitsDone++;
				string += gb + "G";
				leftBytes -= gb * GB;
			}
		}

		if (unitsDone < 2) {
			long mb = leftBytes / MB;
			if (mb > 0) {
				unitsDone++;
				string += mb + "M";
				leftBytes -= mb * MB;
			}
		}
		if (unitsDone < 2) {
			long kb = leftBytes / KB;
			if (kb > 0) {
				unitsDone++;
				string += kb + "K";
				leftBytes -= kb * KB;
			}
		}

		if (unitsDone < 2) {
			if (leftBytes > 0) {
				string += leftBytes + "B";
			} else {
				LOGGER.trace("No more bytes");
			}
		}

		return string;
	}
}
