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

	public static void assumeInternetIsAvailable() {
		try {
			new URL("https://google.com").openConnection();
		} catch (RuntimeException | IOException e) {
			Assume.assumeNoException("Internet is not available: can not tet Xambox API", e);
		}
	}
}
