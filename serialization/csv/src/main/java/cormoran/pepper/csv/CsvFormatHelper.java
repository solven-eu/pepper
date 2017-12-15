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
package cormoran.pepper.csv;

import java.util.Comparator;
import java.util.Optional;
import java.util.OptionalInt;

import com.google.common.base.CharMatcher;

/**
 * Some helpers for CSV files,typically to help inferring the CSV format
 * 
 * @author Benoit Lacelle
 *
 */
public class CsvFormatHelper {
	protected CsvFormatHelper() {
		// hidden
	}

	public static String defaultSeparatorCandidates() {
		return ";,|\t";
	}

	public static OptionalInt guessSeparator(String row) {
		return guessSeparator(CharMatcher.anyOf(defaultSeparatorCandidates()), row);
	}

	public static OptionalInt guessSeparator(CharMatcher allowedSeparators, String row) {
		if (row == null || row.isEmpty()) {
			return OptionalInt.empty();
		} else {
			// Search a separator amongst an hardcoded list of good candidates
			// mapToObj for custom comparator

			String candidateSeparators = allowedSeparators.retainFrom(row);

			Optional<Integer> optMax = candidateSeparators.chars().mapToObj(Integer::valueOf).max(
					Comparator.comparing(i -> CharMatcher.is((char) i.intValue()).countIn(row)));

			if (optMax.isPresent()) {
				int max = optMax.get().intValue();

				return OptionalInt.of(max);
			} else {
				return OptionalInt.empty();
			}
		}
	}
}
