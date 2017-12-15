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
package cormoran.pepper.util;

/**
 * Port of org.apache.commons.lang3.SystemUtils.OS_NAME_WINDOWS_PREFIX
 * 
 * @author Benoit Lacelle
 *
 */
class SystemUtils {
	private SystemUtils() {
		// hidden
	}

	/**
	 * The prefix String for all Windows OS.
	 */
	private static final String OS_NAME_WINDOWS_PREFIX = "Windows";

	/**
	 * <p>
	 * The {@code os.name} System Property. Operating system name.
	 * </p>
	 * <p>
	 * Defaults to {@code null} if the runtime does not have security access to read this property or the property does
	 * not exist.
	 * </p>
	 * <p>
	 * This value is initialized when the class is loaded. If {@link System#setProperty(String,String)} or
	 * {@link System#setProperties(java.util.Properties)} is called after this class is loaded, the value will be out of
	 * sync with that System property.
	 * </p>
	 *
	 * @since Java 1.1
	 */
	public static final String OS_NAME = getSystemProperty("os.name");

	/**
	 * <p>
	 * Is {@code true} if this is Windows.
	 * </p>
	 * <p>
	 * The field will return {@code false} if {@code OS_NAME} is {@code null}.
	 * </p>
	 *
	 * @since 2.0
	 */
	public static final boolean IS_OS_WINDOWS = getOSMatchesName(OS_NAME_WINDOWS_PREFIX);

	/**
	 * <p>
	 * Is {@code true} if this is Linux.
	 * </p>
	 * <p>
	 * The field will return {@code false} if {@code OS_NAME} is {@code null}.
	 * </p>
	 *
	 * @since 2.0
	 */
	public static final boolean IS_OS_LINUX = getOSMatchesName("Linux") || getOSMatchesName("LINUX");

	/**
	 * <p>
	 * Is {@code true} if this is Mac.
	 * </p>
	 * <p>
	 * The field will return {@code false} if {@code OS_NAME} is {@code null}.
	 * </p>
	 *
	 * @since 2.0
	 */
	public static final boolean IS_OS_MAC = getOSMatchesName("Mac");

	/**
	 * Decides if the operating system matches.
	 *
	 * @param osNamePrefix
	 *            the prefix for the os name
	 * @return true if matches, or false if not or can't determine
	 */
	private static boolean getOSMatchesName(final String osNamePrefix) {
		return isOSNameMatch(OS_NAME, osNamePrefix);
	}

	/**
	 * <p>
	 * Gets a System property, defaulting to {@code null} if the property cannot be read.
	 * </p>
	 * <p>
	 * If a {@code SecurityException} is caught, the return value is {@code null} and a message is written to
	 * {@code System.err}.
	 * </p>
	 *
	 * @param property
	 *            the system property name
	 * @return the system property value or {@code null} if a security problem occurs
	 */
	private static String getSystemProperty(final String property) {
		try {
			return System.getProperty(property);
		} catch (final SecurityException ex) {
			// we are not allowed to look at this property
			System.err.println("Caught a SecurityException reading the system property '" + property
					+ "'; the SystemUtils property value will default to null.");
			return null;
		}
	}

	/**
	 * Decides if the operating system matches.
	 * <p>
	 * This method is package private instead of private to support unit test invocation.
	 * </p>
	 *
	 * @param osName
	 *            the actual OS name
	 * @param osNamePrefix
	 *            the prefix for the expected OS name
	 * @return true if matches, or false if not or can't determine
	 */
	static boolean isOSNameMatch(final String osName, final String osNamePrefix) {
		if (osName == null) {
			return false;
		}
		return osName.startsWith(osNamePrefix);
	}
}
