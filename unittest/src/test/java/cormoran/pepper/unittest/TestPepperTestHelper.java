package cormoran.pepper.unittest;

import org.junit.Test;

public class TestPepperTestHelper {
	@Test
	public void testAssumeInternet() {
		PepperTestHelper.assumeInternetIsAvailable();
	}
}
