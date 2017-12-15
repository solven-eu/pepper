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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.eventbus.EventBus;

/**
 * Parent class for events to be published in an {@link EventBus}. They will be handled by
 * {@link PepperActiveTasksMonitor}
 * 
 * @author Benoit Lacelle
 * 
 */
public class AMetricEvent {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AMetricEvent.class);

	// This UUID is constant through the whole application lifecycle. It can be
	// used to seggregate events from different application runs
	public static final String INSTANCE_UUID = UUID.randomUUID().toString();

	private static final AtomicLong EVENT_INCREMENTER = new AtomicLong();

	// This id is unique amongst a given INSTANCE_UUID
	public final long eventId = EVENT_INCREMENTER.getAndIncrement();

	/**
	 * Ability to retrieve all encountered source classes, and then monitor available events
	 */
	protected static final Set<Class<?>> SOURCE_CLASSES = Sets.newConcurrentHashSet();

	public final Object source;
	public final List<? extends String> names;

	public static Set<Class<?>> getEncounteredSourceClasses() {
		return ImmutableSet.copyOf(SOURCE_CLASSES);
	}

	public AMetricEvent(Object source, List<? extends String> names) {
		this.source = Objects.requireNonNull(source);
		if (names == null) {
			this.names = Collections.emptyList();
		} else {
			this.names = names;
		}

		SOURCE_CLASSES.add(source.getClass());
	}

	public AMetricEvent(Object source, String firstName, String... otherNames) {
		this(source, Lists.asList(firstName, otherNames));
	}

	public static void post(Consumer<? super AMetricEvent> eventBus, AMetricEvent simpleEvent) {
		if (eventBus == null) {
			logNoEventBus(simpleEvent.source, simpleEvent.names);
		} else {
			eventBus.accept(simpleEvent);
		}
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("names", names).add("source", source).toString();
	}

	protected static void logNoEventBus(Object source, List<?> names) {
		LOGGER.info("No EventBus has been injected for {} on {}", names, source);
	}

	public static Set<Class<?>> getSourceClasses() {
		return Collections.unmodifiableSet(SOURCE_CLASSES);
	}
}
