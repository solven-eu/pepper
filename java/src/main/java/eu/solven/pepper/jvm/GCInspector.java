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
package eu.solven.pepper.jvm;

import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.AtomicLongMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import eu.solven.pepper.jmx.PepperJMXHelper;
import eu.solven.pepper.logging.PepperLogHelper;
import eu.solven.pepper.memory.PepperMemoryHelper;
import eu.solven.pepper.thread.IThreadDumper;
import eu.solven.pepper.thread.PepperThreadDumper;
import eu.solven.pepper.util.PepperEnvHelper;
import eu.solven.pepper.util.PepperTimeHelper;

/**
 *
 * This class registers itself as listener on GC events. It will produce a thread-dump when long GC pauses happens
 *
 * @author Benoit Lacelle
 * @since Oracle Java 7 update 4 JVM
 */
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals",
		"PMD.GodClass",
		"PMD.ConsecutiveLiteralAppends",
		"PMD.ExcessiveClassLength",
		"PMD.CouplingBetweenObjects" })
@ManagedResource
public class GCInspector implements NotificationListener, InitializingBean, DisposableBean, IGCInspector {
	protected static final Logger LOGGER = LoggerFactory.getLogger(GCInspector.class);

	/**
	 * Remember the length of the first GC, which is used as heuristic to know if GC times are expressed in ms or ns
	 */
	protected final AtomicLong firstGcNotZero = new AtomicLong();

	/**
	 * If the first GC pause is bigger than this size, we expect GC pauses are expressed in NS
	 */
	protected static final long MAX_FIRST_PAUSE_MS = TimeUnit.SECONDS.toMillis(5);

	/**
	 * @deprecated One should use TimeUnit.NANOSECONDS.toMillis(timeInNs)
	 */
	@Deprecated
	public static final long NS_TO_MS = TimeUnit.MILLISECONDS.toNanos(1);

	public static final float BETWEEN_MINUS_ONE_AND_ZERO = -0.5F;

	/**
	 * Any time a GC lasts longer than this duration, we log details about the GC
	 */
	public static final long DEFAULT_GCDURATION_MILLIS_INFO_LOG = 200;
	protected long gcDurationMillisForInfoLog = DEFAULT_GCDURATION_MILLIS_INFO_LOG;

	/**
	 * If a MarkSweep GC lasts more than this duration, we log a ThreadDump
	 */
	public static final long DEFAULT_FULLGC_MILLIS_THREADDUMP = TimeUnit.SECONDS.toMillis(10);
	protected long minFullGCMillisForThreadDump = DEFAULT_FULLGC_MILLIS_THREADDUMP;

	public static final long DEFAULT_FULLGC_MILLIS_HEAPHISTOGRAM = TimeUnit.SECONDS.toMillis(10);
	protected long minFullGCForHeapHistogram = DEFAULT_FULLGC_MILLIS_HEAPHISTOGRAM;

	public static final long DEFAULT_MAX_HEAP_GB_HEAPHISTOGRAM = 20;
	protected long maxHeapGbForHeapHistogram = DEFAULT_MAX_HEAP_GB_HEAPHISTOGRAM;

	private static final String XSS = "-Xss";
	private static final String XX_THREADSTACKSIZE = "-XX:ThreadStackSize=";

	// https://stackoverflow.com/questions/50232400/xxexitonoutofmemoryerror-ignored-on-java-lang-outofmemoryerror-direct-buffe
	private static final String XX_EXITONOUTOFMEMORYERROR = "-XX:+ExitOnOutOfMemoryError";

	protected static final MBeanServer MBEAN_SERVER = ManagementFactory.getPlatformMBeanServer();
	protected static final OperatingSystemMXBean OS_MBEAN = ManagementFactory.getOperatingSystemMXBean();
	protected static final ThreadMXBean THREAD_MBEAN = ManagementFactory.getThreadMXBean();

	protected static final MemoryMXBean MEMORY_MBEAN = ManagementFactory.getMemoryMXBean();
	protected static final List<MemoryPoolMXBean> MEMORY_POOLS_MBEAN = ManagementFactory.getMemoryPoolMXBeans();

	protected AtomicReference<LocalDateTime> latestThreadDump = new AtomicReference<>();

	private final AtomicReference<Map<? extends String, ? extends Long>> allocatedHeapReference =
			new AtomicReference<>(Collections.emptyMap());

	private final AtomicReference<Map<? extends String, ? extends Long>> heapGCNotifReference =
			new AtomicReference<>(Collections.emptyMap());

	private static final double HEAP_ALERT_THRESHOLD = 0.9D;
	private static final int HEAP_ALERT_PERIOD_IN_MINUTES = 15;
	private final AtomicReference<LocalDateTime> overHeapThresholdSince = new AtomicReference<>();

