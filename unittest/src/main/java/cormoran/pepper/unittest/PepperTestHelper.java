package cormoran.pepper.unittest;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Assume;

/**
 * Helpers for Unit-tests
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperTestHelper {
	protected PepperTestHelper() {
		// hidden
	}

	/**
	 * This will check internet (global) is available by checking the connectivity to a global resource expected to be
	 * always UP (e.g. google.com)
	 * 
	 * @return true if the internet is available
	 */
	public static boolean assumeInternetIsAvailable() {
		try {
			URLConnection connection = new URL("https://google.com").openConnection();
			Assume.assumeNotNull(connection);
			// We check some data from the connection as we may receive a not connection connection
			Assume.assumeNotNull(connection.getContentType());

			return true;
		} catch (RuntimeException | IOException e) {
			Assume.assumeNoException("Internet is not available", e);
			return false;
		}
	}
}
