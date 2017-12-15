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
package cormoran.pepper.thread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.management.LockInfo;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

/**
 * A convenience class for getting a thread dump. See com.codahale.metrics.jvm.ThreadDump
 * 
 * @author Benoit Lacelle
 */
@ManagedResource
public class PepperThreadDumper implements IThreadDumper {
	private final ThreadMXBean threadMXBean;

	public PepperThreadDumper() {
		this(ManagementFactory.getThreadMXBean());
	}

	/**
	 * 
	 * @param threadMXBean
	 *            typically ManagementFactory.getThreadMXBean()
	 */
	public PepperThreadDumper(ThreadMXBean threadMXBean) {
		this.threadMXBean = threadMXBean;
	}

	/**
	 * Dumps all of the threads' current information to an output stream.
	 * 
	 * @param out
	 *            an output stream
	 * @throws IOException
	 */
	public void dump(Charset charset, OutputStream out, boolean withMonitorsAndSynchronizers) throws IOException {
		dumpSkeleton(charset, out, withMonitorsAndSynchronizers, (writer, stream) -> {
			stream.forEach(t -> {
				try {
					appendToWritable(writer, t);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		});
	}

	protected ThreadInfo[] dumpAllThreads(boolean withMonitors, boolean withSynchronizers) {
		return threadMXBean.dumpAllThreads(withMonitors, withSynchronizers);
	}

	protected void dumpSkeleton(Charset charset,
			OutputStream out,
			boolean withMonitorsAndSynchronizers,
			BiConsumer<PrintWriter, Stream<ThreadInfo>> threadInfoConsumer) throws IOException {
		final PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, charset));

		printHeader(writer);

		final ThreadInfo[] threads = dumpAllThreads(withMonitorsAndSynchronizers, withMonitorsAndSynchronizers);

		// Group the thread with same state together
		Stream<ThreadInfo> stream =
				Arrays.stream(threads).sorted(Comparator.comparing(ti -> ti.getThreadState().ordinal()));

		threadInfoConsumer.accept(writer, stream);

		println(writer);
		writer.flush();
	}

	protected void printHeader(PrintWriter writer) throws IOException {
		// Header in JVisualVM
		// 2016-07-19 12:50:12 pid@hostname
		RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
		println(writer, LocalDateTime.now() + " " + runtimeMXBean.getName());
		// Full thread dump Java HotSpot(TM) 64-Bit Server VM (25.66-b17 mixed mode):
		println(writer,
				"Full thread dump " + runtimeMXBean.getVmName()
						+ " ("
						+ runtimeMXBean.getVmVersion()
						+ " by "
						+ runtimeMXBean.getVmVendor()
						+ ")");
	}

	protected void appendToWritable(Appendable writer, ThreadInfo t) throws IOException {
		appendThreadHeader(writer, t);
		appendThreadStack(writer, t);
		appendThreadFooter(writer, t);
	}

	/**
	 * Used in smartThreadDump to prevent printing the example footer if it is empty
	 * 
	 * @param t
	 * @return true if the footer is not empty
	 */
	protected boolean hasFooter(ThreadInfo t) {
		return t.getLockedSynchronizers().length >= 1;
	}

	protected void appendThreadFooter(Appendable writer, ThreadInfo t)
			throws UnsupportedEncodingException, IOException {
		final LockInfo[] locks = t.getLockedSynchronizers();
		if (locks.length > 0) {
			printf(writer, "    Locked synchronizers: count = %d%n", locks.length);
			for (LockInfo l : locks) {
				printf(writer, "      - %s%n", l);
			}
			println(writer);
		}
	}

	protected void appendThreadStack(Appendable writer, ThreadInfo t) throws UnsupportedEncodingException, IOException {
		final StackTraceElement[] elements = t.getStackTrace();
		final MonitorInfo[] monitors = t.getLockedMonitors();

		for (int i = 0; i < elements.length; i++) {
			final StackTraceElement element = elements[i];
			printf(writer, "    at %s%n", element);
			for (int j = 1; j < monitors.length; j++) {
				final MonitorInfo monitor = monitors[j];
				if (monitor.getLockedStackDepth() == i) {
					printf(writer, "      - locked %s%n", monitor);
				}
			}
		}
		println(writer);
	}

	protected void appendThreadHeader(Appendable writer, ThreadInfo t)
			throws UnsupportedEncodingException, IOException {
		printf(writer, "%s id=%d state=%s", t.getThreadName(), t.getThreadId(), t.getThreadState());
		final LockInfo lock = t.getLockInfo();
		if (lock != null && t.getThreadState() != Thread.State.BLOCKED) {
			printf(writer, "%n    - waiting on <0x%08x> (a %s)", lock.getIdentityHashCode(), lock.getClassName());
			printf(writer, "%n    - locked <0x%08x> (a %s)", lock.getIdentityHashCode(), lock.getClassName());
		} else if (lock != null && t.getThreadState() == Thread.State.BLOCKED) {
			printf(writer, "%n    - waiting to lock <0x%08x> (a %s)", lock.getIdentityHashCode(), lock.getClassName());
		}

		if (t.isSuspended()) {
			writer.append(" (suspended)");
		}

		if (t.isInNative()) {
			writer.append(" (running in native)");
		}

		println(writer);
		if (t.getLockOwnerName() != null) {
			printf(writer, "     owned by %s id=%d%n", t.getLockOwnerName(), t.getLockOwnerId());
		}
	}

	protected void printf(Appendable writer, String format, Object... parameters)
			throws UnsupportedEncodingException, IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Charset charset = Charset.defaultCharset();
		final PrintWriter printer = new PrintWriter(new OutputStreamWriter(baos, charset));

		printer.printf(format, parameters);
		printer.flush();

		writer.append(baos.toString(charset.name()));
	}

