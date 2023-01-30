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

import java.io.Serializable;

/**
 * This class manages huge bit fields. It is much faster than {@link java.util.BitSet} and was specifically developed to
 * be used with huge bit sets in ISnapshot (e.g. needed in virtual GC traces). Out of performance reasons no method does
 * any parameter checking, i.e. only valid values are expected.
 */
public final class BitField implements Serializable {
	private static final long serialVersionUID = 1L;

	private int[] bits;

	/**
	 * Creates a bit field with the given number of bits. Size is expected to be positive - out of performance reasons
	 * no checks are done!
	 */
	public BitField(int size) {
		bits = new int[(((size) - 1) >>> 0x5) + 1];
	}

	/**
	 * Sets the bit on the given index. Index is expected to be in range - out of performance reasons no checks are
	 * done!
	 */
	public final void set(int index) {
		bits[index >>> 0x5] |= (1 << (index & 0x1f));
	}

	/**
	 * Clears the bit on the given index. Index is expected to be in range - out of performance reasons no checks are
	 * done!
	 */
	public final void clear(int index) {
		bits[index >>> 0x5] &= ~(1 << (index & 0x1f));
	}

	/**
	 * Gets the bit on the given index. Index is expected to be in range - out of performance reasons no checks are
	 * done!
	 */
	public final boolean get(int index) {
		return (bits[index >>> 0x5] & (1 << (index & 0x1f))) != 0;
	}
}
