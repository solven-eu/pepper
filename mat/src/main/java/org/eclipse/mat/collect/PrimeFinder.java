/**
 * The MIT License
 * Copyright (c) 2008-2010 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.collect;

/* package */class PrimeFinder {
	public static int findNextPrime(int floor) {
		boolean isPrime = false;
		while (!isPrime) {
			floor++;
			isPrime = true;
			int sqrt = (int) Math.sqrt(floor);
			for (int i = 2; i <= sqrt; i++) {
				if ((floor / i * i) == floor) {
					isPrime = false;
				}
			}
		}
		return floor;
	}

	public static int findPrevPrime(int ceil) {
		boolean isPrime = false;
		while (!isPrime) {
			ceil--;
			isPrime = true;
			int sqrt = (int) Math.sqrt(ceil);
			for (int i = 2; i <= sqrt; i++) {
				if ((ceil / i * i) == ceil) {
					isPrime = false;
				}
			}
		}
		return ceil;
	}
}
