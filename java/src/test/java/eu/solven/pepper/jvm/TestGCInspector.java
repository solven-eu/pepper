/**
 * The MIT License
 * Copyright (c) 2014-2025 Benoit Lacelle - SOLVEN
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.InstanceNotFoundException;
import javax.management.JMException;
import javax.management.MalformedObjectNameException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.AnnotationJmxAttributeSource;
import org.springframework.jmx.export.assembler.MetadataMBeanInfoAssembler;

import com.google.common.collect.ImmutableMap;

import eu.solven.pepper.memory.IPepperMemoryConstants;
import eu.solven.pepper.thread.IThreadDumper;

public class TestGCInspector implements IPepperMemoryConstants {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TestGCInspector.class);

	/**
	 * Test by monitoring an application doing stress-full memory allocation
	 *
	 * @throws Exception
	 */
	@Test
	public void testReporter() throws Exception {
		GCInspector gcInspector = new GCInspector(Mockito.mock(IThreadDumper.class));
		gcInspector.setMarksweepDurationMillisForThreadDump(1);
		gcInspector.afterPropertiesSet();

		try {
			Queue<int[]> allArrays = new LinkedBlockingQueue<>();
			for (int i = 0; i < Integer.MAX_VALUE; i++) {
				// Allocate more and more memory to stress the GC
				int[] array = new int[i * i * i * KB_INT];

				LOGGER.info("Allocate: " + (array.length / (MB * 4L)) + "MB");

				// We keep the array in memory to have more and more objects
				allArrays.add(array);

				if (allArrays.size() > 3) {
					// Free some memory to enable GC
					allArrays.poll();
				}

				if (gcInspector.getLatestThreadDump() != null) {
					throw new OutOfMemoryError("Early quit: we stressed enough the GC");
				}
			}
			Assertions.fail("We expect an OOM");
		} catch (OutOfMemoryError e) {
			LOGGER.info("We got the expected OOM");
			// We expect an OutOfMemorry as it is the best way to monitor GC
			// activity
		}

		// Tough to stress enough the GC to get a ThreadDump
		// Assertions.assertNotNull(gcInspector.getLatestThreadDump());

		gcInspector.destroy();
	}

	@Test
	public void testGetThreadNameAllocatedHeap() {
		GCInspector gcInspector = new GCInspector(Mockito.mock(IThreadDumper.class));

		Map<String, String> allocated = gcInspector.getThreadNameToAllocatedHeapNiceString();
		Assertions.assertTrue(allocated.containsKey(Thread.currentThread().getName()));

		gcInspector.markNowAsAllocatedHeapReference();

		Map<String, String> allocatedAfterMark = gcInspector.getThreadNameToAllocatedHeapNiceString();
		Assertions.assertTrue(allocatedAfterMark.containsKey(Thread.currentThread().getName()));
	}

	@Test
	public void testGetThreadGroupsAllocatedHeap() {
		GCInspector gcInspector = new GCInspector(Mockito.mock(IThreadDumper.class));

		Map<String, String> allocated = gcInspector.getThreadGroupsToAllocatedHeapNiceString();
		Assertions.assertTrue(allocated.containsKey(Thread.currentThread().getName()));

		gcInspector.markNowAsAllocatedHeapReference();

		Map<String, String> allocatedAfterMark = gcInspector.getThreadGroupsToAllocatedHeapNiceString();
		Assertions.assertTrue(allocatedAfterMark.containsKey(Thread.currentThread().getName()));
	}

	@Test
	public void testGroupThreadNames() {
		GCInspector gcInspector = new GCInspector(Mockito.mock(IThreadDumper.class));

		Map<String, Long> detailedMap = new HashMap<>();

		detailedMap.put("SingleThread", 1L);
		detailedMap.put("GroupThread-1-0", 3L);
		detailedMap.put("GroupThread-2-1", 5L);
		detailedMap.put("GroupThread-2-234", 7L);

		Map<String, Long> grouped = gcInspector.groupThreadNames(detailedMap).asMap();
		Assertions.assertEquals(ImmutableMap.of("SingleThread", 1L, "GroupThread-1-X", 3L, "GroupThread-2-X", 12L),
				grouped);
	}

	@Test
	public void testTriggerFullGC() throws Exception {
		AtomicInteger nbBackToNormal = new AtomicInteger();

		AtomicLong usedHeap = new AtomicLong();
		AtomicLong maxHeap = new AtomicLong(100);

		GCInspector gcInspector = new GCInspector(Mockito.mock(IThreadDumper.class)) {
			@Override
			protected void onMemoryBackUnderThreshold(long heapUsed, long heapMax) {
				nbBackToNormal.incrementAndGet();
			}

			@Override
			protected long getUsedHeap() {
				return usedHeap.get();
			}

			@Override
			protected long getMaxHeap() {
				return maxHeap.get();
			}
		};

		// 10%
		usedHeap.set(10);

		gcInspector.logIfMemoryOverCap();
		Assertions.assertEquals(0, nbBackToNormal.get());

		// 95%
		usedHeap.set(95);

		gcInspector.logIfMemoryOverCap();
		Assertions.assertEquals(0, nbBackToNormal.get());

		// 15%
		usedHeap.set(15);

		gcInspector.logIfMemoryOverCap();
		Assertions.assertEquals(1, nbBackToNormal.get());

		// Log again: still OK
		gcInspector.logIfMemoryOverCap();
		Assertions.assertEquals(1, nbBackToNormal.get());
	}

	// We check the getters and setters are valid according to Spring
	@Test
	public void testGetterSetters() throws JMException {
		MetadataMBeanInfoAssembler assembler = new MetadataMBeanInfoAssembler();

		assembler.setAttributeSource(new AnnotationJmxAttributeSource());

		assembler.afterPropertiesSet();

		assembler.getMBeanInfo(new GCInspector(), "beanKey");
	}

	@Test
	public void testMemoryPerThread() {
		long memory = new GCInspector().getMemoryPerThread(Arrays.asList("-Xss512k"));
		Assertions.assertEquals(524288L, memory);
	}

	@Test
	public void testGetOptionalArgument() {
		org.assertj.core.api.Assertions.assertThat(GCInspector.getOptionalArgument(Arrays.asList("-Xss512k"), "-Xss"))
				.isPresent()
				.hasValue("512k");
		org.assertj.core.api.Assertions
				.assertThat(GCInspector.getOptionalArgument(Arrays.asList("-Xss512k"), "-Xss512k"))
				.isPresent()
				.hasValue("");
	}

	@Test
	public void testAppendDetailsAboutMove() throws MalformedObjectNameException, InstanceNotFoundException {
		GCInspector gcInspector = new GCInspector(Mockito.mock(IThreadDumper.class));
		gcInspector.afterPropertiesSet();

		StringBuilder sb = new StringBuilder();
		gcInspector.appendDetailsAboutMove(sb, 123, 456);

		org.assertj.core.api.Assertions.assertThat(sb.toString())
				.matches("=456B after allocating \\d{1,4}\\wB through all threads including \\d{1,4}\\wB from .+");
	}

	@Test
	public void testAppendDetailsAboutMove_gcd() throws MalformedObjectNameException, InstanceNotFoundException {
		GCInspector gcInspector = new GCInspector(Mockito.mock(IThreadDumper.class));
		gcInspector.afterPropertiesSet();

		StringBuilder sb = new StringBuilder();
		// Negative to represent a GC operation
		gcInspector.appendDetailsAboutMove(sb, -123, 456);

		org.assertj.core.api.Assertions.assertThat(sb.toString())
				.matches(
						"=456B-123B garbage collected after allocating \\d{1,4}\\wB through all threads including \\d{1,4}\\wB from .+");
	}
}
