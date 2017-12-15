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

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link TaskEndEvent} should be published to clese a previous {@link TaskStartEvent}
 * 
 * @author Benoit Lacelle
 * 
 */
public class TaskEndEvent implements ITaskActivityEvent {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TaskEndEvent.class);

	public static final String KEY_RESULT_SIZE = "resultSize";
	public final TaskStartEvent startEvent;

	/**
	 * Generally called by EndMetricEvent.post
	 * 
	 * @param startEvent
	 */
	protected TaskEndEvent(TaskStartEvent startEvent) {
		this.startEvent = Objects.requireNonNull(startEvent);
	}

	/**
	 * return now minus startTime in milliseconds
	 */
	public long durationInMs() {
		return System.currentTimeMillis() - startEvent.startTime;
	}

	public static TaskEndEvent postEndEvent(Consumer<? super ITaskActivityEvent> eventBus, TaskStartEvent startEvent) {
		if (startEvent == null) {
			LOGGER.info("No StartMetricEvent has been provided");
			return null;
		} else {
			TaskEndEvent endMetricEvent = buildEndEvent(startEvent);
			if (startEvent.endMetricEvent.get() == endMetricEvent) {
				// This is the first endMetricEvent
				TaskStartEvent.post(eventBus, endMetricEvent);
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
	public static TaskEndEvent buildEndEvent(TaskStartEvent start) {
		TaskEndEvent endEvent = new TaskEndEvent(start);
		start.registerEndEvent(endEvent);
		return endEvent;
	}

	@Override
	public Object getSource() {
		return startEvent.getSource();
	}

	@Override
	public List<? extends String> getNames() {
		return startEvent.getNames();
	}
}
