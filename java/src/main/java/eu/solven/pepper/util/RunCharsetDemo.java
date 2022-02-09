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