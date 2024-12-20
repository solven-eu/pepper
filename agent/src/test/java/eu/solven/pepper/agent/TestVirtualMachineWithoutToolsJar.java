/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.agent;

import java.net.MalformedURLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestVirtualMachineWithoutToolsJar {
	@Test
	public void testFindVirtualMachineClass() throws ClassNotFoundException, MalformedURLException {
		Assertions.assertEquals("class com.sun.tools.attach.VirtualMachine",
				VirtualMachineWithoutToolsJar.findVirtualMachineClass().get().toString());
	}

	@Test
	public void testIsJRockit() {
		Assertions.assertFalse(VirtualMachineWithoutToolsJar.isJRockit());
	}

	// https://github.com/javamelody/javamelody/blob/master/javamelody-core/src/main/java/net/bull/javamelody/internal/model/VirtualMachine.java#L163
	// @Test
	// public void testHeapHisto() throws Exception {
	//// System.setProperty("jdk.attach.allowAttachSelf", "true");
	//
	// InputStream is = VirtualMachineWithoutToolsJar.heapHisto().get();
	//
	// // We do not use Guava CharSteam as it is marked @Beta
	// String asString = new BufferedReader(new InputStreamReader(is, Charsets.UTF_8)).lines()
	// .parallel()
	// .collect(Collectors.joining("\n"));
	// Assertions.assertNotNull(asString);
	// }

	@Test
	public void testJvmDetach() throws Exception {
		Object jvm = VirtualMachineWithoutToolsJar.getJvmVirtualMachine();
		Assertions.assertNotNull(jvm);
		VirtualMachineWithoutToolsJar.detach();
	}

	@Test
	public void testIsVirtualMachineWithoutToolsJar() {
		VirtualMachineWithoutToolsJar.isVirtualMachineAvailable();
	}

	@Test
	public void testSameVMClass() throws NoSuchFieldException, IllegalAccessException {
		Assertions.assertSame(VirtualMachineWithoutToolsJar.findVirtualMachineClass().get(),
				VirtualMachineWithoutToolsJar.findVirtualMachineClass().get());
	}

	@Test
	public void testGetVMs() {
		VirtualMachineWithoutToolsJar.getJvmVirtualMachines();
	}

	@Disabled("May fail on Homebrew (?) `OpenJDK 64-Bit Server VM Homebrew (build 17.0.11+0, mixed mode, sharing)`")
	@Test
	public void testIsJmapSupported() {
		Assumptions.assumeFalse(TestInstrumentAgent.IS_JDK_12, "TODO JDK12");

		Assertions.assertTrue(VirtualMachineWithoutToolsJar.isJmapSupported(),
				"Java Vendor: " + System.getProperty("java.vendor"));
	}
}