	protected final IThreadDumper pepperThreadDumper;

	protected final AtomicLong targetMaxTotalMemory = new AtomicLong(Long.MAX_VALUE);

	// see also: http://www.fasterj.com/articles/oraclecollectors1.shtml
	public static final Set<String> FULL_GC_NAMES = ImmutableSet.of(
			// enable with -XX:+UseParallelOldGC
			"PS MarkSweep",
			// enable with -XX:+UseG1GC
			"G1 Old Generation",
			// enable with -XX:+UseSerialGC
			"MarkSweepCompact",
			// enable with -XX:+UseConcMarkSweepGC
			"ConcurrentMarkSweep");

	public static final Set<String> NOT_FULL_GC_NAMES = ImmutableSet.of(
			// enable with -XX:+UseG1GC
			"G1 Young Generation",
			// enable with -XX:+UseParallelGC
			"PS Scavenge",
			// enable with -XX:+UseSerialGC
			"Copy",
			// enable with -XX:+UseParNewGC
			"ParNew");

	public static final Set<String> REPORTED_UNKNOWN_GC_NAMES = Sets.newConcurrentHashSet();

	/**
	 * Print the heap histogram only up to given % of total heap
	 */
	private static final int HEAP_HISTO_LIMIT_NB_ROWS = 20;

	public GCInspector(IThreadDumper apexThreadDumper) {
		this.pepperThreadDumper = apexThreadDumper;
	}

	/**
	 * Default constructor with nice default
	 */
	public GCInspector() {
		this(new PepperThreadDumper(ManagementFactory.getThreadMXBean()));
	}

	// This is not a ManagedAttribute as the type (String) is not compatible with the getter
	@ManagedOperation
	public void setTargetMaxTotalMemory(String targetMax) {
		long asLong = PepperMemoryHelper.memoryAsLong(targetMax);

		targetMaxTotalMemory.set(asLong);
	}

	@ManagedAttribute
	public String getTargetMaxTotalMemory() {
		return PepperMemoryHelper.memoryAsString(targetMaxTotalMemory.get());
	}

	@Override
	public void afterPropertiesSet() throws MalformedObjectNameException, InstanceNotFoundException {
		ObjectName gcName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");

		// Register this as listener for any GC event
		for (ObjectName name : MBEAN_SERVER.queryNames(gcName, null)) {
			MBEAN_SERVER.addNotificationListener(name, this, null, null);
		}

	}

	// We prefer to submit a closing status when the bean is disposed, as the JVM may never terminate correctly in case
	// of OOM, or Dead/LiveLock
	@Deprecated
	protected void addShutdownHook() {
		Runtime.getRuntime()
				.addShutdownHook(
						new Thread(this::executeDuringShutdown, this.getClass().getSimpleName() + "-ShutdownHook"));
	}

	protected void executeDuringShutdown() {
		// On shutdown, do not print too many information as, very often, it is a clean closing (e.g. unit-tests).
		// Still, if something is wrong, it is very beneficial to have core information

		if (inUnitTest()) {
			LOGGER.info("Skip GCInspector closing information as current run is a unit-test");
		} else {
			printSmartThreadDump();
			printHeapHistogram(HEAP_HISTO_LIMIT_NB_ROWS);
		}
	}

	/**
	 * 
	 * @deprecated Use {@link PepperEnvHelper#inUnitTest()}
	 */
	@Deprecated
	public static boolean inUnitTest() {
		return PepperEnvHelper.inUnitTest();
	}

	/**
	 * Clean the MBean registration. Else, unit-test would register several GCInexpector (one for each Context loaded)
	 */
	@Override
	public void destroy() throws Exception {
		removeNotificationListener();

		executeDuringShutdown();
	}

	protected void removeNotificationListener() throws MalformedObjectNameException, ListenerNotFoundException {
		ObjectName gcName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");

		// Register this as listener for any GC event
		for (ObjectName name : MBEAN_SERVER.queryNames(gcName, null)) {
			try {
				MBEAN_SERVER.removeNotificationListener(name, this);
			} catch (InstanceNotFoundException | RuntimeException e) {
				// Log in debug as no big-deal to fail disconnecting beans
				LOGGER.debug("Failure for " + name, e);
			}
		}
	}

	@Override
	public void handleNotification(Notification notification, Object handback) {
		String type = notification.getType();
		if (PepperForOracleJVM.GARBAGE_COLLECTION_NOTIFICATION.equals(type)) {
			// retrieve the garbage collection notification information
			CompositeData cd = (CompositeData) notification.getUserData();
			PepperGarbageCollectionNotificationInfo info = PepperGarbageCollectionNotificationInfo.from(cd);

			doLog(info);
		}
	}

