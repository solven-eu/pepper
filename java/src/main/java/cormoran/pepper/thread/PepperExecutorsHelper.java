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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.UnmodifiableIterator;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * Provides some helpers like named {@link ExecutorService}, easy splitting of tasks
 * 
 * @author Benoit Lacelle
 * 
 */
public class PepperExecutorsHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperExecutorsHelper.class);

	public static final int DEFAULT_LOG_ON_SLOW_QUEUE_MS = 1000;

	// See TransactionManager.addAll: We split on task of size 1024, so the
	// maximum nice partition size is 1023
	@Deprecated
	public static final int DEFAULT_PARTITION_TASK_SIZE = 1023;

	public static final int DEFAULT_MINIMUM_QUEUE_SIZE = 16;

	@Deprecated
	public static final int DEFAULT_SPLIT_TASK_SIZE = DEFAULT_PARTITION_TASK_SIZE;

	public static final RejectedExecutionHandler TIMEOUT_POLICY_1_HOUR =
			makeRejectedExecutionHandler(1, TimeUnit.HOURS);

	public static final RejectedExecutionHandler DEFAULT_ABORT_POLICY = new ThreadPoolExecutor.AbortPolicy();

	// We may switch to TIMEOUT_POLICY_1_HOUR, but we encounter some starving issues in some pattern. See
	// TestPepperExecutorsHelper.testBoundedQueueManytasks()
	public static final RejectedExecutionHandler DEFAULT_REJECTION_POLICY = DEFAULT_ABORT_POLICY;
	public static final int DEFAULT_QUEUE_CAPACITY = Integer.MAX_VALUE;

	public static final int DEFAULT_NB_CORES = Runtime.getRuntime().availableProcessors();

	/**
	 * Consider at most 2 active task per Nb of cores to have not too many tasks, but workaround IO latencies
	 */
	public static final int DEFAULT_ACTIVE_TASKS = DEFAULT_NB_CORES * 2;

	/**
	 * The number of seconds a Thread is kept alive before being closed if not used
	 * 
	 * @see ThreadPoolExecutor
	 */
	public static final int CORE_KEEP_ALIVE_IN_SECONDS = 60;

	private static final Callable<Object> NOOP_CALLABLE = () -> null;

	protected static boolean allowTryMax = false;

	private static final Function<Runnable, Callable<Object>> RUNNABLE_TO_CALLABLE = task -> {
		if (task == null) {
			return NOOP_CALLABLE;
		} else {
			return Executors.callable(task);
		}
	};

	protected PepperExecutorsHelper() {
		// hidden
	}

	// TODO: We would like this single-thread to autoclose if not used. If would help preventing thread-leakage
	public static ListeningExecutorService newSingleThreadExecutor(String threadNamePrefix) {
		// Default as defined in Executors.newSingleThreadExecutor
		// java.util.concurrent.ThreadPoolExecutor.defaultHandler
		return newSingleThreadExecutor(threadNamePrefix, DEFAULT_QUEUE_CAPACITY, DEFAULT_REJECTION_POLICY);
	}

	public static ListeningExecutorService newSingleThreadExecutor(String threadNamePrefix,
			int queueCapacity,
			RejectedExecutionHandler rejectedExecutionHandler) {
		final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(queueCapacity);

		// Inspired by Executors.newSingleThreadExecutor
		return MoreExecutors.listeningDecorator(new ThreadPoolExecutor(1,
				1,
				0L,
				TimeUnit.MILLISECONDS,
				queue,
				makeDaemonThreadFactory(threadNamePrefix),
				rejectedExecutionHandler));
	}

	public static ListeningScheduledExecutorService newSingleThreadScheduledExecutor(String threadNamePrefix) {
		return newSingleThreadScheduledExecutor(threadNamePrefix, DEFAULT_REJECTION_POLICY);
	}

	public static ListeningScheduledExecutorService newSingleThreadScheduledExecutor(String threadNamePrefix,
			RejectedExecutionHandler rejectedExecutionHandler) {
		return MoreExecutors.listeningDecorator(new ScheduledThreadPoolExecutor(1,
				makeDaemonThreadFactory(threadNamePrefix),
				rejectedExecutionHandler));
	}

	public static ThreadFactory makeDaemonThreadFactory(String threadNamePrefix) {
		CustomizableThreadFactory ctf = new CustomizableThreadFactory(threadNamePrefix);

		ctf.setDaemon(true);

		return ctf;
	}

	/**
	 * 
	 * @return the default number of threads is equal to the number of available processors
	 * 
	 * @see Runtime#availableProcessors()
	 */
	public static int getDefaultNbThreads() {
		return DEFAULT_NB_CORES;
	}

	public static ListeningExecutorService newShrinkableFixedThreadPool(String threadNamePrefix) {
		return newShrinkableFixedThreadPool(getDefaultNbThreads(), threadNamePrefix);
	}

	public static ListeningExecutorService newShrinkableFixedThreadPool(int maxThreads, String threadNamePrefix) {
		// An heuristic for maximum number of pending Runnable
		long queueCapacity = DEFAULT_MINIMUM_QUEUE_SIZE + (long) maxThreads * maxThreads;

		int queueCapacityAsInt = Ints.saturatedCast(queueCapacity);

		// The default JDK behavior has an abort policy, but it relies on a Queue with size Integer.MAX_VALUE: we would
		// die with OutOfmemory first. Then we prefer to wait first.

		// But it could lead to issues if we need to submit
		// many tasks, pushing the result in an intermediate bounded Queue before consuming the result: we would fill
		// the result queue, then fill the ES queue, and then wait indefinitely (as done in .submitAllInStream, or not
		// as we submit a single task , and then it is the async process which add more tasks)

		// TODO: we use MAX_VALUE else we seem to encounter thread starving, typically when adding ListeningFuture
		// listener as it automatically submit new tasks while the queue may already be full
		queueCapacityAsInt = DEFAULT_QUEUE_CAPACITY;

		return newShrinkableFixedThreadPool(maxThreads, threadNamePrefix, queueCapacityAsInt, DEFAULT_REJECTION_POLICY);
	}

	/**
	 * 
	 * @param threadNamePrefix
	 * @param queueCapacity
	 *            thr capacity of the queue holding pending runnable
	 * @param rejectedExecutionHandler
	 * @return an {@link ExecutorService} relying on daemon threads, closing automatically threads when not used for
	 *         some time
	 */
	public static ListeningExecutorService newShrinkableFixedThreadPool(String threadNamePrefix,
			int queueCapacity,
			RejectedExecutionHandler rejectedExecutionHandler) {
		return newShrinkableFixedThreadPool(getDefaultNbThreads(),
				threadNamePrefix,
				queueCapacity,
				rejectedExecutionHandler);
	}

	public static ListeningExecutorService newShrinkableFixedThreadPool(int nbThreads,
			String threadNamePrefix,
			int queueCapacity,
			RejectedExecutionHandler rejectedExecutionHandler) {
		final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(queueCapacity);

		ThreadPoolExecutor tpExecutor = new ThreadPoolExecutor(nbThreads,
				nbThreads,
				CORE_KEEP_ALIVE_IN_SECONDS,
				TimeUnit.SECONDS,
				queue,
				makeDaemonThreadFactory(threadNamePrefix),
				rejectedExecutionHandler);

		// Allow core threads to shutdown when there is no task to process
		tpExecutor.allowCoreThreadTimeOut(true);

		return MoreExecutors.listeningDecorator(tpExecutor);
	}

	public static ListeningExecutorService newShrinkableCachedThreadPool(String threadNamePrefix,
			RejectedExecutionHandler rejectedExecutionHandler) {
		ThreadPoolExecutor tpExecutor = new ThreadPoolExecutor(0,
				Integer.MAX_VALUE,
				CORE_KEEP_ALIVE_IN_SECONDS,
				TimeUnit.SECONDS,
				new SynchronousQueue<>(),
				makeDaemonThreadFactory(threadNamePrefix),
				rejectedExecutionHandler);

		// Allow core threads to shutdown when there is no task to process (even
		// if coreSize==0, just in case we change coreSize later)
		tpExecutor.allowCoreThreadTimeOut(true);

		return MoreExecutors.listeningDecorator(tpExecutor);
	}

	public static ListeningScheduledExecutorService newShrinkableScheduledThreadPool(String threadNamePrefix) {
		return newShrinkableScheduledThreadPool(threadNamePrefix, DEFAULT_REJECTION_POLICY);
	}

	public static ListeningScheduledExecutorService newShrinkableScheduledThreadPool(String threadNamePrefix,
			RejectedExecutionHandler rejectedExecutionHandler) {
		// At most nbThreads scheduled tasks could be active at the same time
		int nbThreads = getDefaultNbThreads();

		ScheduledThreadPoolExecutor tpExecutor = new ScheduledThreadPoolExecutor(nbThreads,
				makeDaemonThreadFactory(threadNamePrefix),
				rejectedExecutionHandler);

		// Allow core threads to shutdown when there is no task to process
		tpExecutor.setKeepAliveTime(CORE_KEEP_ALIVE_IN_SECONDS, TimeUnit.SECONDS);
		tpExecutor.allowCoreThreadTimeOut(true);

		// http://stackoverflow.com/questions/15888366/meaning-of-core-pool-size-in-scheduledthreadpoolexecutors-constructor
		// ScheduledThreadPoolExecutor makes no use of maximumPoolSize being greater than corePoolSize

		return MoreExecutors.listeningDecorator(tpExecutor);
	}

	@Deprecated
	public static RejectedExecutionHandler makeRejectedExecutionHandler(final int timeout, final TimeUnit unit) {
		return new OfferWithTimeoutPolicy(timeout, unit);
	}

	/**
	 * Softer generic signature
	 * 
	 * @see ExecutorService#invokeAll(Collection)
	 */
	// guaranteed by ListeningExecutorService#invokeAll contract
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List<? extends ListenableFuture<T>> invokeAll(Collection<? extends Supplier<? extends T>> tasks,
			ListeningExecutorService executorService) throws InterruptedException {
		Collection<Callable<T>> callables = Collections2.transform(tasks, PepperExecutorsHelper.supplierToCallable());

		return (List) executorService.invokeAll(callables);
	}

	private static <T> Function<Callable<? extends T>, Callable<T>> callableToCallable() {
		return input -> () -> input.call();
	}

	private static <T> Function<Supplier<? extends T>, Callable<T>> supplierToCallable() {
		return new Function<Supplier<? extends T>, Callable<T>>() {

			@Override
			public Callable<T> apply(final Supplier<? extends T> input) {
				return () -> input.get();
			}
		};
	}

	/**
	 * Softer generic signature
	 * 
	 * @see ExecutorService#invokeAll(Collection, long, TimeUnit)
	 */
	// guaranteed by ListeningExecutorService#invokeAll contract
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> List<? extends ListenableFuture<T>> invokeAll(Collection<? extends Callable<? extends T>> tasks,
			ListeningExecutorService executorService,
			long timeout,
			TimeUnit unit) throws InterruptedException {
		// Use transform to workaround Generic limitations in invokeAll
		Collection<Callable<T>> callables =
				Collections2.transform(tasks, PepperExecutorsHelper.<T>callableToCallable());

		return (List) executorService.invokeAll(callables, timeout, unit);
	}

	/**
	 * Same as ExecutorService#invokeAll(Collection, long, TimeUnit) but for {@link Runnable}. We do not return a
	 * {@link Collection} of {@link Future} as if one of the task fails, the whole compution is cancelled. And there is
	 * not timeout mechanism
	 * 
	 * @see ExecutorService#invokeAll(Collection, long, TimeUnit)
	 * @see Executor#execute(Runnable)
	 */
	public static void executeAllRunnable(Collection<? extends Runnable> tasks,
			ListeningExecutorService executorService) throws InterruptedException {
		invokeAllRunnable(tasks, executorService, Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	}

	/**
	 * Same as ExecutorService#invokeAll(Collection, long, TimeUnit) but for {@link Runnable}
	 * 
	 * @see ExecutorService#invokeAll(Collection, long, TimeUnit)
	 */
	public static List<? extends ListenableFuture<?>> invokeAllRunnable(Collection<? extends Runnable> tasks,
			ListeningExecutorService executorService,
			long timeout,
			TimeUnit unit) throws InterruptedException {
		return invokeAll(Collections2.transform(tasks, RUNNABLE_TO_CALLABLE), executorService, timeout, unit);
	}

	@Deprecated
	public static <T, V> Iterator<? extends Supplier<V>> partitions(Iterator<T> input,
			Function<? super List<T>, V> function) {
		return partitions(input, function, DEFAULT_PARTITION_TASK_SIZE);
	}

	public static <T, V> Iterator<? extends Supplier<V>> partitions(Iterator<T> input,
			final Function<? super List<T>, V> function,
			int partitionSize) {
		final Iterator<List<T>> underlyingIterator = Iterators.partition(input, partitionSize);

		return new AbstractIterator<Supplier<V>>() {

			@Override
			protected Supplier<V> computeNext() {
				if (underlyingIterator.hasNext()) {
					final List<T> next = underlyingIterator.next();
					return () -> function.apply(next);
				} else {
					return endOfData();
				}
			}
		};
	}

	@Deprecated
	public static <T, V> Iterator<? extends Runnable> partitions(Iterator<T> input,
			final Consumer<? super List<T>> consumer) {
		return partitions(input, consumer, DEFAULT_PARTITION_TASK_SIZE);
	}

	public static <T> Iterator<? extends Runnable> partitions(Iterator<T> input,
			final Consumer<? super List<T>> consumer,
			int partitionSize) {
		final UnmodifiableIterator<List<T>> underlyingIterator = Iterators.partition(input, partitionSize);

		return new AbstractIterator<Runnable>() {

			@Override
			protected Runnable computeNext() {
				if (underlyingIterator.hasNext()) {
					final List<T> next = underlyingIterator.next();
					return () -> consumer.accept(next);
				} else {
					return endOfData();
				}
			}
		};
	}

	// http://stackoverflow.com/questions/21163108/custom-thread-pool-in-java-8-parallel-stream
	public static ForkJoinPool newForkJoinPool(String threadPrefix) {
		return newForkJoinPool(threadPrefix, defaultForkJoinPoolParallelism());
	}

	// see ForkJoinPool.MAX_CAP and new ForkJoinPool()
	private static final int MAX_CAP = 0x7fff; // max #workers - 1

	private static int defaultForkJoinPoolParallelism() {
		return Math.min(MAX_CAP, Runtime.getRuntime().availableProcessors());
	}

	public static ForkJoinPool newForkJoinPool(String threadPrefix, int parallelism) {
		return new ForkJoinPool(parallelism, new NamingForkJoinWorkerThreadFactory(threadPrefix), null, false);
	}

	public static <T> long consumeByTimeBlock(Consumer<Long> c,
			Stream<T> conditionsToUpdate,
			int niceTransactionSeconds,
			Object object) {
		Iterator<T> iterator = conditionsToUpdate.iterator();

		long nbTransactions = 0;
		while (iterator.hasNext()) {
			try {
				long start = System.currentTimeMillis();

				c.accept(start);
			} finally {
				nbTransactions++;
			}
		}

		return nbTransactions;

	}
}
