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
package cormoran.pepper.thread;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

import org.junit.Assert;
import org.junit.Test;

public class TestPepperThreadDumper {
	protected final PepperThreadDumper td = new PepperThreadDumper(ManagementFactory.getThreadMXBean());

	@Test
	public void testGetThreadDumpWithMonitor() {
		Assert.assertTrue(td.getThreadDumpAsString(true)
				.contains(this.getClass().getName() + ".testGetThreadDumpWithMonitor(TestPepperThreadDumper.java:"));
	}

	@Test
	public void testGetThreadDumpWithoutMonitor() {
		Assert.assertTrue(td.getThreadDumpAsString(false)
				.contains(this.getClass().getName() + ".testGetThreadDumpWithoutMonitor(TestPepperThreadDumper.java:"));
	}

	@Test
	public void testSmartThreadDump() {
		td.getSmartThreadDumpAsString(false);
	}

	@Test
	public void testHasFooter() throws UnsupportedEncodingException, IOException {
		final ThreadInfo[] threads = td.dumpAllThreads(true, true);
		ThreadInfo firstThreadInfo = threads[0];

		for (ThreadInfo ti : threads) {
			StringWriter writer = new StringWriter();
			td.appendThreadFooter(writer, ti);

			if (td.hasFooter(firstThreadInfo)) {
				Assert.assertFalse(writer.toString().isEmpty());
			} else {
				Assert.assertTrue(writer.toString().isEmpty());
			}
		}

	}
}
