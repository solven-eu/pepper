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

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link RejectedExecutionHandler} which enables waiting for a given timeout before rejecting tasks if queue is full
 * 
 * @author Benoit Lacelle
 *
 */
@Deprecated
public class OfferWithTimeoutPolicy implements RejectedExecutionHandler {

	protected static final Logger LOGGER = LoggerFactory.getLogger(OfferWithTimeoutPolicy.class);

	protected int timeout;
	protected TimeUnit unit;

	/**
	 * This boolean makes sure we monitor the time to go through the queue not to often
	 */
	protected final AtomicBoolean isGoingToLog = new AtomicBoolean();

	public OfferWithTimeoutPolicy(int timeout, TimeUnit unit) {
		this.timeout = timeout;
		this.unit = unit;
	}

	@Override
	public void rejectedExecution(final Runnable r, final ThreadPoolExecutor executor) {
		if (isGoingToLog.compareAndSet(false, true)) {
			try {
				final long start = System.currentTimeMillis();
				boolean result = executor.getQueue().offer(() -> {
					r.run();

					long time = System.currentTimeMillis() - start;
					if (time > PepperExecutorsHelper.DEFAULT_LOG_ON_SLOW_QUEUE_MS) {
						LOGGER.warn("The pool {} is full and it took {} ms for the first rejected task to be processed",
								executor,
								time);

					}
					isGoingToLog.set(false);
				}, timeout, unit);

				if (!result) {
					throw new RuntimeException("We failed pushing the task " + r + " after waiting " + timeout + unit);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				isGoingToLog.set(false);
				throw new RejectedExecutionException("Task " + r.toString() + " rejected", e);
			}
		} else {
			try {
				boolean result = executor.getQueue().offer(r, timeout, unit);

				if (!result) {
					throw new RuntimeException("We failed pushing the task " + r + " after waiting " + timeout + unit);
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new RejectedExecutionException("Task " + r.toString() + " rejected", e);
			}
		}
	}
}