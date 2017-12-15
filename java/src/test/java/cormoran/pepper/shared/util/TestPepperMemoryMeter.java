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
package cormoran.pepper.shared.util;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.agent.InstrumentationAgent;
import cormoran.pepper.memory.PepperMemoryHelper;

public class TestPepperMemoryMeter {

	public static final boolean IS_JDK_9 = "9".equals(System.getProperty("java.specification.version"));

	@BeforeClass
	public static void assumeAgentLoaded() {
		Assume.assumeTrue("We failed retrieving an Instrumentation",
				InstrumentationAgent.getInstrumentation().isPresent());
	}

	@Test
	public void testStringWeight() {
		if (IS_JDK_9) {
			// Lower in JDK9: good!
			Assert.assertEquals(48, PepperMemoryHelper.deepSize("Youpi"));
		} else {
			Assert.assertEquals(56, PepperMemoryHelper.deepSize("Youpi"));
		}

		if (false) {
			// Adding a single char add 2 bytes. As the JVM packes by block of 8 bytes, it may not be enough to grow the
			// estimated size
			Assert.assertTrue(PepperMemoryHelper.deepSize("Youpi") < PepperMemoryHelper.deepSize("Youpi+"));
		}
		// Adding 4 chars leads to adding 8 bytes: the actual JVM size is increased
		Assert.assertTrue(PepperMemoryHelper.deepSize("Youpi") < PepperMemoryHelper.deepSize("Youpi1234"));
	}

	@Test
	public void testImmutableMapWeight() {
		Assertions.assertThat(PepperMemoryHelper.deepSize(ImmutableMap.of("key", "Value"))).isBetween(100L, 250L);
	}

	@Test
	public void testRecursiveMapWeight() {
		// Consider a Map referencing itself
		Map<String, Object> recursiveMap = new HashMap<>();
		recursiveMap.put("myself", recursiveMap);

		long deepSize = PepperMemoryHelper.deepSize(recursiveMap);
		if (IS_JDK_9) {
			// Lower in JDK9: good!
			Assert.assertEquals(208, deepSize);
		} else {
			Assert.assertEquals(216, deepSize);
		}

		// Change the Map so it does not reference itself: the object graph should have the same size
		Map<String, Object> withoutRecursivity = new HashMap<>();
		withoutRecursivity.put("myself", null);

		long notdeepSize = PepperMemoryHelper.deepSize(withoutRecursivity);
		Assert.assertEquals(notdeepSize, deepSize);
	}

	@Test
	public void testArrayWeight() {
		Object[] array = new Object[2];

		long sizeEmpty = PepperMemoryHelper.deepSize(array);
		Assert.assertEquals(24, sizeEmpty);

		array[0] = LocalDate.now();
		array[1] = LocalDate.now();

		long sizeFull = PepperMemoryHelper.deepSize(array);

		// We have different memory consumptions depending on the env/jdk/run
		// With JodaTime, the reported memory was bigger. It seems strange
		// Assertions.assertThat(sizeFull).isBetween(900L, 9200L);

		Assertions.assertThat(sizeFull).isEqualTo(72L);

		Assert.assertTrue(sizeFull > sizeEmpty);
	}
}
