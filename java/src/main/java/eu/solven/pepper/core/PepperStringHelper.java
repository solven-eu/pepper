package eu.solven.pepper.core;

import java.text.Normalizer;
import java.util.Locale;

public class PepperStringHelper {

	// Non-breaking space isn't in \\s so we add \\u00A0
	private static final String REGEX_WHITESPACE = "\\u00A0\\s";

	private static final char INSTEAD_OF_SPECIAL_CHAR = '_';
	private static final String INSTEAD_OF_SPECIAL_CHAR_STRING = Character.toString(INSTEAD_OF_SPECIAL_CHAR);

	/**
	 * 
	 * @param text
	 * @return a clean and simplified version of given text. We clean white spaces characters with single whitespace,
	 *         trim, replace accentuated character by simple character, lower-case,...
	 */
	public static String simplify(String text) {
		// remove accent, remove punctuation, normalize white space, remove leading and trailing whitespace with trim,
		// lower case
		// https://stackoverflow.com/questions/5697171/regex-what-is-incombiningdiacriticalmarks
		// https://stackoverflow.com/questions/18830813/how-can-i-remove-punctuation-from-input-text-in-java
		// https://stackoverflow.com/questions/15633228/how-to-remove-all-white-spaces-in-java/15633284#15633284
		return Normalizer.normalize(text, Normalizer.Form.NFD)
				// https://stackoverflow.com/questions/5697171/regex-what-is-incombiningdiacriticalmarks
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
				// Handle `\r\n` as a single whitespace
				.replaceAll("\r\n", " ")
				// Simplifies whitespace characters
				.replaceAll("[" + REGEX_WHITESPACE + "]", " ")
				// We replace all special characters with a '_', expect '-' which is often meaningful (especially in
				// addresses)
				.replaceAll("[^\\p{ASCII}]+", INSTEAD_OF_SPECIAL_CHAR_STRING);
	}

	/**
	 * Goes farther than {@link #simplify(String)}, by replacing consecutive white-spaces by a single whitespace,
	 * trimming and lowerCasing.
	 * 
	 * @param text
	 * @return a normalized version, meaning we convert to a simple version of the string, which is generally considered
	 *         equivalent but simpler. Apply lowerCase.
	 */
	public static String normalize(String text) {
		// remove leading and trailing whitespace with trim,
		// lower case
		return simplify(text).replaceAll("[ ]+", " ").trim().toLowerCase(Locale.US);
	}

}
