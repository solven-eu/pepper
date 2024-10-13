/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestGZipStringBuilder {
	@Test
	public void testAppend() {
		GZipStringBuilder sb = new GZipStringBuilder();

		sb.append("Azaz");
		sb.append(new StringBuilder("Zeze"));

		Assertions.assertEquals("AzazZeze", sb.toString());

		sb.clear();
		Assertions.assertEquals("", sb.toString());
	}

	@Test
	public void testAppendSub() {
		GZipStringBuilder sb = new GZipStringBuilder();

		sb.append("Azaz", 1, 3);

		Assertions.assertEquals("za", sb.toString());
	}

	@Test
	public void testAppendStringBuilder() {
		GZipStringBuilder sb = new GZipStringBuilder();

		sb.append(new StringBuilder("Azaz"), 1, 3);

		Assertions.assertEquals("za", sb.toString());
	}

	@Test
	public void testNull() {
		GZipStringBuilder sb = new GZipStringBuilder();

		sb.append(null);

		Assertions.assertEquals("null", sb.toString());
	}

	@Test
	public void testCopyInflated() {
		GZipStringBuilder sb = new GZipStringBuilder();

		sb.append("Azaz");

		Assertions.assertTrue(sb.copyInflatedByteArray().length > 0);
	}
}
