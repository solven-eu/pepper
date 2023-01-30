/**
 * The MIT License
 * Copyright (c) 2023 Benoit Lacelle - SOLVEN
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

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Optional;

/**
 * Various helpers related to heap-dumps
 *
 * @author blacelle
 * @see HeapHprofBinWriter
 */
public class PepperHeapDumpHelper {
	protected PepperHeapDumpHelper() {
		// hidden
	}

	/**
	 *
	 * Enable HeapDump on OutOfMemoryError
	 *
	 * -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/disk2/dumps
	 *
	 * jmap <pid>
	 *
	 * jmap -histo <pid>
	 *
	 * jmap -histo -F <pid> > some.file
	 *
	 * jmap -dump:format=b,file=<filename> <pid> -J-Dsun.tools.attach.attachTimeout=<milliseconds>
	 *
	 * @author Benoit Lacelle
	 *
	 */
	interface IPepperHeapAnalysis {
		String PREFIX_OPTION_XX = "-XX:";
		String ACTIVATE_OPTION = "+";
		String SEPARATOR_EQUALS = "=";

		String HEAP_DUMP_ON_OUT_OF_MEMORY_ERROR = "HeapDumpOnOutOfMemoryError";
		String HEAP_DUMP_PATH = "HeapDumpPath";

	}

	public static RuntimeMXBean getRuntimeMXBean() {
		return ManagementFactory.getRuntimeMXBean();
	}

	public static Optional<String> getHeapDumpOnErrorPath() {
		RuntimeMXBean runtimeMxBean = getRuntimeMXBean();
		List<String> arguments = runtimeMxBean.getInputArguments();

		return getHeapDumpOnErrorPath(arguments);
	}

	// TODO Try detecting default files around 'java_pid23227.hprof'
	protected static Optional<String> getHeapDumpOnErrorPath(List<String> arguments) {
		String option = IPepperHeapAnalysis.PREFIX_OPTION_XX + IPepperHeapAnalysis.HEAP_DUMP_PATH
				+ IPepperHeapAnalysis.SEPARATOR_EQUALS;
		return GCInspector.getOptionalArgument(arguments, option);
	}

}
