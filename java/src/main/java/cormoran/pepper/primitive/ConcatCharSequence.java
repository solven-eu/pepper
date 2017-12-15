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

/**
 * Enable concatenating 2 CharSequence at the cost of a very small object. It is much more memory-efficient than
 * concatenating Strings
 * 
 * @author Benoit Lacelle
 *
 */
public class ConcatCharSequence implements CharSequence {
	protected final CharSequence left;
	protected final CharSequence right;

	public ConcatCharSequence(CharSequence left, CharSequence right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public int length() {
		return left.length() + right.length();
	}

	@Override
	public char charAt(int index) {
		if (index >= left.length()) {
			return right.charAt(index - left.length());
		} else {
			return left.charAt(index);
		}
	}

	@Override
	public CharSequence subSequence(int start, int end) {
		if (end < left.length()) {
			return left.subSequence(start, end);
		} else if (start >= left.length()) {
			return right.subSequence(start - left.length(), end - left.length());
		} else {
			return new ConcatCharSequence(left.subSequence(start, left.length()),
					right.subSequence(0, end - left.length()));
		}
	}

	@Override
	public String toString() {
		return left.toString() + right.toString();
	}
}
