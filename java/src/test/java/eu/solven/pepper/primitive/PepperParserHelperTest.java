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
package eu.solven.pepper.primitive;

import java.util.Random;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

// https://github.com/javolution/javolution/blob/master/src/test/java/org/javolution/text/TypeFormatTest.java
public class PepperParserHelperTest {

	@Test
	public void parseDouble_One() {
		Assertions.assertEquals(1.0, PepperParserHelper.parseDouble("1.0"), 0);
		Assertions.assertEquals(1.0, PepperParserHelper.parseDouble("1"), 0);
	}

	@Test
	public void parseDouble_Zero() {
		Assertions.assertEquals(0.0, PepperParserHelper.parseDouble("0.0"), 0);
		Assertions.assertEquals(0.0, PepperParserHelper.parseDouble("0"), 0);
	}

	@Test
	public void parseDouble_NaN() {
		Assertions.assertTrue(Double.isNaN(PepperParserHelper.parseDouble(Double.toString(Double.NaN))));
	}

	@Test
	public void parseDouble_Infinity() {
		Assertions.assertTrue(
				Double.isInfinite(PepperParserHelper.parseDouble(Double.toString(Double.POSITIVE_INFINITY))));
		Assertions.assertTrue(
				Double.isInfinite(PepperParserHelper.parseDouble(Double.toString(Double.NEGATIVE_INFINITY))));
	}

	@Test
	public void parseDouble_random() {
		Random r = new Random(0);

		IntStream.range(0, 10_000).forEach(i -> {
			double d = r.nextInt(1000) * r.nextDouble();

			Assertions.assertEquals(d, PepperParserHelper.parseDouble(Double.toString(d)), 0.000_000_01D);
		});
	}

	@Test
	public void parseDoubleThrowsExceptionIfStringDoesNotRepresentADouble() {
		Assertions.assertThrows(NumberFormatException.class, () -> {
			PepperParserHelper.parseDouble("aaaa");
		});
	}

	@Test
	public void parseDoubleWithExplicitPositiveNumber() {
		Assertions.assertEquals(15.0, PepperParserHelper.parseDouble("+15.0"), 0, "+15 Parsed");
	}

	@Test
	public void parseDoubleWithNegativeNumber() {
		Assertions.assertEquals(-15.0, PepperParserHelper.parseDouble("-15.0"), 0, "-15 Parsed");
	}

	@Test
	public void parseDouble_MultipleZeroPrefix() {
		Assertions.assertEquals(15.0, PepperParserHelper.parseDouble("00015.0"), 0);
		Assertions.assertEquals(15.0, PepperParserHelper.parseDouble("+00015.0"), 0);
		Assertions.assertEquals(15.0, PepperParserHelper.parseDouble("00015"), 0);
		Assertions.assertEquals(15.0, PepperParserHelper.parseDouble("+00015"), 0);
	}

	// * TypeFormat.format(0.2, a) = "0.2" // 17 or 16 digits (as long as lossless conversion), remove trailing zeros.
	// * TypeFormat.format(0.2, 17, false, false, a) = "0.20000000000000001" // Closest 17 digits number.
	// * TypeFormat.format(0.2, 19, false, false, a) = "0.2000000000000000111" // Closest 19 digits.
	// * TypeFormat.format(0.2, 4, false, false, a) = "0.2" // Fixed-point notation, remove trailing zeros.
	// * TypeFormat.format(0.2, 4, false, true, a) = "0.2000" // Fixed-point notation, fixed number of digits.
	// * TypeFormat.format(0.2, 4, true, false, a) = "2.0E-1" // Scientific notation, remove trailing zeros.
	// * TypeFormat.format(0.2, 4, true, true, a) = "2.000E-1" // Scientific notation, fixed number of digits.
	@Test
	public void parseDouble_JavolutionTypeFormatExamples() {
		Assertions.assertEquals(0.2D, PepperParserHelper.parseDouble("0.20000000000000001"), 0);
		Assertions.assertEquals(0.2D, PepperParserHelper.parseDouble("0.2000000000000000111"), 0);
		Assertions.assertEquals(0.2D, PepperParserHelper.parseDouble("0.2"), 0);
		Assertions.assertEquals(0.2D, PepperParserHelper.parseDouble("0.2000"), 0);
		Assertions.assertEquals(0.2D, PepperParserHelper.parseDouble("2.0E-1"), 0);
		Assertions.assertEquals(0.2D, PepperParserHelper.parseDouble("2.000E-1"), 0);
	}

	@Test
	public void parseFloat() {
		Assertions.assertEquals(1.0f, PepperParserHelper.parseFloat("1.0"), 0, "1.0 Parsed");
	}

	@Test
	public void parseFloatThrowsExceptionIfStringDoesNotRepresentAFloat() {
		Assertions.assertThrows(NumberFormatException.class, () -> {
			PepperParserHelper.parseFloat("aaaa");
		});
	}

	@Test
	public void parseFloatWithExplicitPositiveNumber() {
		Assertions.assertEquals(15.0f, PepperParserHelper.parseFloat("+15.0"), 0, "+15 Parsed");
	}

	@Test
	public void parseFloatWithNegativeNumber() {
		Assertions.assertEquals(-15.0f, PepperParserHelper.parseFloat("-15.0"), 0, "-15 Parsed");
	}

}
