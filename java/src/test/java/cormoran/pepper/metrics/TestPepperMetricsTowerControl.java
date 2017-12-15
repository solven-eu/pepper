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
package cormoran.pepper.metrics;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.NavigableMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.google.common.eventbus.EventBus;

import cormoran.pepper.thread.PepperExecutorsHelper;
import cormoran.pepper.thread.PepperThreadDumper;

public class TestPepperMetricsTowerControl {
	protected PepperActiveTasksMonitor makePepperMetricsTowerControl() {
		return new PepperActiveTasksMonitor(new PepperThreadDumper(ManagementFactory.getThreadMXBean()));
	}

	@Test
	public void testMetricsTowerControl() {
		EventBus eventBus = new EventBus((exception, context) -> {
			throw new RuntimeException(exception);
		});

		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();
		eventBus.register(mtc);

		eventBus.post(new OutOfMemoryError("Unit-Test forcing an OOM"));

		// 1 for FailureEvent
		// Assert.assertEquals(1, mtc.metricRegistry.getCounters().size());
		//
		// // 1 for SizeMetricEvent
		// Assert.assertEquals(1, mtc.getMetricsAsString().size());
	}

	@Test
	public void testStartEndWorkFlow() {
		EventBus eventBus = new EventBus((exception, context) -> {
			throw new RuntimeException(exception);
		});

		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();
		eventBus.register(mtc);

		Assert.assertEquals(0, mtc.getActiveTasksSize());

		TaskStartEvent se = new TaskStartEvent(this, "test");
		eventBus.post(se);
		Assert.assertEquals(1, mtc.getActiveTasksSize());

		eventBus.post(new TaskEndEvent(se));
		Assert.assertEquals(0, mtc.getActiveTasksSize());
	}

	@Test
	public void testInvalidateActiveTasks() {
		EventBus eventBus = new EventBus((exception, context) -> {
			throw new RuntimeException(exception);
		});

		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();
		eventBus.register(mtc);

		TaskStartEvent se = new TaskStartEvent(this, "test");
		eventBus.post(se);
		Assert.assertEquals(1, mtc.getActiveTasks().size());

		// Invalidate something which does not exist
		Assert.assertFalse(mtc.invalidateActiveTasks("not-existing"));

		// Invalidate something that exists
		boolean invalidateResult = mtc.invalidateActiveTasks(se.toStringNoStack());
		Assert.assertTrue(invalidateResult);
		Assert.assertEquals(0, mtc.getActiveTasks().size());

		// Check things goes well when ending an invalidated event
		eventBus.post(new TaskEndEvent(se));
		Assert.assertEquals(0, mtc.getActiveTasks().size());
	}

	@Test
	public void testEndEventWithoutStartEvent() {
		EventBus eventBus = new EventBus((exception, context) -> {
			throw new RuntimeException(exception);
		});

		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();
		eventBus.register(mtc);

		TaskStartEvent notSubmittedStartEvent = new TaskStartEvent(this, "detailName");
		mtc.onEndEvent(new TaskEndEvent(notSubmittedStartEvent));
	}

	@Test
	public void testStartWithStack() {
		EventBus eventBus = new EventBus((exception, context) -> {
			throw new RuntimeException(exception);
		});

		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();
		eventBus.register(mtc);

		mtc.setDoRememberStack(true);
		try {
			TaskStartEvent se = new TaskStartEvent(this, "some");
			eventBus.post(se);

			NavigableMap<Date, String> activeTasks = mtc.getActiveTasks();

			// Check thetre is a stack
			Assert.assertTrue(activeTasks.toString().contains("org.junit.runners.model."));
		} finally {
			mtc.setDoRememberStack(false);
		}
	}

	@Test
	public void testThreadDump() {
		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();

		mtc.getAllThreads(true);
		mtc.getAllThreads(false);
	}

