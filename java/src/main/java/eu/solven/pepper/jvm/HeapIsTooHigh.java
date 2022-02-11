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
package eu.solven.pepper.jvm;

import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.LongSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.google.common.annotations.Beta;
import com.google.common.math.LongMath;

/**
 * This component enables switching some special mode when the memory is too high: cancel old queries, stop enqueuing in
 * transaction wrapper,...
 * 
 * @author Benoit Lacelle
 *
 */
@Beta
@ManagedResource
public class HeapIsTooHigh implements IHeapIsTooHigh {
	protected static final Logger LOGGER = LoggerFactory.getLogger(HeapIsTooHigh.class);

	private static final long HUNDRED = 100;

	protected final AtomicInteger ratioForTooHigh;

	protected final LongSupplier heapSupplier;
	protected final LongSupplier maxHeapSupplier;

	public HeapIsTooHigh() {
		this(() -> ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(),
				() -> ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed(),
				DEFAULT_HEAP_RATIO_WRITE_LIMIT);
	}

	public HeapIsTooHigh(LongSupplier heapProvider, LongSupplier maxHeapSupplier, int heapRatioForTooHigh) {
		this.heapSupplier = heapProvider;
		this.maxHeapSupplier = maxHeapSupplier;
		ratioForTooHigh = new AtomicInteger(heapRatioForTooHigh);
	}

	@ManagedAttribute
	public int getRatioForTooHigh() {
		return ratioForTooHigh.get();
	}

	@ManagedAttribute
	@Override
	public void setRatioForTooHigh(int ratioForTooHigh) {
		this.ratioForTooHigh.set(ratioForTooHigh);
	}

	@ManagedAttribute
	public long getHeapUsed() {
		return heapSupplier.getAsLong();
	}

	@ManagedAttribute
	public long getHeapMax() {
		return maxHeapSupplier.getAsLong();
	}

	@ManagedAttribute
	public long getHeapUsedOverMax() {
		return getHeapRatio(maxHeapSupplier.getAsLong());
	}

	@ManagedOperation
	@Override
	public boolean getHeapIsTooHigh() {
		long maxHeap = maxHeapSupplier.getAsLong();

		if (maxHeap <= 0L) {
			LOGGER.debug("We received {} as maxHeap", maxHeap);
			return false;
		}

		long currentPercent = getHeapRatio(maxHeap);

		return currentPercent <= ratioForTooHigh.get();
	}

	protected long getHeapRatio(long maxHeap) {
		return LongMath.saturatedMultiply(heapSupplier.getAsLong(), HUNDRED) / maxHeap;
	}

}
