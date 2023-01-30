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

import java.util.Arrays;

import org.eclipse.mat.report.internal.Messages;
import org.eclipse.mat.util.MessageUtil;

/**
 * Utility class to hold a list of longs Similar to a list, but efficient for longs
 */
public final class ArrayLong {
	long elements[];
	int size;

	/**
	 * Create a list of default size
	 */
	public ArrayLong() {
		this(10);
	}

	/**
	 * Create a list of given size
	 *
	 * @param initialCapacity
	 */
	public ArrayLong(int initialCapacity) {
		elements = new long[initialCapacity];
		size = 0;
	}

	/**
	 * Create a list based on a supplied array
	 *
	 * @param initialValues
	 *            a copy is taken of this array
	 */
	public ArrayLong(long[] initialValues) {
		this(initialValues.length);
		System.arraycopy(initialValues, 0, elements, 0, initialValues.length);
		size = initialValues.length;
	}

	/**
	 * Create a list based on an existing ArrayInt, of size of the template
	 *
	 * @param template
	 *            a copy is taken of these values
	 */
	public ArrayLong(ArrayLong template) {
		this(template.size);
		System.arraycopy(template.elements, 0, elements, 0, template.size);
		size = template.size;
	}

	/**
	 * append one more entry
	 *
	 * @param element
	 *            the int to add to the end
	 */
	public void add(long element) {
		ensureCapacity(size + 1);
		elements[size++] = element;
	}

	/**
	 * append a group of entries
	 *
	 * @param elements
	 */
	public void addAll(long[] elements) {
		ensureCapacity(size + elements.length);
		System.arraycopy(elements, 0, this.elements, size, elements.length);
		size += elements.length;
	}

	/**
	 * append all of another
	 *
	 * @param template
	 */
	public void addAll(ArrayLong template) {
		ensureCapacity(size + template.size);
		System.arraycopy(template.elements, 0, elements, size, template.size);
		size += template.size;
	}

	/**
	 * modify one particular entry
	 *
	 * @param index
	 * @param element
	 * @return the previous value
	 */
	public long set(int index, long element) {
		if (index < 0 || index >= size)
			throw new ArrayIndexOutOfBoundsException(index);

		long oldValue = elements[index];
		elements[index] = element;
		return oldValue;
	}

	/**
	 * retrieve one entry
	 *
	 * @param index
	 * @return the entry
	 */
	public long get(int index) {
		if (index < 0 || index >= size)
			throw new ArrayIndexOutOfBoundsException(index);
		return elements[index];
	}

	/**
	 * get the number of used entries
	 *
	 * @return the number of entries
	 */
	public int size() {
		return size;
	}

	/**
	 * convert to an array
	 *
	 * @return a copy of the entries
	 */
	public long[] toArray() {
		long[] result = new long[size];
		System.arraycopy(elements, 0, result, 0, size);
		return result;
	}

	/**
	 * is the list empty
	 *
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * get an iterator to go through the list
	 *
	 * @return the iterator
	 */
	public IteratorLong iterator() {
		return new IteratorLong() {
			int index = 0;

			@Override
			public boolean hasNext() {
				return index < size;
			}

			@Override
			public long next() {
				return elements[index++];
			}
		};
	}

	/**
	 * clear all the entries
	 */
	public void clear() {
		size = 0;
	}

	/**
	 * get the last entry to be written. Must be at least one entry.
	 *
	 * @return the last element
	 */
	public long lastElement() {
		return elements[size - 1];
	}

	/**
	 * get the first entry to be written. Must be at least one entry.
	 *
	 * @return the first element
	 */
	public long firstElement() {
		if (size == 0)
			throw new ArrayIndexOutOfBoundsException();

		return elements[0];
	}

	/**
	 * arrange the entries in ascending order
	 */
	public void sort() {
		Arrays.sort(elements, 0, size);
	}

	// //////////////////////////////////////////////////////////////
	// implementation stuff
	// //////////////////////////////////////////////////////////////

	private void ensureCapacity(int minCapacity) {
		int oldCapacity = elements.length;
		if (minCapacity > oldCapacity) {
			int newCapacity = newCapacity(oldCapacity, minCapacity);
			if (newCapacity < minCapacity) {
				// Avoid strange exceptions later
				throw new OutOfMemoryError(
						MessageUtil.format(Messages.ArrayLong_Error_LengthExceeded, minCapacity, newCapacity));
			}
			long oldData[] = elements;
			elements = new long[newCapacity];
			System.arraycopy(oldData, 0, elements, 0, size);
		}
	}

	private int newCapacity(int oldCapacity, int minCapacity) {
		// Scale by 1.5 without overflow
		int newCapacity = oldCapacity * 3 >>> 1;
		if (newCapacity < minCapacity) {
			newCapacity = minCapacity * 3 >>> 1;
			if (newCapacity < minCapacity) {
				// Avoid VM limits for final size
				newCapacity = Integer.MAX_VALUE - 8;
			}
		}
		return newCapacity;
	}
}
