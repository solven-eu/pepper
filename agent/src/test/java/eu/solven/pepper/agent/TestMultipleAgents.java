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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

// We used to have issues related to class-loading leading to issues with Library loading
public class TestMultipleAgents {
	@Test
	public void testVMThenAgent() {
		Assumptions.assumeFalse(TestInstrumentAgent.IS_JDK_9, "TODO JDK9");
		Assumptions.assumeFalse(TestInstrumentAgent.IS_JDK_11, "TODO JDK11");
		Assumptions.assumeFalse(TestInstrumentAgent.IS_JDK_12, "TODO JDK12");

		VirtualMachineWithoutToolsJar.getJvmVirtualMachine().get();
		InstrumentationAgent.getInstrumentation().get();
	}

	@Test
	public void testAgentThenVM() {
		Assumptions.assumeFalse(TestInstrumentAgent.IS_JDK_9, "TODO JDK9");
		Assumptions.assumeFalse(TestInstrumentAgent.IS_JDK_11, "TODO JDK11");
		Assumptions.assumeFalse(TestInstrumentAgent.IS_JDK_12, "TODO JDK12");

		InstrumentationAgent.getInstrumentation().get().getClass().getClassLoader();
		VirtualMachineWithoutToolsJar.getJvmVirtualMachine().get();
	}
}