	@Test
	public void benchPerformance() throws InterruptedException {
		final PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();

		final EventBus eventBus = new EventBus();
		eventBus.register(mtc);

		ExecutorService es = PepperExecutorsHelper.newShrinkableFixedThreadPool(64,
				"benchApexMetricsTowerControl",
				1000,
				PepperExecutorsHelper.TIMEOUT_POLICY_1_HOUR);

		for (int i = 0; i < 1000000; i++) {
			es.execute(() -> {
				try (AutoCloseableTaskStartEvent startEvent = TaskStartEvent.post(eventBus::post, mtc, "Test")) {
					// Nothing to do, but to close the startEvent
				}
			});
		}

		es.shutdown();
		es.awaitTermination(1, TimeUnit.MINUTES);
	}

	@Test
	public void testLogLongRunningWithProgress() {
		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();

		Assert.assertEquals(0, mtc.getActiveTasksSize());

		AtomicLong progress = new AtomicLong();
		TaskStartEvent se = new TaskStartEvent(this, Collections.emptyMap(), () -> progress.get(), "test");

		Assert.assertTrue(mtc.noNewLine(se).toString().contains("progress=0"));

		progress.set(123456789L);
		Assert.assertTrue(mtc.noNewLine(se).toString().contains("progress=123456789"));
	}

	@Test
	public void testLogLongRunningWithoutProgress() {
		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();

		Assert.assertEquals(0, mtc.getActiveTasksSize());

		AtomicLong progress = new AtomicLong();
		TaskStartEvent se = new TaskStartEvent(this, Collections.emptyMap(), () -> progress.get(), "test");

		progress.set(-1L);
		Assert.assertFalse(mtc.noNewLine(se).toString().contains("progress"));
	}

	// We test mainly coverage
	@Test
	public void testOnApexMetricsTowerControl() {
		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();

		TaskStartEvent startEvent = new TaskStartEvent(this, "testOnApexMetricsTowerControl");
		Arrays.stream(RemovalCause.values())
				.forEach(c -> mtc.onActiveTaskRemoval(RemovalNotification.create(startEvent, LocalDateTime.now(), c)));
	}

	@Test
	public void testGetLongRunningCheck() throws Exception {
		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();

		Assert.assertEquals(PepperActiveTasksMonitor.DEFAULT_LONGRUNNINGCHECK_SECONDS,
				mtc.getLongRunningCheckSeconds());

		// This should call mtc.scheduleLogLongRunningTasks();
		mtc.afterPropertiesSet();

		// Get the current task future
		ScheduledFuture<?> currentFuture = mtc.scheduledFuture.get();

		// Change the timeout: this should cancel the previous recurrent task
		mtc.setLongRunningCheckSeconds(123);

		Assert.assertEquals(123, mtc.getLongRunningCheckSeconds());

		Assert.assertNotSame(currentFuture, mtc.scheduledFuture.get());

		Assert.assertFalse(mtc.scheduledFuture.get().isCancelled());
		Assert.assertTrue(currentFuture.isCancelled());
	}

	// Start and End events consuming methods have the annotation @AllowConcurrentEvents: we may receive the end before
	// the start
	@Test
	public void testReceiveEndBeforeStart() {
		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();

		TaskStartEvent start = new TaskStartEvent(this, "testReceiveEndBeforeStart");

		TaskEndEvent end = TaskEndEvent.buildEndEvent(start);

		// Let's imagine the end arrives before the start
		mtc.onEndEvent(end);
		mtc.onStartEvent(start);

		// We should have no active task as the end event arrives
		Assert.assertEquals(0, mtc.getActiveTasksSize());
	}

	@Test
	public void testNeverReceiveEndEventButClosed() {
		PepperActiveTasksMonitor mtc = makePepperMetricsTowerControl();

		// Happens if the event is stopped out of the eventBus workflow
		TaskStartEvent start = new TaskStartEvent(this, "testEndEventNeverReceived");

		// We receive the start
		mtc.onStartEvent(start);

		// But the end is never published
		// It may also represent the race-condition of the end happing during the start registration
		TaskEndEvent end = TaskEndEvent.buildEndEvent(start);
		Assert.assertNotNull(end);

		// Run a log operation
		mtc.logLongRunningTasks();

		// Check the log have detected the event is ended
		Assert.assertEquals(0, mtc.activeTasks.size());

		// Check we recorded the endEvent by detected from the known startEvent
		Assert.assertEquals(1, mtc.endEventNotReceivedExplicitely.get());
	}
}