	@Subscribe
	@AllowConcurrentEvents
	public void onThrowable(Throwable t) {
		Throwables.getCausalChain(t)
				.stream()
				.filter(OutOfMemoryError.class::isInstance)
				.findAny()
				.ifPresent(this::onOutOfMemoryError);
	}

	@SuppressWarnings({ "PMD.DoNotCallSystemExit", "PMD.DoNotTerminateVM" })
	@SuppressFBWarnings("DM_EXIT")
	private void onOutOfMemoryError(Throwable oom) {
		LOGGER.error("We encountered an {}", oom.getClass());

		// https://stackoverflow.com/questions/48147092/how-to-listen-for-outofmemoryerror-and-exit-the-jvm
		// java.nio.Bits.reserveMemory(long, int)
		if (isExitOnOutOfMemoryError() && "Direct buffer memory".equals(oom.getMessage())) {
			LOGGER.error(
					"We force System.exit as we encountered an OutOfMemory, probably dues to java.nio.Bits.reserveMemory(...)");
			System.exit(1);
		}
	}

	private boolean isExitOnOutOfMemoryError() {
		return getOptionalArgument(getVMInputArguments(), XX_EXITONOUTOFMEMORYERROR).isPresent();
	}

	protected long computeDurationMs(IPepperGarbageCollectionNotificationInfo info) {

		long rawDuration = info.getGcDuration();

		if (rawDuration == 0) {
			return 0;
		}

		if (firstGcNotZero.compareAndSet(0, rawDuration)) {
			// We expect first GC to be less than 5 seconds. If it is
			if (firstGcNotZero.get() > MAX_FIRST_PAUSE_MS) {
				// Duration are supposed to be expressed in ms. The first pause
				// is supposed to be short. If it is very long, it probably
				// means it is expressed in ns
				// http://www.docjar.com/docs/api/com/sun/management/GcInfo.html#getDuration
				LOGGER.warn("We guess GC times are expressed in ns instead of ms since first pause lasted {}?s",
						firstGcNotZero.get());
			} else {
				LOGGER.info("We guess GC times are expressed in ms as first GC pause lasted {}?s",
						firstGcNotZero.get());
			}
		}

		if (firstGcNotZero.get() > MAX_FIRST_PAUSE_MS) {
			return TimeUnit.NANOSECONDS.toMillis(rawDuration);
		} else {
			return rawDuration;
		}
	}

	protected String makeGCMessage(IPepperGarbageCollectionNotificationInfo info) {
		long duration = computeDurationMs(info);

		// String gctype = info.getGcAction();
		// if ("end of minor GC".equals(gctype)) {
		// gctype = "Young Gen GC";
		// } else if ("end of major GC".equals(gctype)) {
		// gctype = "Old Gen GC";
		// }

		StringBuilder sb = new StringBuilder();

		appendCPU(sb);

		appendCurrentGCDuration(sb, info, duration);

		long totalAfterMinusbefore = 0L;

		NavigableSet<String> keys = getSortedGCKeys(info);

		long totalHeapUsedBefore = 0L;
		long totalHeapUsedAfter = 0L;
		for (String key : keys) {
			MemoryUsage before = info.getMemoryUsageBeforeGc().get(key);
			MemoryUsage after = info.getMemoryUsageAfterGc().get(key);
			if (after == null) {
				LOGGER.debug("No .getMemoryUsageAfterGc for {}", key);
			} else {
				long afterUsed = after.getUsed();
				long beforeUsed = before.getUsed();

				totalHeapUsedBefore += beforeUsed;
				totalHeapUsedAfter += afterUsed;

				if (afterUsed != beforeUsed) {
					long afterMinusBefore = afterUsed - beforeUsed;
					totalAfterMinusbefore += afterMinusBefore;
				}

				appendMovedMemory(sb, key, before, after);

				if (!key.equals(keys.last())) {
					// Do NTO add the separator for the last entry
					sb.append("; ");
				}
			}
		}

		// TODO: Some of this is actually non-heap
		appendHeap(sb, totalHeapUsedAfter);

		if (totalAfterMinusbefore != 0) {
			appendDetailsAboutMove(sb, totalAfterMinusbefore, totalHeapUsedBefore);
		}

		appendDirectMemoryAndThreads(sb);

		return sb.toString();
	}

