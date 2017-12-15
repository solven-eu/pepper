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

import java.util.Objects;

//https://github.com/netroby/jdk9-dev/blob/master/jdk/src/java.base/share/classes/java/lang/Integer.java
// https://github.com/netroby/jdk9-dev/blob/master/jdk/src/java.base/share/classes/java/lang/Long.java
//https://github.com/netroby/jdk9-dev/blob/master/jdk/src/java.base/share/classes/java/lang/Short.java
/**
 * Backport from JDK9 .parse(CharSequence)
 * 
 * @author Benoit Lacelle
 *
 */
// https://stackoverflow.com/questions/5767747/pmd-cpd-ignore-bits-of-code-using-comments
@SuppressWarnings("CPD-START")
public class Jdk9CharSequenceParsers {
	/**
	 * Parses the {@link CharSequence} argument as a signed {@code int} in the specified {@code radix}, beginning at the
	 * specified {@code beginIndex} and extending to {@code endIndex - 1}.
	 *
	 * <p>
	 * The method does not take steps to guard against the {@code CharSequence} being mutated while parsing.
	 *
	 * @param s
	 *            the {@code CharSequence} containing the {@code int} representation to be parsed
	 * @param beginIndex
	 *            the beginning index, inclusive.
	 * @param endIndex
	 *            the ending index, exclusive.
	 * @param radix
	 *            the radix to be used while parsing {@code s}.
	 * @return the signed {@code int} represented by the subsequence in the specified radix.
	 * @throws NullPointerException
	 *             if {@code s} is null.
	 * @throws IndexOutOfBoundsException
	 *             if {@code beginIndex} is negative, or if {@code beginIndex} is greater than {@code endIndex} or if
	 *             {@code endIndex} is greater than {@code s.length()}.
	 * @throws NumberFormatException
	 *             if the {@code CharSequence} does not contain a parsable {@code int} in the specified {@code radix},
	 *             or if {@code radix} is either smaller than {@link java.lang.Character#MIN_RADIX} or larger than
	 *             {@link java.lang.Character#MAX_RADIX}.
	 * @since 9
	 */
	public static int parseInt(CharSequence s, int beginIndex, int endIndex, int radix) throws NumberFormatException {
		s = Objects.requireNonNull(s);

		if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length()) {
			throw new IndexOutOfBoundsException();
		}
		if (radix < Character.MIN_RADIX) {
			throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
		}
		if (radix > Character.MAX_RADIX) {
			throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
		}

		boolean negative = false;
		int i = beginIndex;
		int limit = -Integer.MAX_VALUE;

