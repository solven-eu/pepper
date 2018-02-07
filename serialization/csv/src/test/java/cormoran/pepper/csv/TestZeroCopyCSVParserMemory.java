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
package cormoran.pepper.csv;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;

import cormoran.pepper.jvm.PepperForOracleJVM;
import cormoran.pepper.logging.PepperLogHelper;
import cormoran.pepper.memory.IPepperMemoryConstants;
import cormoran.pepper.memory.PepperMemoryHelper;
import cormoran.pepper.primitive.PepperParserHelper;

//@Ignore("Broken: TODO")
public class TestZeroCopyCSVParserMemory {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestZeroCopyCSVParserMemory.class);

	ZeroCopyCSVParser parser = new ZeroCopyCSVParser(1024);

	static final int largeProblem = 2 * 1000000;

	protected static final ThreadMXBean THREAD_MBEAN = ManagementFactory.getThreadMXBean();

	// -javaagent:C:\HOMEWARE\local-maven-repo-2017-05-12\com\google\code\java-allocation-instrumenter\java-allocation-instrumenter\3.1.0\java-allocation-instrumenter-3.1.0.jar
	protected static final AtomicBoolean doRecord = new AtomicBoolean(false);

	@BeforeClass
	public static void initMBeans() throws IOException {
		// Enable monitoring of memory per thread
		if (PepperForOracleJVM.isThreadAllocatedMemorySupported(THREAD_MBEAN)) {
			if (!PepperForOracleJVM.isThreadAllocatedMemoryEnabled(THREAD_MBEAN)) {
				PepperForOracleJVM.setThreadAllocatedMemoryEnabled(THREAD_MBEAN, true);
			}
		}

		// https://github.com/google/allocation-instrumenter/wiki
		AllocationRecorder.addSampler(new Sampler() {
			@Override
			public void sampleAllocation(int count, String desc, Object newObj, long size) {
				if (doRecord.get()) {

					// System.out.println("I just allocated the object " +
					// newObj +
					// " of type " + desc + " whose size is " + size);
					if (count != -1) {
						System.out.print("It's an array of size ");
						System.out.print(count);
						System.out.print(" type ");
						System.out.print(desc);
						System.out.print(" size: ");
						System.out.print(size);
						System.out.println();
					} else {
						System.out.print("It's an object of ");
						System.out.print(" type ");
						System.out.print(desc);
						System.out.print(" size: ");
						System.out.print(size);
						System.out.println();
					}
				}
			}
		});
	}

	protected long snapshotMemory() {
		return PepperForOracleJVM.getThreadAllocatedBytes(THREAD_MBEAN, Thread.currentThread().getId());
	}

	public static DoubleStream streamOfValues(int problemSize) {
		return IntStream.range(0, problemSize).mapToDouble(i -> 1D * i * Math.sqrt(i));
	}

	@Test
	public void testMemoryConsumptionIsConstantOverProblemSize() throws IOException {
		int smallProblemSize = 100;

		String smallProblem = streamOfValues(smallProblemSize).mapToInt(i -> (int) i).mapToObj(i -> i + "\r\n").collect(
				Collectors.joining());

		String bigProblem = streamOfValues(largeProblem).mapToInt(i -> (int) i).mapToObj(i -> i + "\r\n").collect(
				Collectors.joining());

		int[] smallArray = new int[smallProblemSize];
		int[] bigArray = new int[largeProblem];

		// Initialize any static buffer, or class loading
		parser.parse(new StringReader(smallProblem),
				',',
				ZeroCopyConsumers.intBinaryOperator((rowIndex, rowValue) -> smallArray[rowIndex] = rowValue));

		long threadAllocatedBytes = snapshotMemory();
		doRecord.set(true);

		parser.parse(new StringReader(smallProblem),
				',',
				ZeroCopyConsumers.intBinaryOperator((rowIndex, rowValue) -> smallArray[rowIndex] = rowValue));
		long memoryAfterSmall = snapshotMemory();

		parser.parse(new StringReader(bigProblem),
				',',
				ZeroCopyConsumers.intBinaryOperator((rowIndex, rowValue) -> bigArray[rowIndex] = rowValue));

		doRecord.set(false);
		long memoryAfterBig = snapshotMemory();

		long bigAllocation = memoryAfterBig - memoryAfterSmall;
		long smallAllocation = memoryAfterSmall - threadAllocatedBytes;

		// The allocation is not stable (JIT, ...): we simply check the difference is small enough
		Assertions.assertThat(Math.abs(bigAllocation - smallAllocation))
				.isLessThanOrEqualTo(10 * IPepperMemoryConstants.KB);
	}

	@Test
	public void testBenchMemoryConsumption_int() throws IOException {
		String oneColumnOfInts = streamOfValues(largeProblem).mapToInt(i -> (int) i).mapToObj(i -> i + "\r\n").collect(
				Collectors.joining());

		int[] array = new int[largeProblem];

		long threadAllocatedBytes = snapshotMemory();

		parser.parse(new StringReader(oneColumnOfInts),
				',',
				ZeroCopyConsumers.intBinaryOperator((rowIndex, rowValue) -> array[rowIndex] = rowValue));
		long memoryAfter = snapshotMemory();

		Assert.assertArrayEquals(streamOfValues(largeProblem).mapToInt(i -> (int) i).toArray(), array);

		long memoryDiff = memoryAfter - threadAllocatedBytes;
		LOGGER.info("Memory usage for {} ints: {} (String is {})",
				largeProblem,
				PepperLogHelper.humanBytes(memoryDiff),
				PepperLogHelper.humanBytes(PepperMemoryHelper.getStringMemory(oneColumnOfInts)));

		// Check the total allocation for this large problem is very low
		Assertions.assertThat(Math.abs(memoryDiff - 121 * IPepperMemoryConstants.KB))
				.isLessThanOrEqualTo(IPepperMemoryConstants.KB);
	}

	@Test
	public void testBenchMemoryConsumption_long() throws IOException {
		String oneColumnOfLongs =
				streamOfValues(largeProblem).mapToLong(i -> (long) i).mapToObj(i -> i + "\r\n").collect(
						Collectors.joining());

		long[] array = new long[largeProblem];

		long threadAllocatedBytes = snapshotMemory();

		StringReader reader1 = new StringReader(oneColumnOfLongs);
		StringReader reader2 = new StringReader(oneColumnOfLongs);
		IZeroCopyConsumer writeToArray =
				ZeroCopyConsumers.longBinaryOperator((rowIndex, rowValue) -> array[(int) rowIndex] = rowValue);
		IZeroCopyConsumer writeToArray2 =
				ZeroCopyConsumers.longBinaryOperator((rowIndex, rowValue) -> array[(int) rowIndex] = rowValue);
		parser.parse(reader1, ',', writeToArray);
		long memoryAfter = snapshotMemory();

		doRecord.set(true);
		threadAllocatedBytes = snapshotMemory();
		parser.parse(reader2, ',', writeToArray2);
		memoryAfter = snapshotMemory();
		doRecord.set(false);

		Assert.assertArrayEquals(streamOfValues(largeProblem).mapToLong(i -> (long) i).toArray(), array);

		long memoryDiff = memoryAfter - threadAllocatedBytes;
		LOGGER.info("Memory usage for {} longs: {} (String is {})",
				largeProblem,
				PepperLogHelper.humanBytes(memoryDiff),
				PepperLogHelper.humanBytes(PepperMemoryHelper.getStringMemory(oneColumnOfLongs)));

		// Check the total allocation for this large problem is very low
		Assertions.assertThat(Math.abs(memoryDiff - 11 * IPepperMemoryConstants.KB))
				.isLessThanOrEqualTo(IPepperMemoryConstants.KB);
	}

	@Test
	public void testBenchMemoryConsumption_double() throws IOException {
		// Ensure we are initialized
		PepperParserHelper.initialize();

		String oneColumnOfDoubles =
				streamOfValues(largeProblem).mapToObj(i -> i + "\r\n").collect(Collectors.joining());

		double[] array = new double[largeProblem];

		long threadAllocatedBytes = snapshotMemory();

		doRecord.set(true);
		parser.parse(new StringReader(oneColumnOfDoubles),
				',',
				ZeroCopyConsumers.doubleBinaryOperator((rowIndex, rowValue) -> array[(int) rowIndex] = rowValue));
		doRecord.set(false);
		long memoryAfter = snapshotMemory();

		// Assert.assertArrayEquals(streamOfValues(largeProblem).toArray(), array, 0.01D);

		long memoryDiff = memoryAfter - threadAllocatedBytes;
		LOGGER.info("Memory usage for {} doubles: {} (String is {})",
				largeProblem,
				PepperLogHelper.humanBytes(memoryDiff),
				PepperLogHelper.humanBytes(PepperMemoryHelper.getStringMemory(oneColumnOfDoubles)));

		// Check the total allocation for this large problem is very low
		Assertions.assertThat(Math.abs(memoryDiff - 43 * IPepperMemoryConstants.KB))
				.isLessThanOrEqualTo(IPepperMemoryConstants.KB);
	}

	@Ignore("Very slow, for JMC reports only")
	@Test
	public void testHugeBenchMemoryConsumption() throws IOException {
		String oneColumnOfInts = streamOfValues(largeProblem).mapToObj(i -> i + "\r\n").collect(Collectors.joining());

		int[] array = new int[largeProblem];

		IntStream.range(0, largeProblem).forEach(index -> {
			try {
				parser.parse(new StringReader(oneColumnOfInts),
						',',
						ZeroCopyConsumers.intBinaryOperator((rowIndex, rowValue) -> array[rowIndex] = rowValue));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		Assert.assertArrayEquals(streamOfValues(largeProblem).mapToInt(i -> (int) i).toArray(), array);
	}
}