	protected void appendMovedMemory(StringBuilder sb, String key, MemoryUsage before, MemoryUsage after) {

		long beforeUsed = before.getUsed();
		long beforeCommited = before.getCommitted();

		long afterUsed = after.getUsed();
		long afterCommited = after.getCommitted();

		if (after.getUsed() == before.getUsed()) {
			sb.append(key).append(" == ");
			appendPercentage(sb, afterUsed, afterCommited);
			sb.append(" (");
			appendSize(sb, afterUsed);
			sb.append(')');
		} else {
			sb.append(key).append(' ');
			appendPercentage(sb, beforeUsed, beforeCommited);
			sb.append("->");
			appendPercentage(sb, afterUsed, afterCommited);

			sb.append(" (");

			appendSize(sb, beforeUsed);

			if (after.getUsed() > before.getUsed()) {
				sb.append('+');
			} else {
				LOGGER.trace("A negative number already provides a '-' sign");
			}

			long afterMinusBefore = afterUsed - beforeUsed;
			appendSize(sb, afterMinusBefore);

			sb.append("->");
			appendSize(sb, afterUsed);

			sb.append(')');
		}
	}

	protected NavigableSet<String> getSortedGCKeys(IPepperGarbageCollectionNotificationInfo info) {
		// Sort by lexicographical order
		return new TreeSet<>(info.getMemoryUsageBeforeGc().keySet());
	}

	protected void appendCurrentGCDuration(StringBuilder sb,
			IPepperGarbageCollectionNotificationInfo info,
			long duration) {
		sb.append(info.getGcName()).append(" lasted ").append(PepperLogHelper.humanDuration(duration)).append(". ");
	}

	protected void appendHeap(StringBuilder sb, long totalHeapUsedAfter) {
		long maxHeap = MEMORY_MBEAN.getHeapMemoryUsage().getMax();

		sb.append(" - Heap: fromGC=");
		appendSize(sb, totalHeapUsedAfter);
		sb.append(" - ").append(PepperLogHelper.getNicePercentage(totalHeapUsedAfter, maxHeap));

		long heapAsTotal = MEMORY_MBEAN.getHeapMemoryUsage().getUsed();
		sb.append(" - fromMX=");
		appendSize(sb, heapAsTotal);
		sb.append(" - ").append(PepperLogHelper.getNicePercentage(heapAsTotal, maxHeap));

	}

	protected void appendNonHeap(StringBuilder sb, long nonHeapUsedAfter) {
		sb.append(" - Non-Heap:");
		appendSize(sb, nonHeapUsedAfter);
	}

	@SuppressWarnings("PMD.ConsecutiveAppendsShouldReuse")
	protected void appendDetailsAboutMove(StringBuilder sb, long totalAfterMinusbefore, long totalHeapUsedBefore) {
		sb.append('=');
		appendSize(sb, totalHeapUsedBefore);

		if (totalAfterMinusbefore < 0) {
			appendSize(sb, totalAfterMinusbefore);
			sb.append(" garbage collected");
		}

		// Add the name of the Thread producing the maximum amount of memory
		{
			Map<? extends String, ? extends Long> immutableCurrentHeapByThread = getThreadNameToAllocatedHeap();
			Map<? extends String, ? extends Long> previousStatus = getAndSetByThreadRef(immutableCurrentHeapByThread);

			AtomicLongMap<String> currentHeap = AtomicLongMap.create(immutableCurrentHeapByThread);

			// Compute the difference between previous status and current status
			adjustWithReference(currentHeap, previousStatus);

			if (!currentHeap.isEmpty()) {
				long sumPrevious = previousStatus.values().stream().mapToLong(Long::longValue).sum();
				long sumCurrent = immutableCurrentHeapByThread.values().stream().mapToLong(Long::longValue).sum();

				if (sumCurrent > sumPrevious) {
					long transientlyGenerated = sumCurrent - sumPrevious;

					sb.append(" after allocating ");
					appendSize(sb, transientlyGenerated);
					sb.append(" through all threads");
				}

				// Sort from big memory to small memory
				Map<String, Long> valueOrdered = PepperJMXHelper.convertToJMXValueOrderedMap(currentHeap.asMap(), true);

				// currentHeap is not empty then valueOrdered is not empty
				assert !valueOrdered.isEmpty();
				Entry<String, Long> maxEntry = valueOrdered.entrySet().iterator().next();

				if (maxEntry.getValue() > 0) {
					sb.append(" including ");
					appendSize(sb, maxEntry.getValue());
					sb.append(" from ");
					sb.append(maxEntry.getKey());

					AtomicLongMap<String> groupBy = groupThreadNames(currentHeap.asMap());
					Map<String, Long> groupByValueOrdered =
							PepperJMXHelper.convertToJMXValueOrderedMap(groupBy.asMap(), true);

					Entry<String, Long> groupByMaxEntry = groupByValueOrdered.entrySet().iterator().next();

					// Report the biggest group only if it a different thread than the single biggest thread
					if (groupByMaxEntry.getValue() > 0 && !groupByMaxEntry.getKey().equals(maxEntry.getKey())) {
						sb.append(" and ");
						appendSize(sb, groupByMaxEntry.getValue());
						sb.append(" from ");
						sb.append(groupByMaxEntry.getKey());
					}
				} else {
					LOGGER.debug("We have only decreasing in {}", valueOrdered);
				}
			}
		}
	}

