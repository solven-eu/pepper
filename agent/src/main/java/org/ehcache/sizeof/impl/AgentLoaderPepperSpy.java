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
package org.ehcache.sizeof.impl;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

/**
 * Introspect org.ehcache.sizeof.impl.AgentLoader to retrieve a reference to Instrumentation
 * 
 * @author Benoit Lacelle
 *
 */
public class AgentLoaderPepperSpy {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AgentLoaderPepperSpy.class);

	private static final AtomicBoolean HAS_TRIED_LOADING_AGENT = new AtomicBoolean();

	public static boolean loadAgent() {
		return AgentLoader.loadAgent();
	}

	public static Optional<Instrumentation> getInstrumentation()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		// ByteBuddyAgent.install();

		if (HAS_TRIED_LOADING_AGENT.compareAndSet(false, true)) {
			LOGGER.info("Initializing Agent to provide a reference to {}", Instrumentation.class.getName());

			// Load the agent as first try
			AgentLoaderPepperSpy.loadAgent();
		}
		Field f = AgentLoader.class.getDeclaredField("instrumentation");

		f.setAccessible(true);

		return Optional.fromNullable((Instrumentation) f.get(null));
	}
}
