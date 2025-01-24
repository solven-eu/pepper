/**
 * The MIT License
 * Copyright (c) 2025 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.jvm;

import eu.solven.pepper.system.PepperEnvHelper;
import eu.solven.pepper.thread.IThreadDumper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This can be useful when one want to understand what prevented a nice shutdown, or what processes were interrupted by
 * the shutdown.
 * 
 * @author Benoit Lacelle
 *
 */
@RequiredArgsConstructor
@Slf4j
public class LogThreadsOnShutdown {

	final IThreadDumper pepperThreadDumper;

	// We prefer to submit a closing status when the bean is disposed, as the JVM may never terminate correctly in case
	// of OOM, or Dead/LiveLock
	@Deprecated
	protected void addShutdownHook() {
		Runtime.getRuntime()
				.addShutdownHook(
						new Thread(this::executeDuringShutdown, this.getClass().getSimpleName() + "-ShutdownHook"));
	}

	@SuppressWarnings("PMD.AvoidSynchronizedStatement")
	protected void executeDuringShutdown() {
		// synchronized as this may be called by Bean `.destroy` or by JVM shutdown
		// so we try to prevent these long logs to interleave
		synchronized (this) {
			// On shutdown, do not print too many information as, very often, it is a clean closing (e.g. unit-tests).
			// Still, if something is wrong, it is very beneficial to have core information

			if (PepperEnvHelper.inUnitTest()) {
				log.info("Skip GCInspector closing information as current run is a unit-test");
			} else {
				printSmartThreadDump();
				// printHeapHistogram(GCInspector.HEAP_HISTO_LIMIT_NB_ROWS);
			}
		}
	}

	protected void printSmartThreadDump() {
		String threadDumpAsString = pepperThreadDumper.getSmartThreadDumpAsString(false);

		log.info("Thread Dump: {}", threadDumpAsString);
	}
}