	protected Map<? extends String, ? extends Long> getAndSetByThreadRef(
			Map<? extends String, ? extends Long> immutableCurrentHeapByThread) {
		return heapGCNotifReference.getAndSet(immutableCurrentHeapByThread);
	}

	@SuppressWarnings("PMD.MagicNumber")
	protected String getCurrentMemoryStatusMessage() {
		StringBuilder sb = new StringBuilder(32);

		appendCPU(sb);

		long totalHeapUsedAfter = 0L;
		long totalNonHeapUsedAfter = 0L;
		for (MemoryPoolMXBean key : MEMORY_POOLS_MBEAN) {
			MemoryUsage after = key.getUsage();

			if (after != null) {
				long afterUsed = after.getUsed();
				long afterCommited = after.getCommitted();

				if (key.getType() == MemoryType.HEAP) {
					totalHeapUsedAfter += afterUsed;
				} else {
					totalNonHeapUsedAfter += afterUsed;
				}

				sb.append(key.getName()).append(" ==");
				appendSize(sb, afterUsed);
				sb.append(" == ");
				appendPercentage(sb, afterUsed, afterCommited);

				sb.append("; ");
			}
		}

		appendHeap(sb, totalHeapUsedAfter);
		appendNonHeap(sb, totalNonHeapUsedAfter);

		long directAndThreadBytes = appendDirectMemoryAndThreads(sb);

		long totalMemoryFootprint = totalHeapUsedAfter + directAndThreadBytes;

		sb.append(" - GrandTotal:");
		appendSize(sb, totalMemoryFootprint);

		return sb.toString();
	}

	protected void appendCPU(StringBuilder sb) {
		// Add information about CPU consumption
		OptionalDouble optCpu = PepperForOracleJVM.getCpu(OS_MBEAN);

		optCpu.ifPresent(cpu -> {
			// -1 == No CPU info
			if (cpu >= BETWEEN_MINUS_ONE_AND_ZERO) {
				sb.append("CPU=");
				appendPercentage(sb, (long) (cpu * PepperLogHelper.THOUSAND), PepperLogHelper.THOUSAND);
				sb.append(" - ");
			}
		});
	}

	protected long appendDirectMemoryAndThreads(StringBuilder sb) {
		AtomicLong additionalMemory = new AtomicLong();

		// Add information about DirectMemory
		{
			directMemoryStatus().ifPresent(directMemoryBean -> {
				sb.append("; ").append("DirectMemory").append(": ");
				long directMemoryUsed = directMemoryBean.getMemoryUsed();
				additionalMemory.addAndGet(directMemoryUsed);
				appendSize(sb, directMemoryUsed);
				sb.append(" over max=");
				long maxDirectMemory = PepperForOracleJVM.maxDirectMemory();
				appendSize(sb, maxDirectMemory);
				sb.append(" - ")
						.append(PepperLogHelper.getNicePercentage(directMemoryUsed, maxDirectMemory))
						.append(" (allocationCount=")
						.append(directMemoryBean.getCount())
						.append(')');
			});
		}

		// Add the number of live threads as the OS may refuse to make new
		// threads
		{
			long nbLiveThreads = THREAD_MBEAN.getThreadCount();
			sb.append(" LiveThreadCount=").append(nbLiveThreads);

			long threadMemory = nbLiveThreads * getMemoryPerThread();
			additionalMemory.addAndGet(threadMemory);
			sb.append(" (");
			appendSize(sb, threadMemory);
			sb.append(')');
		}

		return additionalMemory.get();
	}

	protected void appendPercentage(StringBuilder sb, long numerator, long denominator) {
		sb.append(PepperLogHelper.getNicePercentage(numerator, denominator));
	}

	public static void appendSize(StringBuilder sb, long size) {
		sb.append(PepperLogHelper.humanBytes(size));
	}

	@Deprecated
	public static String getNiceBytes(long size) {
		return PepperLogHelper.humanBytes(size).toString();
	}

	protected void doLog(IPepperGarbageCollectionNotificationInfo info) {
		// Javadoc tells duration is in millis while it seems to be in micros
		long duration = computeDurationMs(info);

		{
			String gcMessage = makeGCMessage(info);
			if (duration >= gcDurationMillisForInfoLog) {
				LOGGER.info(gcMessage);
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(gcMessage);
			}
		}

		// In case we encounter a long MarkSweep
		if (isFullGC(info)) {
			onFullGC(info);
		}

		logIfMemoryOverCap();
	}

