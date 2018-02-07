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

import com.google.common.base.CharMatcher;

/**
 * Try to provide faster primitive faster than FLoat.parseFLoat and Double.parseDouble, even if these method are
 * intrisic methods.
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperParserHelper {
	private static double pow10[];

	private static final CharMatcher E_UPPER_MATCHER = CharMatcher.is('E').precomputed();
	private static final CharMatcher PLUS_MATCHER = CharMatcher.is('+').precomputed();
	private static final CharMatcher MINUS_MATCHER = CharMatcher.is('-').precomputed();
	private static final CharMatcher E_LOWER_MATCHER = CharMatcher.is('e').precomputed();
	private static final CharMatcher DOT_MATCHER = CharMatcher.is('.').precomputed();

	/**
	 * Initializes the cache for sin and cos and the rest.
	 */
	public static synchronized void initialize() {
		pow10 = new double[634];
		for (int i = 0; i < pow10.length; i++) {
			pow10[i] = Double.parseDouble("1.0e" + (i - 325)); // Math.pow(10.0, i-308);
		}
	}

	/**
	 * Parses a double from a CharSequence. Performance compared to intrinsic java method varies a lot: between 20%
	 * faster to 100% faster depending on the input values. The trick is to use Long.parseLong, which is extremely fast.
	 * 
	 * @param s
	 *            The string.
	 * @return The double value.
	 */
	public static double parseDouble(CharSequence s) {
		if (pow10 == null)
			initialize();
		if (s.charAt(0) == 'N' && s.charAt(1) == 'a' && s.charAt(2) == 'N')
			return Double.NaN;
		if (s.charAt(0) == 'I' && s.charAt(1) == 'n'
				&& s.charAt(2) == 'f'
				&& s.charAt(3) == 'i'
				&& s.charAt(4) == 'n'
				&& s.charAt(5) == 'i'
				&& s.charAt(6) == 't'
				&& s.charAt(7) == 'y')
			return Double.POSITIVE_INFINITY;

		if (s.charAt(0) == '-' && s.charAt(1) == 'I'
				&& s.charAt(2) == 'n'
				&& s.charAt(3) == 'f'
				&& s.charAt(4) == 'i'
				&& s.charAt(5) == 'n'
				&& s.charAt(6) == 'i'
				&& s.charAt(7) == 't'
				&& s.charAt(8) == 'y')
			return Double.POSITIVE_INFINITY;

		int first = 0;
		int last = s.length();

		if (PLUS_MATCHER.matches(s.charAt(0))) {
			// s = s.subSequence(1, s.length());
			first = 1;
		}

		int exp = 0;
		int e = E_UPPER_MATCHER.indexIn(s);
		if (e < 0) {
			e = E_LOWER_MATCHER.indexIn(s);
		}
		if (e >= 0) {
			int expStart;
			if (PLUS_MATCHER.matches(s.charAt(e + 1))) {
				expStart = e + 2;
			} else {
				expStart = e + 1;
			}
			exp = Jdk9CharSequenceParsers.parseShort(s, expStart, last, 10);
			last = e;
		} else {
			if (PLUS_MATCHER.lastIndexIn(s) > 0 || MINUS_MATCHER.lastIndexIn(s) > 0)
				throw new RuntimeException("Not a number");
		}

		int n;
		{
			int p = DOT_MATCHER.indexIn(s);

			if (p >= 0) {
				while (first < p && s.charAt(first) == '0') {
					// Skip the initial 0
					first++;
				}

				while (last > p + 1 && s.charAt(last - 1) == '0') {
					// Skip the trailing 0
					last--;
				}

				if (last == p + 1) {
					last = p;
					n = last - first;
					exp += n - 1;
				} else {

					// Adjust with first to handle the optional initial '+'
					exp += p - 1 - first;
					n = last - first - 1;

					if (first == p) {
						first = p + 1;
					} else {// TODO: this consumes transient memory
						s = new ConcatCharSequence(s.subSequence(first, p), s.subSequence(p + 1, last));
						first = 0;
					}
				}
			} else {
				n = last - first;
				exp += n - 1;
			}
		}

		final double intermediate;
		if (n == 0) {
			return 0D;
		} else if (n > 17) {
			intermediate = Jdk9CharSequenceParsers.parseLong(s, first, 17 + first, 10) * pow10[326 - 17];
		} else if (n < 9) {
			intermediate = Jdk9CharSequenceParsers.parseInt(s, first, first + n, 10) * pow10[326 - n];
		} else {
			intermediate = Jdk9CharSequenceParsers.parseLong(s, first, first + n, 10) * pow10[326 - n];
		}

		return intermediate * pow10[exp + 325];
	}

	public static float parseFloat(CharSequence floatAsCharSequence) {
		return (float) parseDouble(floatAsCharSequence);
	}
}
