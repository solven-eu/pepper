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
package cormoran.pepper.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import com.google.common.annotations.Beta;
import com.google.common.base.CharMatcher;
import com.google.common.base.Strings;

/**
 * Various utilities related to URL
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperURLHelper {
	protected PepperURLHelper() {
		// hidden
	}

	/**
	 * URL.equals can be slow and not return the expected result as it will relies on the resolution on the host given
	 * current JVM network configuration. One may prefer to rely on the actual String comparison
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	// https://stackoverflow.com/questions/3771081/proper-way-to-check-for-url-equality
	public static boolean equalsUrl(URL left, URL right) {
		if (left == null) {
			return right == null;
		} else if (right == null) {
			return left == null;
		} else {
			// none is null
			return Objects.equals(left.toExternalForm(), right.toExternalForm());
		}
	}

	private static final String DEFAULT_PROTOCOL = "http";

	/**
	 * 
	 * @param asString
	 * @return an URL associated to given String, adding the protocol "http://" by default
	 */
	public static URL toHttpURL(String asString) {
		if (Strings.isNullOrEmpty(asString)) {
			throw new IllegalArgumentException("Should not be null");
		}

		try {
			int indexOfSemiColumn = asString.indexOf(':');

			final boolean addHttpPrefix;
			if (indexOfSemiColumn < 0) {
				addHttpPrefix = true;
			} else {
				OptionalInt notProtocolChar = asString.chars()
						.limit(indexOfSemiColumn)
						.filter(c -> !CharMatcher.javaLetter().matches((char) c))
						.findFirst();

				if (notProtocolChar.isPresent()) {
					addHttpPrefix = true;
				} else {
					addHttpPrefix = false;
				}
			}

			if (addHttpPrefix) {
				// No protocol
				return new URL(DEFAULT_PROTOCOL + "://" + asString);
			} else {
				return new URL(asString);
			}

			// if (asString.startsWith("http://") || asString.startsWith("https://")) {
			//
			// } else {
			// }
		} catch (MalformedURLException e) {
			throw new RuntimeException("Issue while converting '" + asString + "'");
		}
	}

	public static Optional<PepperHostDescriptor> getHost(URL url) {
		Objects.requireNonNull(url);

		return PepperHostDescriptor.parseHost(url.getHost());
	}

	public static Optional<PepperHostDescriptor> getHost(String string) {
		return getHost(toHttpURL(string));
	}

	/**
	 * Create a new absolute URL, from a provided existing absolute URL and a relative URL component.
	 * 
	 * @param baseUrl
	 *            the existing absolute base URL
	 * @param relUrl
	 *            the relative URL to resolve. (If it's already absolute, it will be returned)
	 * @return an absolute URL if one was able to be generated, or the empty string if not
	 */
	// see org.jsoup.helper.StringUtil
	public static String resolve(final String baseUrl, final String relUrl) {
		URL base;
		try {
			try {
				base = new URL(baseUrl);
			} catch (MalformedURLException e) {
				// the base is unsuitable, but the attribute/rel may be abs on its own, so try that
				URL abs = new URL(relUrl);
				return abs.toExternalForm();
			}
			return resolve(base, relUrl).toExternalForm();
		} catch (MalformedURLException e) {
			return "";
		}

	}

	/**
	 * Create a new absolute URL, from a provided existing absolute URL and a relative URL component.
	 * 
	 * @param base
	 *            the existing absolute base URL
	 * @param relUrl
	 *            the relative URL to resolve. (If it's already absolute, it will be returned)
	 * @return the resolved absolute URL
	 * @throws MalformedURLException
	 *             if an error occurred generating the URL
	 */
	public static URL resolve(URL base, String relUrl) throws MalformedURLException {
		// workaround: java resolves '//path/file + ?foo' to '//path/?foo', not '//path/file?foo' as desired
		if (relUrl.startsWith("?"))
			relUrl = base.getPath() + relUrl;
		// workaround: //example.com + ./foo = //example.com/./foo, not //example.com/foo
		if (relUrl.indexOf('.') == 0 && base.getFile().indexOf('/') != 0) {
			base = new URL(base.getProtocol(), base.getHost(), base.getPort(), "/" + base.getFile());
		}
		return new URL(base, relUrl);
	}

	@Beta
	public static OutputStream outputStream(URI uri) throws IOException, MalformedURLException {
		if (uri.getScheme().equals("file")) {
			// For an unknown reason, the default connection to a file is not writable: we prepare the file manually
			return new FileOutputStream(Paths.get(uri).toFile());
		} else {
			return uri.toURL().openConnection().getOutputStream();
		}
	}
}
