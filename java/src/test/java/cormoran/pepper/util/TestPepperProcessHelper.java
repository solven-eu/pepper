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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import cormoran.pepper.agent.PepperAgentHelper;
import cormoran.pepper.memory.IPepperMemoryConstants;

public class TestPepperProcessHelper {
	@Test
	public void testMemoryOnMac() throws IOException {
		String macMemoryOutput = Arrays.asList("mapped file 32.9M 10.7M 32K 0K 0K 0K 0K 139",
				"shared memory 44K 44K 44K 0K 0K 0K 0K 6",
				"=========== ======= ======== ===== ======= ======== ====== ===== =======",
				"TOTAL 2.2G 538.2M 377.3M 0K 0K 16K 0K 845",
				"TOTAL, minus reserved VM space 2.2G 538.2M 377.3M 0K 0K 16K 0K 845",
				"",
				"VIRTUAL RESIDENT DIRTY SWAPPED ALLOCATION BYTES DIRTY+SWAP REGION",
				"MALLOC ZONE SIZE SIZE SIZE SIZE COUNT ALLOCATED FRAG SIZE % FRAG COUNT",
				"=========== ======= ========= ========= ========= ========= ========= ========= ======",
				"DefaultMallocZone_0x10b7b6000 203.0M 148.4M 87.4M 0K 167902 64.5M 22.9M 27% 19",
				"GFXMallocZone_0x10b7e7000 0K 0K 0K 0K 0 0K 0K 0% 0",
				"=========== ======= ========= ========= ========= ========= ========= ========= ======",
				"TOTAL 203.0M 148.4M 87.4M 0K 167902 64.5M 22.9M 27% 19").stream().collect(Collectors.joining("\r"));

		long nbBytes = PepperProcessHelper
				.extractMemory(PepperProcessHelper.OS_MARKER_MAC, new ByteArrayInputStream(macMemoryOutput.getBytes()))
				.getAsLong();
		Assert.assertEquals((long) (538.2D * IPepperMemoryConstants.MB), nbBytes);
	}

	@Test
	public void testMemory_mac_multiplespaces() throws IOException {
		String macMemoryOutput =
				"TOTAL                                1.5G   113.3M    7208K      52K       0K      20K       0K      485 ";

		long nbBytes = PepperProcessHelper
				.extractMemory(PepperProcessHelper.OS_MARKER_MAC, new ByteArrayInputStream(macMemoryOutput.getBytes()))
				.getAsLong();
		Assert.assertEquals((long) (113.3D * IPepperMemoryConstants.MB), nbBytes);
	}

	// Without -X on pmap, we receive only the 'whole' memory, whatever it means
	@Test
	public void testMemoryOnLinux_MissingDashX() throws IOException {
		String macMemoryOutput = Arrays.asList(" total 65512K").stream().collect(Collectors.joining("\n"));

		long nbBytes =
				PepperProcessHelper
						.extractMemory(PepperProcessHelper.OS_MARKER_LINUX,
								new ByteArrayInputStream(macMemoryOutput.getBytes()))
						.getAsLong();
		Assert.assertEquals(65512 * IPepperMemoryConstants.KB, nbBytes);
	}

	@Test
	public void testMemoryOnLinux_WithDashX() throws IOException {
		// RSS is generally the second figure
		String macMemoryOutput =
				Arrays.asList("total kB         4824728  390544  377152").stream().collect(Collectors.joining("\n"));

		long nbBytes =
				PepperProcessHelper
						.extractMemory(PepperProcessHelper.OS_MARKER_LINUX,
								new ByteArrayInputStream(macMemoryOutput.getBytes()))
						.getAsLong();
		Assert.assertEquals(390544 * IPepperMemoryConstants.KB, nbBytes);
	}

	@Test
	public void testMemoryOnWindows() throws IOException {
		// "/fo csv"
		String windowsMemoryOutput = "\"chrome.exe\",\"6740\",\"Console\",\"1\",\"107,940 K\"";

		// "/fo table"
		// String windowsMemoryOutput = "chrome.exe 6740 Console 1 108,760 K";

		long nbBytes =
				PepperProcessHelper
						.extractMemory(PepperProcessHelper.OS_MARKER_WINDOWS,
								new ByteArrayInputStream(windowsMemoryOutput.getBytes()))
						.getAsLong();
		Assert.assertEquals(107940 * IPepperMemoryConstants.KB, nbBytes);
	}

	// French has no comma as thousands separator
	@Test
	public void testMemoryOnWindows_FrenchLocal() throws IOException {
		// "/fo csv"
		String windowsMemoryOutput = "\"chrome.exe\",\"6740\",\"Console\",\"1\",\"78 332 K\"";

		// "/fo table"
		// String windowsMemoryOutput = "chrome.exe 6740 Console 1 108,760 K";

		long nbBytes =
				PepperProcessHelper
						.extractMemory(PepperProcessHelper.OS_MARKER_WINDOWS,
								new ByteArrayInputStream(windowsMemoryOutput.getBytes()))
						.getAsLong();
		Assert.assertEquals(78332 * IPepperMemoryConstants.KB, nbBytes);
	}

	@Test
	public void testMemoryOnWindows_pid_does_not_match() throws IOException {
		String macMemoryOutput = "INFO: No tasks are running which match the specified criteria.";

		OptionalLong nbBytes = PepperProcessHelper.extractMemory(PepperProcessHelper.OS_MARKER_WINDOWS,
				new ByteArrayInputStream(macMemoryOutput.getBytes()));
		Assert.assertFalse(nbBytes.isPresent());
	}

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
