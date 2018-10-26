package cormoran.pepper.jvm;

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

	protected static Optional<String> getHeapDumpOnErrorPath(List<String> arguments) {
		String option = IPepperHeapAnalysis.PREFIX_OPTION_XX + IPepperHeapAnalysis.HEAP_DUMP_PATH
				+ IPepperHeapAnalysis.SEPARATOR_EQUALS;
		return GCInspector.getOptionalArgument(arguments, option);
	}

}
