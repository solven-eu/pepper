/**
 * Copyright (C) 2014 Benoit Lacelle (benoit.lacelle@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cormoran.pepper.jvm;

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