	protected void println(Appendable writer) throws IOException {
		// @see java.io.PrintWriter.PrintWriter(Writer, boolean)
		writer.append(System.getProperty("line.separator"));
	}

	protected void println(Appendable writer, String string) throws IOException {
		writer.append(string);

		println(writer);
	}

	@ManagedOperation
	@Override
	public String getThreadDumpAsString(boolean withMonitorsAndSynchronizers) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		// Do not query monitors and synchronizers are they are not the cause of
		// a FullGC: we prevent not to freeze the JVM collecting these monitors
		try {
			dump(Charset.defaultCharset(), os, withMonitorsAndSynchronizers);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return os.toString();
	}

	@ManagedOperation
	@Override
	public String getSmartThreadDumpAsString(boolean withMonitorsAndSynchronizers) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		Charset charset = Charset.defaultCharset();

		// Do not query monitors and synchronizers are they are not the cause of
		// a FullGC: we prevent not to freeze the JVM collecting these monitors
		try {
			dumpSmart(charset, os, withMonitorsAndSynchronizers);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return os.toString();
	}

	// qfs-common-worker-76 id=905 state=WAITING
	// - waiting on <0x760d8059> (a jsr166e.ForkJoinPool)
	// - locked <0x760d8059> (a jsr166e.ForkJoinPool)
	// at sun.misc.Unsafe.park(Native Method)
	// at jsr166e.ForkJoinPool.awaitWork(ForkJoinPool.java:1758)
	// at jsr166e.ForkJoinPool.scan(ForkJoinPool.java:1696)
	// at jsr166e.ForkJoinPool.runWorker(ForkJoinPool.java:1644)
	// at jsr166e.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:111)

	// http-nio-184.10.18.189-9080-exec-5 id=1019 state=WAITING
	// - waiting on <0x4f603cd5> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	// - locked <0x4f603cd5> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
	// at sun.misc.Unsafe.park(Native Method)
	// at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	// at
	// java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
	// at java.util.concurrent.LinkedBlockingQueue.take(LinkedBlockingQueue.java:442)
	// at org.apache.tomcat.util.threads.TaskQueue.take(TaskQueue.java:103)
	// at org.apache.tomcat.util.threads.TaskQueue.take(TaskQueue.java:31)
	// at java.util.concurrent.ThreadPoolExecutor.getTask(ThreadPoolExecutor.java:1067)
	// at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1127)
	// at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
	// at org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)
	// at java.lang.Thread.run(Thread.java:745)

	public void dumpSmart(Charset charset, OutputStream out, boolean withMonitorsAndSynchronizers) throws IOException {
		dumpSkeleton(charset, out, withMonitorsAndSynchronizers, (writer, stream) -> {
			// Group the thread with same state together
			stream.collect(Collectors.groupingBy(t -> {
				Writer localWriter = new StringWriter();
				try {
					appendThreadStack(localWriter, t);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				return localWriter.toString();
			})).forEach((stack, tis) -> {
				try {
					printThreadGroup(writer, tis);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		});
	}

	protected void printThreadGroup(PrintWriter writer, List<ThreadInfo> tis)
			throws UnsupportedEncodingException, IOException {
		if (tis.size() == 1) {
			ThreadInfo t = tis.get(0);
			// Full stack
			appendThreadHeader(writer, t);

			appendThreadStack(writer, t);
			appendThreadFooter(writer, t);
		} else {
			ThreadInfo t = tis.get(0);

			writer.write("One header amongst ");
			println(writer, Integer.toString(tis.size()));
			appendThreadHeader(writer, t);
			println(writer, "---------------------");

			appendThreadStack(writer, t);

			// Write the footer example only if it is present. Else we would have an overhead wrapping an empty footer:
			// useless
			if (hasFooter(t)) {
				writer.write("One footer amongst ");
				println(writer, Integer.toString(tis.size()));
				appendThreadFooter(writer, t);
				println(writer, "---------------------");

				// Ensure the row after the footer is empty, and not rightaway the next thread
				println(writer, "\r\n");
			}
		}
	}

}
