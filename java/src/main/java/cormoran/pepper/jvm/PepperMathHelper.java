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
package cormoran.pepper.jvm;

/**
 * Helpers related to math and numbers
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperMathHelper {

	protected PepperMathHelper() {
		// hidden
	}

	/**
	 * This method is useful to understand the precision available through floats. It may be used to analyze a flow of
	 * doubles to check if float-precision would be sufficient
	 * 
	 * @param input
	 *            any Float
	 * @return the smallest float strictly bigger the the input
	 */
	public static float nextFloat(float input) {
		if (Float.isInfinite(input)) {
			// Infinite + finite value remains infinite
			return input;
		} else {
			// https://stackoverflow.com/questions/3658174/how-to-alter-a-float-by-its-smallest-increment-in-java
			return Float.intBitsToFloat(Float.floatToIntBits(input) + 1);
		}
	}
}
