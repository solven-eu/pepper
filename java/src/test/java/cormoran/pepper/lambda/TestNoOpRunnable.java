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
package cormoran.pepper.lambda;

import java.io.IOException;
import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;

import cormoran.pepper.io.PepperSerializationHelper;
import cormoran.pepper.lamda.NoOpRunnable;

public class TestNoOpRunnable {
	@Test
	public void testRunNoOp() throws IOException {
		NoOpRunnable noOpRunnable = new NoOpRunnable();
		noOpRunnable.run();

		Assert.assertTrue(noOpRunnable instanceof Serializable);

		Assert.assertEquals("rO0ABXNyACJjb3Jtb3Jhbi5wZXBwZXIubGFtZGEuTm9PcFJ1bm5hYmxlyv5gVq2UgTgCAAB4cA==",
				PepperSerializationHelper.toString(noOpRunnable));
	}
}
