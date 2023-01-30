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
package net.bull.javamelody;

import org.junit.Assert;
import org.junit.Test;

import eu.solven.pepper.agent.VirtualMachineWithoutToolsJar;
import eu.solven.pepper.memory.histogram.HeapHistogram;
import eu.solven.pepper.memory.histogram.IHeapHistogram;

public class TestHeapHistogram {
	@Test
	public void testHeapHistogramm() throws Exception {
		IHeapHistogram heapHisto = HeapHistogram.createHeapHistogram();

		if (VirtualMachineWithoutToolsJar.IS_JDK_9_OR_LATER) {
			Assert.assertNull(heapHisto);
		} else {
			Assert.assertTrue(heapHisto.getTotalHeapBytes() > 0);
		}
	}
}
