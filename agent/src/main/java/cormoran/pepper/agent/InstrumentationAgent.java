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
package cormoran.pepper.agent;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.ByteBuddyAgent.AttachmentProvider;
import net.bytebuddy.agent.ByteBuddyAgent.AttachmentProvider.Accessor;

/**
 * The entry point for the instrumentation agent.
 * 
 * Either the premain method is called by adding the JVM arg -javaagent. Or one could call the
 * {@link InstrumentationAgent#getInstrumentation()} method. Anyway, tools.jar will have to be present at Runtime
 * 
 * @author Benoit Lacelle
 * 
 */
public class InstrumentationAgent {

	protected static final Logger LOGGER = LoggerFactory.getLogger(InstrumentationAgent.class);

	protected static final AtomicBoolean BYTE_BUDDY_IS_INSTALLED = new AtomicBoolean();

	// Ensure we do a single attemps, else we may receive issue like:
	// java.lang.UnsatisfiedLinkError: Native Library ...\jre\bin\attach.dll already loaded in another classloader
	protected static final AtomicReference<Accessor> DEFAULT_ATTEMPT = new AtomicReference<Accessor>();

	/**
	 * It may not be available for many reasons (tools.jar no in the class path, or "Failed to attach to VM and load the
	 * agent: class java.lang.UnsatisfiedLinkError: Native Library /usr/lib/jvm/java-8-oracle/jre/lib/amd64/libattach.so
	 * already loaded in another classloader")
	 * 
	 * @return an {@link Instrumentation} instance as instantiated by the JVM itself.
	 */
	// Rely on Guava Optional to enable compatibility with JDK6
	public static synchronized Optional<Instrumentation> getInstrumentation() {
		try {
			if (BYTE_BUDDY_IS_INSTALLED.get()) {
				return Optional.of(ByteBuddyAgent.getInstrumentation());
			} else {
				BYTE_BUDDY_IS_INSTALLED.set(true);

				final Accessor singleAttempt = safeGetDefaultAttempt();
				if (singleAttempt.isAvailable()) {
					return Optional.of(ByteBuddyAgent.install(new AttachmentProvider() {

						@Override
						public Accessor attempt() {
							return singleAttempt;
						}

					}));
				} else {
					return Optional.absent();
				}
			}
		} catch (Throwable e) {
			LOGGER.warn("Issue while getting instrumentation", e);
			return Optional.absent();
		}
	}

	/**
	 * Also referred by {cormoran.pepper.agent.VirtualMachineWithoutToolsJar#findVirtualMachineClass()}
	 */
	synchronized static Accessor safeGetDefaultAttempt() {
		if (DEFAULT_ATTEMPT.get() == null) {
			final Accessor singleAttempt = ByteBuddyAgent.AttachmentProvider.DEFAULT.attempt();
			DEFAULT_ATTEMPT.compareAndSet(null, singleAttempt);
		}
		return DEFAULT_ATTEMPT.get();
	}
}