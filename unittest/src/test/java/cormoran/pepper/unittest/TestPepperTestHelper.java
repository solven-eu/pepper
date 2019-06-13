package cormoran.pepper.unittest;

import org.junit.Test;

public class TestPepperTestHelper {
	@Test
	public void testAssumeInternet() {
		PepperTestHelper.assumeInternetIsAvailable();
	}

	@Test
	public void testDisableLog() {
		PepperTestHelper.disableLog(getClass());
	}

	@Test
	public void testEnableLogToInfo() {
		PepperTestHelper.enableLogToInfo(getClass());
	}
}
