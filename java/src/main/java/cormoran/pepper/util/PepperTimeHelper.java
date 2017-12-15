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
package cormoran.pepper.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;

/**
 * Some utility methods relative to time, typically for pretty-printing of performance
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperTimeHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperTimeHelper.class);

	/**
	 * How many times occurrences has to be captured before logging about an outlier?
	 */
	protected static final long NB_OCCURENCES_FOR_INFO = 1000;

	@VisibleForTesting
	public static final AtomicLong NB_LOG_FOR_OUTLIER = new AtomicLong();

	protected PepperTimeHelper() {
		// hidden
	}

	/**
	 * This class enables logging of outlier detection, typically when a small operation takes more time than usual, or
	 * a result size is bigger
	 * 
	 * @return true if maxTiming has been updated
	 */
	public static boolean updateOutlierDetectorStatistics(AtomicLong nb,
			AtomicLong max,
			long newValue,
			Object className,
			Object methodName,
			Object... more) {
		nb.incrementAndGet();

		// How long did this operation lasted
		long currentMax = max.get();
		if (newValue > currentMax) {
			// We encountered a new max time
			if (max.compareAndSet(currentMax, newValue)) {
				// We start logging only after encountered at least N operations, preventing to log too much during
				// start-up
				if (nb.get() > NB_OCCURENCES_FOR_INFO) {
					NB_LOG_FOR_OUTLIER.incrementAndGet();

					if (more != null && more.length > 0) {
						LOGGER.info("{}.{}.{} increased its max from {} to {}",
								className,
								methodName,
								Arrays.asList(more),
								currentMax,
								newValue);
					} else {
						LOGGER.info("{}.{} increased its max from {} to {}", className, methodName, newValue);
					}
				}
			} else {
				LOGGER.trace("Race-condition. We may have lost a max");
			}
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 
	 * @param localDateTime
	 * @return the number of milliseconds since 1970
	 */
	public static long java8ToMillis(LocalDateTime localDateTime) {
		return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
	}

	public static Date toDate(LocalDateTime ldt) {
		return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
	}
}
