package eu.solven.pepper.util;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import eu.solven.pepper.agent.PepperAgentHelper;

// see TestPepperProcessHelper
public class TestWithAgentPepperProcessHelper {

	/**
	 * Enable to check the behavior on any system
	 *
	 * @throws IOException
	 */
	@Test
	public void testMemoryOnCurrentSystem() throws IOException {
		long currentProcessPID = Long.parseLong(PepperAgentHelper.getPIDForAgent());
		long nbBytes = PepperProcessHelper.getProcessResidentMemory(currentProcessPID).getAsLong();
		Assert.assertTrue(nbBytes > 0);
	}
}
