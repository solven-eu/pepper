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
package eu.solven.pepper.jvm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.pepper.core.PepperMathHelper;

public class TestPepperMathHelper {

	@Test
	public void testNextFloat() {
		Assertions.assertEquals(1F, PepperMathHelper.nextFloat(1F), 0.000_001F);
	}

	@Test
	public void testNextFloat_Nan() {
		Assertions.assertTrue(Float.isNaN(PepperMathHelper.nextFloat(Float.NaN)));
	}

	@Test
	public void testNextFloat_Infinite() {
		float greaterThanInfinity = PepperMathHelper.nextFloat(Float.POSITIVE_INFINITY);
		Assertions.assertTrue(Float.isInfinite(greaterThanInfinity));
		Assertions.assertTrue(greaterThanInfinity > 0);
	}

	@Test
	public void testNextFloat_NegativeInfinite() {
		float greaterThanInfinity = PepperMathHelper.nextFloat(Float.NEGATIVE_INFINITY);
		Assertions.assertTrue(Float.isInfinite(greaterThanInfinity));
		Assertions.assertTrue(greaterThanInfinity < 0);
	}
}
