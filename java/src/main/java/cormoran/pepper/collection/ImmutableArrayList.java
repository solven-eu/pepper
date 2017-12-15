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
package cormoran.pepper.collection;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A List implementation, improving performance when List are used as a Map key, or when one need to extract only a
 * subset of indexes from an input array
 * 
 * @author Benoit Lacelle
 *
 * @param <T>
 */
public final class ImmutableArrayList<T> extends AbstractList<T> {

	private static final int HASHCODE_CONSTANT = 31;

	protected final T[] underlyingArray;
	protected final int[] indexes;
	protected final int hashCode;

	public ImmutableArrayList(T[] underlyingArray, int[] indexes) {
		this.underlyingArray = underlyingArray;
		this.indexes = indexes;

		// Precompute hashCode for better performance in TuplesToInsertForVersioningKeyId
		// Do not use super.hashCode else it would generate a List.Iterator
		this.hashCode = filteredHashCode(underlyingArray, indexes);
	}

	public ImmutableArrayList(T[] underlyingArray) {
		this.underlyingArray = underlyingArray;
		this.indexes = null;

		// Should be the same than filteredHashCode
		this.hashCode = Arrays.hashCode(underlyingArray);
	}

	private static int filteredHashCode(Object[] array, int[] indexes) {
		if (array == null) {
			return 0;
		}

		int result = 1;
		for (int index : indexes) {
			result = HASHCODE_CONSTANT * result;
			if (array[index] != null) {
				result += array[index].hashCode();
			}
		}

		return result;
	}

	@Override
	public T get(int index) {
		if (indexes == null) {
			return underlyingArray[index];
		} else {
			return underlyingArray[indexes[index]];
		}
	}

	@Override
	public T set(int index, T value) {
		throw new UnsupportedOperationException("Do not mutate as we pre-computed the hashcode");
	}

	@Override
	public int size() {
		if (indexes == null) {
			return underlyingArray.length;
		} else {
			return indexes.length;
		}
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	// Based on AbstractList.equals
	// We assert o will be a RandomAccess list
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof List)) {
			return false;
		}

		List<?> oList = (List<?>) o;

		if (oList.size() != this.size()) {
			return false;
		}

		if (indexes == null) {
			for (int i = 0; i < underlyingArray.length; i++) {
				Object o1 = this.get(i);
				Object o2 = ((List<?>) o).get(i);
				if (!Objects.equals(o1, o2)) {
					return false;
				}
			}
		} else {
			for (int i = 0; i < indexes.length; i++) {
				Object o1 = this.get(i);
				Object o2 = ((List<?>) o).get(i);
				if (!Objects.equals(o1, o2)) {
					return false;
				}
			}
		}

		return true;
	}
}
