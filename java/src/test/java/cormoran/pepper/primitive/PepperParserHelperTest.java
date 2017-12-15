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
package cormoran.pepper.primitive;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

// https://github.com/javolution/javolution/blob/master/src/test/java/org/javolution/text/TypeFormatTest.java
public class PepperParserHelperTest {

	@Test
	public void parseDouble_One() {
		Assert.assertEquals(1.0, PepperParserHelper.parseDouble("1.0"), 0);
		Assert.assertEquals(1.0, PepperParserHelper.parseDouble("1"), 0);
	}

	@Test
	public void parseDouble_Zero() {
		Assert.assertEquals(0.0, PepperParserHelper.parseDouble("0.0"), 0);
		Assert.assertEquals(0.0, PepperParserHelper.parseDouble("0"), 0);
	}

	@Test
	public void parseDouble_NaN() {
		Assert.assertTrue(Double.isNaN(PepperParserHelper.parseDouble(Double.toString(Double.NaN))));
	}

	@Test
	public void parseDouble_Infinity() {
		Assert.assertTrue(Double.isInfinite(PepperParserHelper.parseDouble(Double.toString(Double.POSITIVE_INFINITY))));
		Assert.assertTrue(Double.isInfinite(PepperParserHelper.parseDouble(Double.toString(Double.NEGATIVE_INFINITY))));
	}

	@Test
	public void parseDouble_random() {
		Random r = new Random(0);

		IntStream.range(0, 10000).forEach(i -> {
			double d = r.nextInt(1000) * r.nextDouble();

			Assert.assertEquals(d, PepperParserHelper.parseDouble(Double.toString(d)), 0.00000001D);
		});
	}

	@Test(expected = NumberFormatException.class)
	public void parseDoubleThrowsExceptionIfStringDoesNotRepresentADouble() {
		PepperParserHelper.parseDouble("aaaa");
	}

	@Test
	public void parseDoubleWithExplicitPositiveNumber() {
		Assert.assertEquals("+15 Parsed", 15.0, PepperParserHelper.parseDouble("+15.0"), 0);
	}

	@Test
	public void parseDoubleWithNegativeNumber() {
		Assert.assertEquals("-15 Parsed", -15.0, PepperParserHelper.parseDouble("-15.0"), 0);
	}

	@Test
	public void parseDouble_MultipleZeroPrefix() {
		Assert.assertEquals(15.0, PepperParserHelper.parseDouble("00015.0"), 0);
		Assert.assertEquals(15.0, PepperParserHelper.parseDouble("+00015.0"), 0);
		Assert.assertEquals(15.0, PepperParserHelper.parseDouble("00015"), 0);
		Assert.assertEquals(15.0, PepperParserHelper.parseDouble("+00015"), 0);
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
		Assert.assertEquals(0.2D, PepperParserHelper.parseDouble("0.20000000000000001"), 0);
		Assert.assertEquals(0.2D, PepperParserHelper.parseDouble("0.2000000000000000111"), 0);
		Assert.assertEquals(0.2D, PepperParserHelper.parseDouble("0.2"), 0);
		Assert.assertEquals(0.2D, PepperParserHelper.parseDouble("0.2000"), 0);
		Assert.assertEquals(0.2D, PepperParserHelper.parseDouble("2.0E-1"), 0);
		Assert.assertEquals(0.2D, PepperParserHelper.parseDouble("2.000E-1"), 0);
	}

	@Test
	public void parseFloat() {
		Assert.assertEquals("1.0 Parsed", 1.0f, PepperParserHelper.parseFloat("1.0"), 0);
	}

	@Test(expected = NumberFormatException.class)
	public void parseFloatThrowsExceptionIfStringDoesNotRepresentAFloat() {
		PepperParserHelper.parseFloat("aaaa");
	}

	@Test
	public void parseFloatWithExplicitPositiveNumber() {
		assertEquals("+15 Parsed", 15.0f, PepperParserHelper.parseFloat("+15.0"), 0);
	}

	@Test
	public void parseFloatWithNegativeNumber() {
		assertEquals("-15 Parsed", -15.0f, PepperParserHelper.parseFloat("-15.0"), 0);
	}

}