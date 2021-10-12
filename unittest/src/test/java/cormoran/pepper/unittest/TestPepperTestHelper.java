package cormoran.pepper.unittest;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.LoggerFactory;

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
	public void testLogBack() {
		Assertions.assertThat(LoggerFactory.getLogger(getClass()).getClass().getName())
				.isEqualTo("ch.qos.logback.classic.Logger");
	}

	@Test
	public void testEnableLogToInfo() {
		PepperTestHelper.enableLogToInfo(getClass());
	}
}
