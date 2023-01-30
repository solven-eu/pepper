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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A simple way of splitting up a String.
 */
public final class SimpleStringTokenizer implements Iterable<String> {
	private String subject;
	private char delim;

	/**
	 * Gets the different part of a string which are separated by the delimiter.
	 *
	 * @param subject
	 * @param delim
	 */
	public SimpleStringTokenizer(String subject, char delim) {
		this.subject = subject;
		this.delim = delim;
	}

	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			int position = 0;
			int maxPosition = subject.length();

			@Override
			public boolean hasNext() {
				return position < maxPosition;
			}

			@Override
			public String next() {
				if (position >= maxPosition)
					throw new NoSuchElementException();

				String answer;

				int p = subject.indexOf(delim, position);

				if (p < 0) {
					answer = subject.substring(position);
					position = maxPosition;
					return answer;
				} else {
					answer = subject.substring(position, p);
					position = p + 1;
				}

				return answer;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * Splits the string at the delimiter character.
	 *
	 * @param subject
	 * @param delim
	 * @return the string split at the delimiter
	 */
	public static String[] split(String subject, char delim) {
		List<String> answer = new ArrayList<String>();
		for (String s : new SimpleStringTokenizer(subject, delim))
			answer.add(s.trim());
		return answer.toArray(new String[0]);
	}
}
