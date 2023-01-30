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
import java.util.NoSuchElementException;

/**
 * Utility class to hold a set of ints Similar to a Set, but efficient for ints
 */
public final class SetInt implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Largest requested size that can be allocated on many VMs. Size will be rounded up to the next prime, so choose
	 * prime - 1. Biggest primes less than 2^31 are 0x7fffffff and 0x7fffffed, but JVM limit can be less than
	 * Integer.MAX_VALUE. E.g. ArrayList has a limit of Integer.MAX_VALUE - 8
	 */
	private static final int BIG_CAPACITY = PrimeFinder.findPrevPrime(Integer.MAX_VALUE - 8 + 1) - 1;

	private int capacity;
	private int step;
	private int limit;
	private int size;
	private boolean[] used;
	private int[] keys;

	/**
	 * Create a set of default size
	 */
	public SetInt() {
		this(10);
	}

	/**
	 * Create a set of given size
	 *
	 * @param initialCapacity
	 */
	public SetInt(int initialCapacity) {
		init(initialCapacity);
	}

	/**
	 * Add a value to the set
	 *
	 * @param key
	 *            the value to add
	 * @return return true if added
	 */
	public boolean add(int key) {
		if (size == limit) {
			// Double in size but avoid overflow or JVM limits
			resize(capacity <= BIG_CAPACITY >> 1 ? capacity << 1
					: capacity < BIG_CAPACITY ? BIG_CAPACITY : capacity + 1);
		}
		int hash = (key & Integer.MAX_VALUE) % capacity;
		while (used[hash]) {
			if (keys[hash] == key) {
				return false;
			}
			hash = (hash + step) % capacity;
		}
		used[hash] = true;
		keys[hash] = key;
		size++;
		return true;
	}

	/**
	 * Remove a value from the set
	 *
	 * @param key
	 *            the value to add
	 * @return return true if removed
	 */
	public boolean remove(int key) {
		int hash = (key & Integer.MAX_VALUE) % capacity;
		while (used[hash]) {
			if (keys[hash] == key) {
				used[hash] = false;
				size--;
				// Re-hash all follow-up entries anew; Do not fiddle with the
				// capacity limit (75 %) otherwise this code may loop forever
				hash = (hash + step) % capacity;
				while (used[hash]) {
					key = keys[hash];
					used[hash] = false;
					int newHash = (key & Integer.MAX_VALUE) % capacity;
					while (used[newHash]) {
						newHash = (newHash + step) % capacity;
					}
					used[newHash] = true;
					keys[newHash] = key;
					hash = (hash + step) % capacity;
				}
				return true;
			}
			hash = (hash + step) % capacity;
		}
		return false;
	}

	/**
	 * Find a value from the set
	 *
	 * @param key
	 *            the value to find
	 * @return return true if found
	 */
	public boolean contains(int key) {
		int hash = (key & Integer.MAX_VALUE) % capacity;
		while (used[hash]) {
			if (keys[hash] == key) {
				return true;
			}
			hash = (hash + step) % capacity;
		}
		return false;
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
	 * is the set empty
	 *
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * clear all the entries
	 */
	public void clear() {
		size = 0;
		used = new boolean[capacity];
	}

	/**
	 * get an iterator to go through the set
	 *
	 * @return the iterator
	 */
	public IteratorInt iterator() {
		return new IteratorInt() {
			int n = 0;
			int i = -1;

			@Override
			public boolean hasNext() {
				return n < size;
			}

			@Override
			public int next() throws NoSuchElementException {
				while (++i < used.length) {
					if (used[i]) {
						n++;
						return keys[i];
					}
				}
				throw new NoSuchElementException();
			}
		};
	}

	/**
	 * convert to an array
	 *
	 * @return a copy of the entries
	 */
	public int[] toArray() {
		int[] array = new int[size];
		int j = 0;
		for (int i = 0; i < used.length; i++) {
			if (used[i]) {
				array[j++] = keys[i];
			}
		}
		return array;
	}

	private void init(int initialCapacity) {
		capacity = PrimeFinder.findNextPrime(initialCapacity);
		step = Math.max(1, PrimeFinder.findPrevPrime(initialCapacity / 3));
		limit = (int) (capacity * 0.75);
		clear();
		keys = new int[capacity];
	}

	private void resize(int newCapacity) {
		int oldSize = size;
		boolean[] oldUsed = used;
		int[] oldKeys = keys;
		// JIT bug with IBM Java 6.0 - avoid JIT using stale values
		keys = null;
		capacity = 0;
		// end work-around
		init(newCapacity);
		int key;
		int hash;
		for (int i = 0; i < oldUsed.length; i++) {
			if (oldUsed[i]) {
				key = oldKeys[i];
				hash = (key & Integer.MAX_VALUE) % capacity;
				while (used[hash]) {
					hash = (hash + step) % capacity;
				}
				used[hash] = true;
				keys[hash] = key;
			}
		}
		size = oldSize;
	}
}
