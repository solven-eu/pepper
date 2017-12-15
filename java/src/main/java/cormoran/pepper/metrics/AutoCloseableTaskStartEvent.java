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

/**
 * Help usage of StartMetricEvent with try-finally blocks
 * 
 * @author Benoit Lacelle
 *
 */
public class AutoCloseableTaskStartEvent implements AutoCloseable, ITaskActivityEvent {
	protected final TaskStartEvent decorated;
	protected final Consumer<? super ITaskActivityEvent> eventBus;

	public AutoCloseableTaskStartEvent(TaskStartEvent startEvent, Consumer<? super ITaskActivityEvent> eventBus) {
		Objects.requireNonNull(startEvent);
		this.decorated = startEvent;
		this.eventBus = eventBus;
	}

	@Override
	public void close() {
		// Will be handled by ApexMetricsTowerControl.onEndEvent(EndMetricEvent)
		TaskEndEvent.postEndEvent(eventBus, decorated);
	}

	public TaskStartEvent getStartEvent() {
		return decorated;
	}

	@Override
	public Object getSource() {
		return decorated.getSource();
	}

	@Override
	public List<?> getNames() {
		return decorated.getNames();
	}

}
