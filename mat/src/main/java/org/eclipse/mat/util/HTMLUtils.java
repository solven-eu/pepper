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

/**
 * Helpers for building HTML reports.
 */
public final class HTMLUtils {

	/**
	 * Escape input text so that characters are not misinterpreted by HTML parsers.
	 *
	 * @param text
	 * @return the escaped text
	 */
	public static String escapeText(String text) {
		final StringBuilder result = new StringBuilder(text.length() * 120 / 100);

		for (int ii = 0; ii < text.length(); ii++) {
			char ch = text.charAt(ii);
			if (ch == '<')
				result.append("&lt;");
			else if (ch == '>')
				result.append("&gt;");
			else if (ch == '&')
				result.append("&amp;");
			else if (ch == '\u00bb')
				result.append("&raquo;");
			else
				result.append(ch);
		}

		return result.toString();
	}

	private HTMLUtils() {
	}
}
