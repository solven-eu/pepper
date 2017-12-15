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
package cormoran.pepper.jvm;

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.InvocationTargetException;
import java.util.OptionalDouble;

import com.google.common.annotations.Beta;

/**
 * Holds all call to methods/fields not in the Java spec but present in the Oracle jvm
 * 
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings("restriction")
@Beta
public class PepperForOracleJVM {
	protected PepperForOracleJVM() {
		// hidden
	}

	// com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION
	public static final String GARBAGE_COLLECTION_NOTIFICATION = "com.sun.management.gc.notification";

	public static long maxDirectMemory() {
		try {
			// return sun.misc.VM.maxDirectMemory();
			Class<?> VM = Class.forName("sun.misc.VM");

			return ((Long) VM.getMethod("maxDirectMemory").invoke(null)).longValue();
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// JDK9?
			return 0;
		}
	}

	public static OptionalDouble getCpu(OperatingSystemMXBean osMbean) {
		if (osMbean instanceof com.sun.management.OperatingSystemMXBean) {
			double cpu = ((com.sun.management.OperatingSystemMXBean) osMbean).getProcessCpuLoad();

			return OptionalDouble.of(cpu);
		} else {
			return OptionalDouble.empty();
		}
	}

	public static long getThreadAllocatedBytes(ThreadMXBean threadMbean, long l) {
		if (threadMbean instanceof com.sun.management.ThreadMXBean) {
			return ((com.sun.management.ThreadMXBean) threadMbean).getThreadAllocatedBytes(l);
		} else {
			return -1L;
		}
	}

	public static boolean isThreadAllocatedMemorySupported(ThreadMXBean threadMbean) {
		if (threadMbean instanceof com.sun.management.ThreadMXBean) {
			return ((com.sun.management.ThreadMXBean) threadMbean).isThreadAllocatedMemorySupported();
		} else {
			return false;
		}
	}

	public static boolean isThreadAllocatedMemoryEnabled(ThreadMXBean threadMbean) {
		if (threadMbean instanceof com.sun.management.ThreadMXBean) {
			return ((com.sun.management.ThreadMXBean) threadMbean).isThreadAllocatedMemoryEnabled();
		} else {
			return false;
		}
	}

	public static void setThreadAllocatedMemoryEnabled(ThreadMXBean threadMbean, boolean enable) {
		if (threadMbean instanceof com.sun.management.ThreadMXBean) {
			((com.sun.management.ThreadMXBean) threadMbean).setThreadAllocatedMemoryEnabled(enable);
		}
	}
}