		if (i < endIndex) {
			char firstChar = s.charAt(i);
			if (firstChar < '0') { // Possible leading "+" or "-"
				if (firstChar == '-') {
					negative = true;
					limit = Integer.MIN_VALUE;
				} else if (firstChar != '+') {
					throw forCharSequence(s, beginIndex, endIndex, i);
				}
				i++;
				if (i == endIndex) { // Cannot have lone "+" or "-"
					throw forCharSequence(s, beginIndex, endIndex, i);
				}
			}
			int multmin = limit / radix;
			int result = 0;
			while (i < endIndex) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				int digit = Character.digit(s.charAt(i), radix);
				if (digit < 0 || result < multmin) {
					throw forCharSequence(s, beginIndex, endIndex, i);
				}
				result *= radix;
				if (result < limit + digit) {
					throw forCharSequence(s, beginIndex, endIndex, i);
				}
				i++;
				result -= digit;
			}
			return negative ? result : -result;
		} else {
			throw forInputString("");
		}
	}

	/**
	 * Parses the {@link CharSequence} argument as a signed {@code long} in the specified {@code radix}, beginning at
	 * the specified {@code beginIndex} and extending to {@code endIndex - 1}.
	 *
	 * <p>
	 * The method does not take steps to guard against the {@code CharSequence} being mutated while parsing.
	 *
	 * @param s
	 *            the {@code CharSequence} containing the {@code long} representation to be parsed
	 * @param beginIndex
	 *            the beginning index, inclusive.
	 * @param endIndex
	 *            the ending index, exclusive.
	 * @param radix
	 *            the radix to be used while parsing {@code s}.
	 * @return the signed {@code long} represented by the subsequence in the specified radix.
	 * @throws NullPointerException
	 *             if {@code s} is null.
	 * @throws IndexOutOfBoundsException
	 *             if {@code beginIndex} is negative, or if {@code beginIndex} is greater than {@code endIndex} or if
	 *             {@code endIndex} is greater than {@code s.length()}.
	 * @throws NumberFormatException
	 *             if the {@code CharSequence} does not contain a parsable {@code int} in the specified {@code radix},
	 *             or if {@code radix} is either smaller than {@link java.lang.Character#MIN_RADIX} or larger than
	 *             {@link java.lang.Character#MAX_RADIX}.
	 * @since 9
	 */
	public static long parseLong(CharSequence s, int beginIndex, int endIndex, int radix) throws NumberFormatException {
		s = Objects.requireNonNull(s);

		if (beginIndex < 0 || beginIndex > endIndex || endIndex > s.length()) {
			throw new IndexOutOfBoundsException();
		}
		if (radix < Character.MIN_RADIX) {
			throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
		}
		if (radix > Character.MAX_RADIX) {
			throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
		}

		boolean negative = false;
		int i = beginIndex;
		long limit = -Long.MAX_VALUE;

		if (i < endIndex) {
			char firstChar = s.charAt(i);
			if (firstChar < '0') { // Possible leading "+" or "-"
				if (firstChar == '-') {
					negative = true;
					limit = Long.MIN_VALUE;
				} else if (firstChar != '+') {
					throw forCharSequence(s, beginIndex, endIndex, i);
				}
				i++;
			}
			if (i >= endIndex) { // Cannot have lone "+", "-" or ""
				throw forCharSequence(s, beginIndex, endIndex, i);
			}
			long multmin = limit / radix;
			long result = 0;
			while (i < endIndex) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				int digit = Character.digit(s.charAt(i), radix);
				if (digit < 0 || result < multmin) {
					throw forCharSequence(s, beginIndex, endIndex, i);
				}
				result *= radix;
				if (result < limit + digit) {
					throw forCharSequence(s, beginIndex, endIndex, i);
				}
				i++;
				result -= digit;
			}
			return negative ? result : -result;
		} else {
			throw new NumberFormatException("");
		}
	}

	/**
	 * Parses the string argument as a signed {@code short} in the radix specified by the second argument. The
	 * characters in the string must all be digits, of the specified radix (as determined by whether
	 * {@link java.lang.Character#digit(char, int)} returns a nonnegative value) except that the first character may be
	 * an ASCII minus sign {@code '-'} ({@code '\u005Cu002D'}) to indicate a negative value or an ASCII plus sign
	 * {@code '+'} ({@code '\u005Cu002B'}) to indicate a positive value. The resulting {@code short} value is returned.
	 *
	 * <p>
	 * An exception of type {@code NumberFormatException} is thrown if any of the following situations occurs:
	 * <ul>
	 * <li>The first argument is {@code null} or is a string of length zero.
	 *
	 * <li>The radix is either smaller than {@link java.lang.Character#MIN_RADIX} or larger than
	 * {@link java.lang.Character#MAX_RADIX}.
	 *
	 * <li>Any character of the string is not a digit of the specified radix, except that the first character may be a
	 * minus sign {@code '-'} ({@code '\u005Cu002D'}) or plus sign {@code '+'} ({@code '\u005Cu002B'}) provided that the
	 * string is longer than length 1.
	 *
	 * <li>The value represented by the string is not a value of type {@code short}.
	 * </ul>
	 *
	 * @param s
	 *            the {@code String} containing the {@code short} representation to be parsed
	 * @param radix
	 *            the radix to be used while parsing {@code s}
	 * @return the {@code short} represented by the string argument in the specified radix.
	 * @throws NumberFormatException
	 *             If the {@code String} does not contain a parsable {@code short}.
	 */
	// We change this method in order to accept a CharSequence
	public static short parseShort(CharSequence s, int beginIndex, int endIndex, int radix)
			throws NumberFormatException {
		int i = parseInt(s, beginIndex, endIndex, radix);
		if (i < Short.MIN_VALUE || i > Short.MAX_VALUE)
			throw new NumberFormatException(
					"Value out of range. Value:\"" + s.subSequence(beginIndex, endIndex) + "\" Radix:" + radix);
		return (short) i;
	}

	/**
	 * Factory method for making a {@code NumberFormatException} given the specified input which caused the error.
	 *
	 * @param s
	 *            the input causing the error
	 */
	static NumberFormatException forInputString(String s) {
		return new NumberFormatException("For input string: \"" + s + "\"");
	}

	/**
	 * Factory method for making a {@code NumberFormatException} given the specified input which caused the error.
	 *
	 * @param s
	 *            the input causing the error
	 * @param beginIndex
	 *            the beginning index, inclusive.
	 * @param endIndex
	 *            the ending index, exclusive.
	 * @param errorIndex
	 *            the index of the first error in s
	 */
	static NumberFormatException forCharSequence(CharSequence s, int beginIndex, int endIndex, int errorIndex) {
		return new NumberFormatException(
				"Error at index " + (errorIndex - beginIndex) + " in: \"" + s.subSequence(beginIndex, endIndex) + "\"");
	}
}
