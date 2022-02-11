package eu.solven.pepper.jvm;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.solven.pepper.jvm.PepperHeapDumpHelper;

public class TestPepperHeapDumpHelper {
	@Test
	public void testGetHeapDumpPath_notConfigured() {
		Optional<String> path = PepperHeapDumpHelper.getHeapDumpOnErrorPath(Arrays.asList(""));
		Assert.assertFalse(path.isPresent());
	}

	@Test
	public void testGetHeapDumpPath_relative() {
		Optional<String> path = PepperHeapDumpHelper.getHeapDumpOnErrorPath(Arrays.asList("-XX:HeapDumpPath=path"));
		Assert.assertTrue(path.isPresent());
		Assert.assertEquals("path", path.get());
	}
}
