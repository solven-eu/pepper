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
package eu.solven.pepper.util;

import java.util.Optional;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;

/**
 * Various helpers for {@link Optional}
 *
 * @author Benoit Lacelle
 *
 */
public class PepperOptionalHelper {

	protected PepperOptionalHelper() {
		// hidden
	}

	/**
	 *
	 * @param string
	 * @return an Optional empty if the string is null or empty
	 */
	public static Optional<String> fromNotEmpty(String string) {
		if (Strings.isNullOrEmpty(string)) {
			return Optional.empty();
		} else {
			return Optional.of(string);
		}
	}

	/**
	 *
	 * @param string
	 * @return an Optional empty if the string is null or empty
	 */
	@Beta
	public static Optional<String> fromNotBlank(String string) {
		// TODO: Very slow implementation
		if (Strings.isNullOrEmpty(string) || Strings.isNullOrEmpty(string.trim())) {
			return Optional.empty();
		} else {
			return Optional.of(string);
		}
	}
}
