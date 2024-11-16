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

import java.lang.instrument.Instrumentation;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestInstrumentAgent {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestInstrumentAgent.class);

	// https://stackoverflow.com/questions/2591083/getting-java-version-at-runtime
	public static final boolean IS_JDK_9 = "9".equals(System.getProperty("java.specification.version"));
	public static final boolean IS_JDK_11 = "11".equals(System.getProperty("java.specification.version"));
	public static final boolean IS_JDK_12 = "12".equals(System.getProperty("java.specification.version"));
	public static final boolean IS_JDK_17 = "17".equals(System.getProperty("java.specification.version"));
	public static final boolean IS_JDK_21 = "21".equals(System.getProperty("java.specification.version"));

	@Test
	public void testGetInstrument() {
		Optional<Instrumentation> instrument = PepperAgentLoader.getInstrumentation();

		Assertions.assertTrue(instrument.isPresent());
	}
}
