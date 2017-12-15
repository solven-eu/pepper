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

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.CharMatcher;

public class TestPepperFormatHelper {
	@Test
	public void testGuessSeparator_empty() {
		String row = "";

		Assert.assertFalse(CsvFormatHelper.guessSeparator(row).isPresent());
	}

	@Test
	public void testGuessSeparator_onlyWord() {
		String row = "field1field2field3";

		Assert.assertFalse(CsvFormatHelper.guessSeparator(row).isPresent());
	}

	@Test
	public void testGuessSeparator_Coma() {
		String row = "field1,field2,field3";

		Assert.assertEquals(',', CsvFormatHelper.guessSeparator(row).getAsInt());
	}

	@Test
	public void testGuessSeparator_Tabulation() {
		String row = "field1\tfield2\tfield3";

		Assert.assertEquals('\t', CsvFormatHelper.guessSeparator(row).getAsInt());
	}

	@Test
	public void testGuessSeparator_CustomWordChare() {
		String row = "youApiAerf";

		Assert.assertEquals('A', CsvFormatHelper.guessSeparator(CharMatcher.anyOf("abAB"), row).getAsInt());
	}
}
