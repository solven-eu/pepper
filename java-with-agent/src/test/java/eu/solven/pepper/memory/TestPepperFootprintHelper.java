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
package eu.solven.pepper.memory;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPepperFootprintHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TestPepperFootprintHelper.class);

	@Test
	public void testIntArrayWeight() {
		Assert.assertEquals(56, PepperFootprintHelper.getObjectArrayMemory(new int[9]));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testDouble() {
		Assert.assertEquals(24, PepperFootprintHelper.getDoubleMemory());
	}

	@Test
	public void testStringMemory() {
		long memory = PepperFootprintHelper.getStringMemory("Youpi");
		Assert.assertEquals(48, memory);
	}

	@Test
	public void testStringMemory_huge() {
		CharSequence existingRef = Mockito.mock(CharSequence.class);

		// Consider a very large String
		Mockito.when(existingRef.length()).thenReturn(Integer.MAX_VALUE);

		long memory = PepperFootprintHelper.getStringMemory(existingRef);
		Assertions.assertThat(memory).isGreaterThan(Integer.MAX_VALUE + 1L);
	}
}