	protected void onFullGC(IPepperGarbageCollectionNotificationInfo info) {
		long duration = computeDurationMs(info);

		if (duration > minFullGCMillisForThreadDump) {
			printThreadDump();
		} else {
			LOGGER.info("We encountered a FullGC but short enough ({}) not to print a ThreadDump",
					PepperLogHelper.humanDuration(duration));
		}

		// This block is comparable to the usage of -XX:+PrintClassHistogramAfterFullGC
		if (duration > minFullGCForHeapHistogram) {
			long heapUsed = getUsedHeap();
			long maxHeapBytesForHeapHisto = maxHeapGbForHeapHistogram * GB;
			if (heapUsed < maxHeapBytesForHeapHisto) {
				// Print HeapHistogram only if heap is small enough
				printHeapHistogram(HEAP_HISTO_LIMIT_NB_ROWS);
			} else {
				LOGGER.info("We encountered a long FullGC but no Heap histogram as {} < {}",
						PepperLogHelper.humanBytes(heapUsed),
						PepperLogHelper.humanBytes(maxHeapBytesForHeapHisto));
			}
		}
	}

	protected void logIfMemoryOverCap() {
		long heapUsed = getUsedHeap();
		long heapMax = getMaxHeap();
		if (isOverThreashold(heapUsed, heapMax)) {
			LocalDateTime now = LocalDateTime.now();
			overHeapThresholdSince.compareAndSet(null, now);

			LocalDateTime overThresholdSince = overHeapThresholdSince.get();
			if (overThresholdSince != null
					&& overThresholdSince.isBefore(now.minusMinutes(HEAP_ALERT_PERIOD_IN_MINUTES))) {
				// We are over heapThreshold since more than 15 minutes
				overHeapThresholdSince.set(null);
				onOverHeapAlertSinceTooLong(overThresholdSince);
			}
		} else {
			overHeapThresholdSince.getAndUpdate(current -> {
				if (current != null) {
					onMemoryBackUnderThreshold(heapUsed, heapMax);
				}

				return null;
			});
		}
	}

	protected boolean isOverThreashold(long heapUsed, long heapMax) {
		return heapUsed > heapMax * HEAP_ALERT_THRESHOLD;
	}

	protected long getUsedHeap() {
		return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
	}

	protected long getMaxHeap() {
		return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
	}

	protected void onMemoryBackUnderThreshold(long heapUsed, long heapMax) {
		// We got back under a nice memory level
		LOGGER.info("The heap got back under the threashold: {} out of {}",
				PepperLogHelper.humanBytes(heapUsed),
				PepperLogHelper.humanBytes(heapMax));
	}

	protected void onOverHeapAlertSinceTooLong(LocalDateTime overThresholdSince) {
		long heapUsed = getUsedHeap();
		long heapMax = getMaxHeap();
		LOGGER.warn("We have a heap of {} given a max of {} since {}",
				PepperLogHelper.humanBytes(heapUsed),
				PepperLogHelper.humanBytes(heapMax),
				overThresholdSince);
		printThreadDump();
	}

	protected boolean isFullGC(IPepperGarbageCollectionNotificationInfo info) {
		String gcName = info.getGcName();
		boolean isFullGC = FULL_GC_NAMES.contains(gcName);

		if (!isFullGC && !NOT_FULL_GC_NAMES.contains(gcName) && REPORTED_UNKNOWN_GC_NAMES.add(gcName)) {
			LOGGER.info("We encountered an Unknown GC name: {}. Please report to {}",
					gcName,
					"https://github.com/cormoran-io/pepper/issues");
		}

		return isFullGC;
	}

	protected void printThreadDump() {
		LocalDateTime beforeThreadDump = LocalDateTime.now();

		String threadDumpAsString = getAllThreads(true);

		this.latestThreadDump.set(beforeThreadDump);

		LOGGER.warn("Thread Dump: {}", threadDumpAsString);
	}

	protected void printSmartThreadDump() {
		LocalDateTime beforeThreadDump = LocalDateTime.now();

		String threadDumpAsString = pepperThreadDumper.getSmartThreadDumpAsString(false);

		this.latestThreadDump.set(beforeThreadDump);

		LOGGER.info("Thread Dump: {}", threadDumpAsString);
	}

	// https://github.com/javamelody/javamelody/blob/master/javamelody-core/src/main/java/net/bull/javamelody/internal/model/VirtualMachine.java#L163
	protected void printHeapHistogram(int nbRows) {
		String threadDumpAsString = "";
		LOGGER.debug("HeapHistogram: {}{}", System.lineSeparator(), threadDumpAsString);
	}

