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
package org.eclipse.mat.util;

import com.ibm.icu.text.NumberFormat;

/**
 * A way of adding units to values.
 */
public abstract class Units {
	public enum Storage {
		BYTE("B", 1L), KILOBYTE("KB", 1L << 10), MEGABYTE("MB", 1L << 20), GIGABYTE("GB", 1L << 30);

		private final String symbol;
		private final long divider; // divider of BASE unit

		Storage(String name, long divider) {
			this.symbol = name;
			this.divider = divider;
		}

		public static Storage of(final long number) {
			long n;
			if (number > 0) {
				n = -number;
			} else {
				n = number;
			}
			if (n > -(1L << 10)) {
				return BYTE;
			} else if (n > -(1L << 20)) {
				return KILOBYTE;
			} else if (n > -(1L << 30)) {
				return MEGABYTE;
			} else {
				return GIGABYTE;
			}
		}

		public String format(long number) {
			return nf.format((double) number / divider) + " " + symbol;
		}
	}

	public enum Plain {
		BASE(null, 1L), //
		THOUSANDS("k", 1000L), MILLIONS("m", 1000000L);

		private final String symbol;
		private final long divider; // divider of BASE unit

		Plain(String name, long divider) {
			this.symbol = name;
			this.divider = divider;
		}

		public static Plain of(final long number) {
			long n;
			if (number > 0) {
				n = -number;
			} else {
				n = number;
			}
			if (n > -1_000) {
				return BASE;
			} else if (n > -1_000_000) {
				return THOUSANDS;
			} else {
				return MILLIONS;
			}
		}

		public String format(long number) {
			String f = nf.format((double) number / divider);
			if (symbol != null) {
				return f + symbol;
			} else {
				return f;
			}
		}
	}

	/**
	 * NumberFormat is not thread safe.
	 */
	private static NumberFormat nf = NumberFormat.getInstance();

	static {
		nf.setGroupingUsed(false);
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(1);
	}

}
