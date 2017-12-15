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
package cormoran.pepper.core.metrics;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * An {@link EndMetricEvent} should be published to clese a previous {@link StartMetricEvent}
 * 
 * @author Benoit Lacelle
 * 
 */
public class EndMetricEvent extends AMetricEvent {
	public static final String KEY_RESULT_SIZE = "resultSize";
	public final StartMetricEvent startEvent;

	/**
	 * Generally called by EndMetricEvent.post
	 * 
	 * @param startEvent
	 */
	protected EndMetricEvent(StartMetricEvent startEvent) {
		super(startEvent.source, startEvent.names);
		this.startEvent = Objects.requireNonNull(startEvent);
	}

	/**
	 * return now minus startTime in milliseconds
	 */
	public long durationInMs() {
		return System.currentTimeMillis() - startEvent.startTime;
	}

	public static EndMetricEvent postEndEvent(Consumer<? super AMetricEvent> eventBus, StartMetricEvent startEvent) {
		if (startEvent == null) {
			LOGGER.info("No StartMetricEvent has been provided");
			return null;
		} else {
			EndMetricEvent endMetricEvent = buildEndEvent(startEvent);
			if (startEvent.endMetricEvent.get() == endMetricEvent) {
				// This is the first endMetricEvent
				post(eventBus, endMetricEvent);
				return endMetricEvent;
			} else {
				return startEvent.endMetricEvent.get();
			}
		}
	}

	/**
	 * 
	 * @param start
	 * @return a new EndMetricEvent instance. We try to attach it as end event for StartMetricEvent, which would fail if
	 *         another EndEvent has already been attached
	 */
	public static EndMetricEvent buildEndEvent(StartMetricEvent start) {
		EndMetricEvent endEvent = new EndMetricEvent(start);
		start.registerEndEvent(endEvent);
		return endEvent;
	}
}
