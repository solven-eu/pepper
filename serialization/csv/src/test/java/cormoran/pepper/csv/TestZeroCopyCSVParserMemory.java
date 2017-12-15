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

import cormoran.pepper.jvm.PepperForOracleJVM;
import cormoran.pepper.logging.PepperLogHelper;
import cormoran.pepper.memory.IPepperMemoryConstants;
import cormoran.pepper.memory.PepperMemoryHelper;
import cormoran.pepper.primitive.PepperParserHelper;

@Ignore("Broken: TODO")
public class TestZeroCopyCSVParserMemory {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestZeroCopyCSVParserMemory.class);

	ZeroCopyCSVParser parser = new ZeroCopyCSVParser(1024);

	static final int largeProblem = 1000000;

	protected static final ThreadMXBean THREAD_MBEAN = ManagementFactory.getThreadMXBean();

	@BeforeClass
	public static void initMBeans() throws IOException {
		// Enable monitoring of memory per thread
		if (PepperForOracleJVM.isThreadAllocatedMemorySupported(THREAD_MBEAN)) {
			if (!PepperForOracleJVM.isThreadAllocatedMemoryEnabled(THREAD_MBEAN)) {
				PepperForOracleJVM.setThreadAllocatedMemoryEnabled(THREAD_MBEAN, true);
			}
		}
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

		String smallProblem = streamOfValues(smallProblemSize).mapToObj(i -> i + "\r\n").collect(Collectors.joining());

		String bigProblem = streamOfValues(largeProblem).mapToObj(i -> i + "\r\n").collect(Collectors.joining());

		int[] smallArray = new int[smallProblemSize];
		int[] bigArray = new int[largeProblem];

		// Initialize any static buffer
		parser.parse(new StringReader(smallProblem),
				',',
				ZeroCopyConsumers.intBinaryOperator((rowIndex, rowValue) -> smallArray[rowIndex] = rowValue));

		long threadAllocatedBytes = snapshotMemory();

		parser.parse(new StringReader(smallProblem),
				',',
				ZeroCopyConsumers.intBinaryOperator((rowIndex, rowValue) -> smallArray[rowIndex] = rowValue));
		long memoryAfterSmall = snapshotMemory();

		parser.parse(new StringReader(bigProblem),
				',',
				ZeroCopyConsumers.intBinaryOperator((rowIndex, rowValue) -> bigArray[rowIndex] = rowValue));
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
				PepperLogHelper.getNiceMemory(memoryDiff),
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.getStringMemory(oneColumnOfInts)));

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

		parser.parse(new StringReader(oneColumnOfLongs),
				',',
				ZeroCopyConsumers.longBinaryOperator((rowIndex, rowValue) -> array[(int) rowIndex] = rowValue));
		long memoryAfter = snapshotMemory();

		Assert.assertArrayEquals(streamOfValues(largeProblem).mapToLong(i -> (long) i).toArray(), array);

		long memoryDiff = memoryAfter - threadAllocatedBytes;
		LOGGER.info("Memory usage for {} longs: {} (String is {})",
				largeProblem,
				PepperLogHelper.getNiceMemory(memoryDiff),
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.getStringMemory(oneColumnOfLongs)));

		// Check the total allocation for this large problem is very low
		Assertions.assertThat(Math.abs(memoryDiff - 153 * IPepperMemoryConstants.KB))
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

		parser.parse(new StringReader(oneColumnOfDoubles),
				',',
				ZeroCopyConsumers.doubleBinaryOperator((rowIndex, rowValue) -> array[(int) rowIndex] = rowValue));
		long memoryAfter = snapshotMemory();

		// Assert.assertArrayEquals(streamOfValues(largeProblem).toArray(), array, 0.01D);

		long memoryDiff = memoryAfter - threadAllocatedBytes;
		LOGGER.info("Memory usage for {} doubles: {} (String is {})",
				largeProblem,
				PepperLogHelper.getNiceMemory(memoryDiff),
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.getStringMemory(oneColumnOfDoubles)));

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
