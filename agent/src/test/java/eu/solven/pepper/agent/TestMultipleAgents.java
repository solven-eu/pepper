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

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.google.common.base.Optional;

// We used to have issues related to class-loading leading to issues with Library loading
public class TestMultipleAgents {
	@Test
	public void testVMThenAgent() {
		Assumptions.assumeTrue(VirtualMachineWithoutToolsJar.isAllowAttachSelf());

		VirtualMachineWithoutToolsJar.getJvmVirtualMachine().get();
		InstrumentationAgent.getInstrumentation().get();
	}

	@Test
	public void testAgentThenVM() {
		Assumptions.assumeTrue(VirtualMachineWithoutToolsJar.isAllowAttachSelf());

		InstrumentationAgent.getInstrumentation().get().getClass().getClassLoader();

		Optional<Object> optVm = VirtualMachineWithoutToolsJar.getJvmVirtualMachine();
		Assertions.assertThat(optVm.isPresent()).isEqualTo(VirtualMachineWithoutToolsJar.isAllowAttachSelf());
	}
}
