/**
 * The MIT License
 * Copyright (c) 2014-2024 Benoit Lacelle - SOLVEN
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.ByteBuddyAgent.AttachmentProvider.Accessor;

/**
 * Rely on ByteBuddy to get an {@link Instrumentation}.
 *
 * @author Benoit Lacelle
 *
 */
// https://github.com/ehcache/sizeof/issues/52
public class PepperAgentLoader {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperAgentLoader.class);

	// Ensure we do a single attemps, else we may receive issue like:
	// java.lang.UnsatisfiedLinkError: Native Library ...\jre\bin\attach.dll already loaded in another classloader
	private static final AtomicBoolean HAS_TRIED_LOADING_AGENT = new AtomicBoolean();

	protected static final AtomicReference<Accessor> DEFAULT_ATTEMPT = new AtomicReference<>();

	protected PepperAgentLoader() {
		// hidden
	}

	/**
	 * You should have `-XX:+EnableDynamicAgentLoading`
	 * 
	 * @return
	 */
	public static boolean loadAgent() {

		try {
			ByteBuddyAgent.install();
			return true;
		} catch (IllegalStateException e) {
			LOGGER.warn("ByteBuddyAgent is not installed", e);
			return false;
		}
	}

	/**
	 * It may not be available for many reasons (tools.jar no in the class path, or "Failed to attach to VM and load the
	 * agent: class java.lang.UnsatisfiedLinkError: Native Library /usr/lib/jvm/java-8-oracle/jre/lib/amd64/libattach.so
	 * already loaded in another classloader").
	 * 
	 * You should have `-XX:+EnableDynamicAgentLoading`
	 *
	 * @return an {@link Instrumentation} instance as instantiated by the JVM itself.
	 */
	public static Optional<Instrumentation> getInstrumentation() {
		if (HAS_TRIED_LOADING_AGENT.compareAndSet(false, true)) {
			LOGGER.info("Initializing Agent to provide a reference to {}", Instrumentation.class.getName());

			// Load the agent as first try
			loadAgent();
		}

		try {
			return Optional.of(ByteBuddyAgent.getInstrumentation());
		} catch (IllegalStateException e) {
			LOGGER.warn("ByteBuddyAgent is not installed", e);
			return Optional.empty();
		}
	}

	/**
	 * Also referred by {VirtualMachineWithoutToolsJar#findVirtualMachineClass}
	 */
	static Accessor safeGetDefaultAttempt() {
		if (DEFAULT_ATTEMPT.get() == null) {
			final Accessor singleAttempt = ByteBuddyAgent.AttachmentProvider.DEFAULT.attempt();
			DEFAULT_ATTEMPT.compareAndSet(null, singleAttempt);
		}
		return DEFAULT_ATTEMPT.get();
	}
}
