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
package cormoran.pepper.buffer;

import java.io.IOException;
import java.nio.IntBuffer;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestPepperBufferHelper {
	@Before
	public void resetConstants() {
		PepperBufferHelper.forceNoSpaceDisk = false;
		PepperBufferHelper.forceNoHeap = false;
	}

	@Test
	public void testBuffer_small() throws IOException {
		int nbInts = 123;
		try (CloseableIntBuffer buffer = PepperBufferHelper.makeIntBuffer(nbInts)) {

			IntBuffer intBuffer = buffer.asIntBuffer();
			Assertions.assertThat(intBuffer.getClass().getSimpleName()).contains("Direct");

			// By default, we are filled with 0
			Assert.assertEquals(0, intBuffer.get(0));
			Assert.assertEquals(0, intBuffer.get(nbInts - 1));
		}
	}

	@Test
	public void testBuffer_noDiskButHeap() throws IOException {
		PepperBufferHelper.forceNoSpaceDisk = true;

		try (CloseableIntBuffer buffer = PepperBufferHelper.makeIntBuffer(123)) {
			// By default, we are filled with 0
			IntBuffer intBuffer = buffer.asIntBuffer();

			Assertions.assertThat(intBuffer.getClass().getSimpleName()).contains("Heap");

			Assert.assertEquals(0, intBuffer.get(0));
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testBuffer_noDiskNoHeap() throws IOException {
		PepperBufferHelper.forceNoSpaceDisk = true;
		PepperBufferHelper.forceNoHeap = true;

		PepperBufferHelper.makeIntBuffer(123);
	}
}
