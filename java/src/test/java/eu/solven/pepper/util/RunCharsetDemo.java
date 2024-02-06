/**
 * The MIT License
 * Copyright (c) 2023 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.util;

import static java.lang.System.out;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Demonstrate default Charset-related details.
 *
 * @author Benoit Lacelle
 */
// https://dzone.com/articles/java-may-use-utf-8-as-its-default-charset
public class RunCharsetDemo {

	protected RunCharsetDemo() {
		// hidden
	}

	/**
	 * Supplies the default encoding without using Charset.defaultCharset() and without accessing
	 * System.getProperty("file.encoding").
	 *
	 * @return Default encoding (default charset).
	 */
	public static String getEncoding() {
		final byte[] bytes = { 'D' };
		final InputStream inputStream = new ByteArrayInputStream(bytes);

		try (InputStreamReader reader = new InputStreamReader(inputStream, Charset.defaultCharset())) {
			final String encoding = reader.getEncoding();
			return encoding;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static void main(final String[] arguments) {
		out.println("Default Locale:   " + Locale.getDefault());
		out.println("Default Charset:  " + Charset.defaultCharset());
		out.println("file.encoding;    " + System.getProperty("file.encoding"));
		out.println("sun.jnu.encoding: " + System.getProperty("sun.jnu.encoding"));
		out.println("Default Encoding: " + getEncoding());
	}
}