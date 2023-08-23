/*******************************************************************************
 * Copyright (c) 2008, 2023 SAP AG, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - additional debug information
 *    Netflix (Jason Koch) - refactors for increased performance and concurrency
 *******************************************************************************/
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
			if (n > -1000) {
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