	/**
	 * @deprecated rely on {@link PepperForOracleJVM}
	 */
	@Deprecated
	protected static long getMaxDirectMemorySize() {
		return PepperForOracleJVM.maxDirectMemory();
	}

	protected long getMemoryPerThread() {
		List<String> arguments = getVMInputArguments();

		return getMemoryPerThread(arguments);
	}

	protected List<String> getVMInputArguments() {
		// https://stackoverflow.com/questions/1490869/how-to-get-vm-arguments-from-inside-of-java-application
		RuntimeMXBean runtimeMxBean = getRuntimeMXBean();
		List<String> arguments = runtimeMxBean.getInputArguments();
		return arguments;
	}

	protected long getMemoryPerThread(List<String> arguments) {
		// '-XX:ThreadStackSize=512' or '-Xss64k'

		Optional<String> xss = getOptionalArgument(arguments, XSS);
		Optional<String> tss = getOptionalArgument(arguments, XX_THREADSTACKSIZE);

		if (xss.isPresent()) {
			return PepperMemoryHelper.memoryAsLong(xss.get());
		} else if (tss.isPresent()) {
			return PepperMemoryHelper.memoryAsLong(xss.get());
		} else {
			// https://stackoverflow.com/questions/6020619/where-to-find-default-xss-value-for-sun-oracle-jvm
			return MB;
		}
	}

	public static Optional<String> getOptionalArgument(List<String> arguments, String option) {
		return arguments.stream().filter(s -> s.startsWith(option)).map(s -> s.substring(option.length())).findAny();
	}

	protected RuntimeMXBean getRuntimeMXBean() {
		return ManagementFactory.getRuntimeMXBean();
	}

