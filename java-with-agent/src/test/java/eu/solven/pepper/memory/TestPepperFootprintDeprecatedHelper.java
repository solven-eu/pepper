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
package eu.solven.pepper.memory;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestPepperFootprintDeprecatedHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TestPepperFootprintDeprecatedHelper.class);

	@Test
	public void testIntArrayWeight() {
		Assertions.assertEquals(56, PepperFootprintDeprecatedHelper.getObjectArrayMemory(new int[9]));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testDouble() {
		Assertions.assertEquals(24, PepperFootprintDeprecatedHelper.getDoubleMemory());
	}

	@Test
	public void testStringMemory() {
		long memory = PepperFootprintDeprecatedHelper.getStringMemory("Youpi");
		Assertions.assertEquals(48, memory);
	}

	@Test
	public void testStringMemory_huge() {
		CharSequence existingRef = Mockito.mock(CharSequence.class);

		// Consider a very large String
		Mockito.when(existingRef.length()).thenReturn(Integer.MAX_VALUE);

		long memory = PepperFootprintDeprecatedHelper.getStringMemory(existingRef);
		org.assertj.core.api.Assertions.assertThat(memory).isGreaterThan(Integer.MAX_VALUE + 1L);
	}

	@Disabled("Typically fails with JDK21 due to accessing private fields")
	@Test
	public void testConcurrentHashMap() {
		Map<String, LocalDate> map = new ConcurrentHashMap<>();
		map.put("k", LocalDate.now());

		long memory = PepperFootprintDeprecatedHelper.deepSize(map);
		org.assertj.core.api.Assertions.assertThat(memory).isEqualTo(248L);
	}
}
