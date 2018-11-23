package cormoran.pepper.unittest;

import java.io.IOException;
import java.net.URL;

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
	 */
	public static void assumeInternetIsAvailable() {
		try {
			new URL("https://google.com").openConnection();
		} catch (RuntimeException | IOException e) {
			Assume.assumeNoException("Internet is not available", e);
		}
	}
}