	protected Optional<BufferPoolMXBean> directMemoryStatus() {
		return ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class)
				.stream()
				.filter(b -> "direct".equals(b.getName()))
				.findAny();
	}

	public long getDirectMemory() {
		return directMemoryStatus().map(BufferPoolMXBean::getMemoryUsed).orElse(-1L);
	}

	@ManagedAttribute
	public Date getLatestThreadDump() {
		LocalDateTime latest = latestThreadDump.get();
		if (latest == null) {
			return null;
		} else {
			return PepperTimeHelper.toDate(latest);
		}
	}

	@Override
	@ManagedAttribute
	public void setMarksweepDurationMillisForThreadDump(long marksweepDurationMillisForThreadDump) {
		this.minFullGCMillisForThreadDump = marksweepDurationMillisForThreadDump;
	}

	@Override
	@ManagedAttribute
	public long getMarksweepDurationMillisForThreadDump() {
		return minFullGCMillisForThreadDump;
	}

	@Override
	@ManagedAttribute
	public void setMarksweepDurationMillisForHeapHistogram(long marksweepDurationMillisForHeapHistogram) {
		this.minFullGCForHeapHistogram = marksweepDurationMillisForHeapHistogram;
	}

	@Override
	@ManagedAttribute
	public long getMarksweepDurationMillisForHeapHistogram() {
		return minFullGCForHeapHistogram;
	}

	@Override
	@ManagedAttribute
	public void setMaxHeapGbForHeapHistogram(long maxHeapGbForHeapHistogram) {
		this.maxHeapGbForHeapHistogram = maxHeapGbForHeapHistogram;
	}

	@Override
	@ManagedAttribute
	public long getMaxHeapGbForHeapHistogram() {
		return maxHeapGbForHeapHistogram;
	}

	@Override
	@ManagedOperation
	public void markNowAsAllocatedHeapReference() {
		allocatedHeapReference.set(getThreadNameToAllocatedHeap());
	}

	@Override
	@ManagedOperation
	public void clearAllocatedHeapReference() {
		allocatedHeapReference.set(Collections.<String, Long>emptyMap());
	}

	protected Map<? extends String, ? extends Long> getThreadNameToAllocatedHeap() {
		if (PepperForOracleJVM.isThreadAllocatedMemorySupported(THREAD_MBEAN)) {
			if (!PepperForOracleJVM.isThreadAllocatedMemoryEnabled(THREAD_MBEAN)) {
				PepperForOracleJVM.setThreadAllocatedMemoryEnabled(THREAD_MBEAN, true);
			}

			// Order Thread by Name
			Map<String, Long> threadNameToAllocatedMemory = new TreeMap<>();

			// Snapshot total allocation until now
			{
				long[] liveThreadIds = THREAD_MBEAN.getAllThreadIds();

				ThreadInfo[] threadInfos = THREAD_MBEAN.getThreadInfo(liveThreadIds);

				for (int i = 0; i < liveThreadIds.length; i++) {
					ThreadInfo threadInfo = threadInfos[i];
					if (threadInfo == null) {
						LOGGER.debug("No more info about thread #{}", i);
					} else {
						long threadAllocatedBytes =
								PepperForOracleJVM.getThreadAllocatedBytes(THREAD_MBEAN, liveThreadIds[i]);

						// We may receive -1
						if (threadAllocatedBytes > 0) {
							threadNameToAllocatedMemory.put(threadInfo.getThreadName(), threadAllocatedBytes);
						}
					}
				}
			}

			return Collections.unmodifiableMap(threadNameToAllocatedMemory);
		} else {
			return Collections.emptyMap();
		}
	}

	@Override
	@ManagedAttribute
	public Map<String, String> getThreadNameToAllocatedHeapNiceString() {
		// Mutable for adjustWithReference
		AtomicLongMap<String> threadNameToAllocatedHeap = AtomicLongMap.create(getThreadNameToAllocatedHeap());
		adjustWithReference(threadNameToAllocatedHeap, allocatedHeapReference.get());

		Map<String, Long> orderedByDecreasingSize =
				PepperJMXHelper.convertToJMXValueOrderedMap(threadNameToAllocatedHeap.asMap(), true);

		return PepperJMXHelper.convertToJMXMapString(convertByteValueToString(orderedByDecreasingSize));
	}

	protected void adjustWithReference(AtomicLongMap<String> currentHeapToAdjust,
			Map<? extends String, ? extends Long> reference) {
		// Remove the allocation what has been previously marked
		for (String threadName : currentHeapToAdjust.asMap().keySet()) {
			Long threadReferenceHeap = reference.get(threadName);
			if (threadReferenceHeap != null) {
				currentHeapToAdjust.addAndGet(threadName, -threadReferenceHeap);
			}
		}
	}

	@Override
	@ManagedAttribute
	public Map<String, String> getThreadGroupsToAllocatedHeapNiceString() {
		// Get current heap
		AtomicLongMap<String> threadNameToAllocatedHeap = AtomicLongMap.create(getThreadNameToAllocatedHeap());

		// Adjust with the marked reference
		adjustWithReference(threadNameToAllocatedHeap, allocatedHeapReference.get());

		// Group by thread
		AtomicLongMap<String> threadGroupToAllocatedHeap = groupThreadNames(threadNameToAllocatedHeap.asMap());

		Map<String, Long> orderedByDecreasingSize =
				PepperJMXHelper.convertToJMXValueOrderedMap(threadGroupToAllocatedHeap.asMap(), true);

		return PepperJMXHelper.convertToJMXMapString(convertByteValueToString(orderedByDecreasingSize));
	}

	protected AtomicLongMap<String> groupThreadNames(Map<String, Long> threadNameToAllocatedHeap) {
		AtomicLongMap<String> threadGroupToAllocatedHeap = AtomicLongMap.create();

		// Search for trailing digits
		Pattern p = Pattern.compile("(.*?)\\d+");

		for (Entry<String, Long> entry : threadNameToAllocatedHeap.entrySet()) {
			Matcher matcher = p.matcher(entry.getKey());

			if (matcher.matches()) {
				threadGroupToAllocatedHeap.addAndGet(matcher.group(1) + "X", entry.getValue());
			} else {
				threadGroupToAllocatedHeap.addAndGet(entry.getKey(), entry.getValue());
			}
		}

		return threadGroupToAllocatedHeap;
	}

	public static <T> Map<T, String> convertByteValueToString(Map<T, Long> threadNameToAllocatedHeap) {
		// Convert byte as long to byte as Nice String
		return Maps.transformValues(threadNameToAllocatedHeap, b -> PepperLogHelper.humanBytes(b).toString());
	}

	/**
	 * @param withoutMonitors
	 *            JConsole will set withoutMonitors = true by default
	 */
	@ManagedOperation
	@Override
	public String getAllThreads(boolean withoutMonitors) {
		return pepperThreadDumper.getThreadDumpAsString(!withoutMonitors);
	}

	/**
	 * @param withoutMonitors
	 *            JConsole will set withoutMonitors = true by default
	 */
	@ManagedOperation
	@Override
	public String getAllThreadsSmart(boolean withoutMonitors) {
		return pepperThreadDumper.getSmartThreadDumpAsString(!withoutMonitors);
	}

	@ManagedOperation
	@Override
	public String getAndLogCurrentMemoryStatus() {
		String currentMemoryStatusMessage = getCurrentMemoryStatusMessage();

		// Ensure status is written in the log file
		LOGGER.info(currentMemoryStatusMessage);

		return currentMemoryStatusMessage;
	}
}
