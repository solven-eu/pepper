package eu.solven.pepper.util;

import org.junit.Assert;
import org.junit.Test;

public class TestPepperEnvHelper {

	@Test
	public void testDetectUnitTest() {
		Assert.assertTrue(PepperEnvHelper.inUnitTest());
	}
}
