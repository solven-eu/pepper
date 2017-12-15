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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class TestPepperExecutorsHelper {
	@Test
	public void testNewExecutorService() {
		PepperExecutorsHelper.newSingleThreadExecutor("test", 3, PepperExecutorsHelper.TIMEOUT_POLICY_1_HOUR)
				.shutdown();
	}

	@Test
	public void testVeryLargeNumberOfThreads() {
		PepperExecutorsHelper.newShrinkableFixedThreadPool(Integer.MAX_VALUE, "test").shutdown();
	}

	@Test
	public void testNewScheduledExecutorService() {
		PepperExecutorsHelper.newShrinkableScheduledThreadPool("test", PepperExecutorsHelper.TIMEOUT_POLICY_1_HOUR)
				.shutdown();
	}

	@Test
	public void testRejectOfferWithTimeoutPolicy() {
		// TODO
	}

	// We fail adding many tasks with a listener on a bounded queue, even with timeout policy
	@Test(expected = RuntimeException.class)
	public void testBoundedQueueManytasks() {
		// Small thread, small queue and wait policy(abort policy would reject right away as we submit many tasks)
		ListeningExecutorService es = PepperExecutorsHelper.newShrinkableFixedThreadPool(2,
				"test",
				2,
				PepperExecutorsHelper.makeRejectedExecutionHandler(1, TimeUnit.SECONDS));

		for (int i = 0; i < 1000; i++) {
			ListenableFuture<Object> future = es.submit(() -> {
				Thread.sleep(1);
				return new Object();
			});

			future.addListener(() -> {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}, es);
		}

		MoreExecutors.shutdownAndAwaitTermination(es, 1, TimeUnit.MINUTES);
	}

	@Test
	public void testManyTasksBigPool() throws InterruptedException {
		ListeningExecutorService es = PepperExecutorsHelper.newShrinkableFixedThreadPool("Test",
				1000,
				PepperExecutorsHelper.makeRejectedExecutionHandler(1, TimeUnit.MINUTES));

		List<Supplier<Object>> runnables = new ArrayList<>();
		for (int i = 0; i < 10 * 1000; i++) {
			runnables.add(() -> {
				int coucou = 2;

				coucou *= coucou;

				return null;
			});
		}

		PepperExecutorsHelper.invokeAll(runnables, es);

		es.shutdown();
		es.awaitTermination(1, TimeUnit.MINUTES);
	}

	@Test
	public void testManyTasksSmallPool() throws InterruptedException {
		ListeningExecutorService es = PepperExecutorsHelper.newShrinkableFixedThreadPool("Test");

		List<Supplier<Object>> runnables = new ArrayList<>();
		for (int i = 0; i < 10 * 1000; i++) {
			runnables.add(() -> {
				int coucou = 2;

				coucou *= coucou;

				return null;
			});
		}

		PepperExecutorsHelper.invokeAll(runnables, es);

		es.shutdown();
		es.awaitTermination(1, TimeUnit.MINUTES);
	}

	@Test
	public void testExecuteAllRunnables() throws InterruptedException {
		ListeningExecutorService es = PepperExecutorsHelper.newShrinkableFixedThreadPool("Test");

		List<Runnable> runnables = new ArrayList<>();
		for (int i = 0; i < 10 * 1000; i++) {
			runnables.add(() -> {
				int coucou = 2;

				coucou *= coucou;
			});
		}

		PepperExecutorsHelper.executeAllRunnable(runnables, es);
	}

	// Test not invokeAll methods, which does NOT stream tasks
	@Test
	public void testNotStream() throws InterruptedException {
		final int maxSize =
				3 * PepperExecutorsHelper.DEFAULT_ACTIVE_TASKS * PepperExecutorsHelper.DEFAULT_PARTITION_TASK_SIZE;

		{
			final Set<Integer> materialized = Sets.newConcurrentHashSet();
			Iterator<Integer> iterable = new Iterator<Integer>() {
				AtomicInteger currentIndex = new AtomicInteger();

				@Override
				public boolean hasNext() {
					return currentIndex.get() < maxSize;
				}

				@Override
				public Integer next() {
					int output = currentIndex.getAndIncrement();

					materialized.add(output);

					return output;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}
			};

			// Split in block of size
			// ApexExecutorsHelper.DEFAULT_SPLIT_TASK_SIZE
			Iterator<? extends Runnable> runnables = PepperExecutorsHelper.partitions(iterable, param -> {
				// Not in stream: all have been materialized very soon
				Assert.assertEquals(maxSize, materialized.size());
			});

			// Preparing Runnable in a Collection DO materialize the whole
			// initial iterator
			List<Runnable> runnablesAsCollection = Lists.newArrayList(runnables);
			Assert.assertEquals(maxSize, materialized.size());

			// Of course, executing is far too late
			List<? extends ListenableFuture<?>> futures = PepperExecutorsHelper.invokeAllRunnable(runnablesAsCollection,
					MoreExecutors.newDirectExecutorService(),
					1,
					TimeUnit.MINUTES);
			Assert.assertEquals(maxSize, materialized.size());
			Assert.assertEquals(maxSize / PepperExecutorsHelper.DEFAULT_PARTITION_TASK_SIZE, futures.size());
		}
	}

}
