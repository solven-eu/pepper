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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Not thread-safe, Mutable, no .hashcode/.equals subsequence, but does not allocate any memory
 * 
 * @author Benoit Lacelle
 *
 */
public class UnsafeSubSequence implements CharSequence {
	protected static final Logger LOGGER = LoggerFactory.getLogger(UnsafeSubSequence.class);

	protected final CharSequence undelrying;
	protected int start;
	protected int end;

	public UnsafeSubSequence(CharSequence undelrying) {
		this.undelrying = undelrying;
		this.start = -1;
		this.end = -1;
	}

	public UnsafeSubSequence(CharSequence undelrying, int from, int to) {
		this.undelrying = undelrying;
		this.start = from;
		this.end = to;
	}

	// Not-thread-safe
	public void resetWindow(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public boolean isValid() {
		return start >= 0 && end >= start;
	}

	@Override
	public int length() {
		// Empty String if end == start: OK
		return end - start;
	}

	@Override
	public char charAt(int index) {
		if (index >= end) {
			throw new IndexOutOfBoundsException(index + " is above " + index + " on " + this);
		} else if (index < 0) {
			throw new IndexOutOfBoundsException(index + " is below " + 0 + " on " + this);
		} else if (start + index >= undelrying.length()) {
			throw new IndexOutOfBoundsException("start=" + start
					+ " + index="
					+ index
					+ " is above underlying length="
					+ undelrying.length()
					+ " on "
					+ this);
		}
		return undelrying.charAt(start + index);
	}

	@Override
	public CharSequence subSequence(int subStart, int subEnd) {
		if (undelrying instanceof UnsafeSubSequence) {
			UnsafeSubSequence unsafeUnderlying = (UnsafeSubSequence) undelrying;
			return new UnsafeSubSequence(unsafeUnderlying.undelrying,
					unsafeUnderlying.start + subStart,
					unsafeUnderlying.start + subEnd);
		} else {
			return new UnsafeSubSequence(undelrying, this.start + subStart, this.start + subEnd);
		}
	}

	@Override
	public int hashCode() {
		throw new RuntimeException("UNsafe");
	}

	@Override
	public boolean equals(Object obj) {
		throw new RuntimeException("UNsafe");
	}

	@Override
	public String toString() {
		if (isValid()) {
			return undelrying.subSequence(start, end).toString();
		} else {
			LOGGER.warn("We unexpectedly used .toString on a not initialized {}", getClass());
			return this.getClass() + " Not Initialized";
		}
	}
}
