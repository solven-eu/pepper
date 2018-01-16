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
package org.ehcache.sizeof;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.ehcache.sizeof.ObjectGraphWalker.Visitor;
import org.ehcache.sizeof.filters.SizeOfFilter;
import org.ehcache.sizeof.util.WeakIdentityConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AtomicLongMap;

/**
 * This will walk an object graph and let you execute some "function" along the way
 *
 * @author Alex Snaps
 */
// https://raw.githubusercontent.com/ehcache/sizeof/master/src/main/java/org/ehcache/sizeof/ObjectGraphWalker.java
// 113b412 on 5 Oct
// FlyweightType is package-friendly
public final class PepperObjectGraphWalker {

	private static final Logger LOGGER = LoggerFactory.getLogger(PepperObjectGraphWalker.class);
	private static final String VERBOSE_DEBUG_LOGGING = "org.ehcache.sizeof.verboseDebugLogging";

	private static final boolean USE_VERBOSE_DEBUG_LOGGING;

	private final WeakIdentityConcurrentMap<Class<?>, SoftReference<Collection<Field>>> fieldCache =
			new WeakIdentityConcurrentMap<>();
	private final WeakIdentityConcurrentMap<Class<?>, Boolean> classCache = new WeakIdentityConcurrentMap<>();

	private final boolean bypassFlyweight;
	private final SizeOfFilter sizeOfFilter;

	private static final ConcurrentMap<Field, Map<Object, Object>> FIELD_TO_REFERENCES = new ConcurrentHashMap<>();
	private static final AtomicLongMap<Field> FIELD_TO_CACHE_HIT = AtomicLongMap.create();
	private static final AtomicLongMap<Field> FIELD_TO_CACHE_MISS = AtomicLongMap.create();
	private static final ConcurrentMap<Class<?>, Map<Object, Object>> ARRAY_COMPONENT_TO_REFERENCES =
			new ConcurrentHashMap<>();
	private static final AtomicLongMap<Class<?>> ARRAY_COMPONENT_TO_CACHE_HIT = AtomicLongMap.create();
	private static final AtomicLongMap<Class<?>> ARRAY_COMPONENT_TO_CACHE_MISS = AtomicLongMap.create();

	static {
		USE_VERBOSE_DEBUG_LOGGING = getVerboseSizeOfDebugLogging();
	}

	/**
	 * Constructor
	 *
	 * @param visitor
	 *            the visitor to use
	 * @param filter
	 *            the filtering
	 * @param bypassFlyweight
	 *            the filtering
	 * @see Visitor
	 * @see SizeOfFilter
	 */
	public PepperObjectGraphWalker(SizeOfFilter filter, final boolean bypassFlyweight) {
		if (filter == null) {
			throw new NullPointerException("SizeOfFilter can't be null");
		}
		this.sizeOfFilter = filter;
		this.bypassFlyweight = bypassFlyweight;
	}

	private static boolean getVerboseSizeOfDebugLogging() {

		String verboseString = System.getProperty(VERBOSE_DEBUG_LOGGING, "false").toLowerCase();

		return verboseString.equals("true");
	}

	/**
	 * Walk the graph
	 *
	 * @param root
	 *            the roots of the objects (a shared graph will only be visited once)
	 * @return the sum of all Visitor#visit returned values
	 */
	public void walk(Object... root) {
		final StringBuilder traversalDebugMessage;
		if (USE_VERBOSE_DEBUG_LOGGING && LOGGER.isDebugEnabled()) {
			traversalDebugMessage = new StringBuilder();
		} else {
			traversalDebugMessage = null;
		}

		Deque<Object> toVisit = new ArrayDeque<>();
		IdentityHashMap<Object, Object> visited = new IdentityHashMap<>();

		if (root != null) {
			if (traversalDebugMessage != null) {
				traversalDebugMessage.append("visiting ");
			}
			for (Object object : root) {
				nullSafeAdd(toVisit, object);
				if (traversalDebugMessage != null && object != null) {
					traversalDebugMessage.append(object.getClass().getName())
							.append("@")
							.append(System.identityHashCode(object))
							.append(", ");
				}
			}
			if (traversalDebugMessage != null) {
				traversalDebugMessage.deleteCharAt(traversalDebugMessage.length() - 2).append("\n");
			}
		}

		while (!toVisit.isEmpty()) {
			Object ref = toVisit.pop();

			if (visited.containsKey(ref)) {
				// This object has already been processed
				continue;
			}

			Class<?> refClass = ref.getClass();
			if (!byPassIfFlyweight(ref) && shouldWalkClass(refClass)) {
				if (refClass.isArray() && !refClass.getComponentType().isPrimitive()) {
					for (int i = 0; i < Array.getLength(ref); i++) {
						Object referred = Array.get(ref, i);
						if (nullSafeAdd(toVisit, referred)) {
							Class<?> component = refClass.getComponentType();

							Map<Object, Object> internalized = ARRAY_COMPONENT_TO_REFERENCES.computeIfAbsent(component,
									f -> new ConcurrentHashMap<>());

							if (internalized == Collections.EMPTY_MAP) {
								// Field rejected for internalization
								LOGGER.trace("Rejected");
							} else {
								Object validInternalized = internalized.putIfAbsent(referred, referred);
								if (validInternalized != null) {
									// There is already an equal Object in the internalization cache
									Array.set(ref, i, validInternalized);
									ARRAY_COMPONENT_TO_CACHE_HIT.incrementAndGet(component);
								} else {
									long miss = ARRAY_COMPONENT_TO_CACHE_MISS.incrementAndGet(component);
									if (miss > 1000) {
										// TODO: Check if we should stop considering this field
										LOGGER.trace("Consider me");
									}
								}
							}
						}
					}
				} else {
					for (Field field : getFilteredFields(refClass)) {
						try {
							Object referred = field.get(ref);
							if (nullSafeAdd(toVisit, referred)) {
								Map<Object, Object> internalized =
										FIELD_TO_REFERENCES.computeIfAbsent(field, f -> new ConcurrentHashMap<>());

								if (internalized == Collections.EMPTY_MAP) {
									// Field rejected for internalization
									LOGGER.trace("Rejected");
								} else {
									Object validInternalized = internalized.putIfAbsent(referred, referred);
									if (validInternalized != null) {
										// There is already an equal Object in the internalization cache
										field.set(ref, validInternalized);
										FIELD_TO_CACHE_HIT.incrementAndGet(field);
									} else {
										long miss = FIELD_TO_CACHE_MISS.incrementAndGet(field);
										if (miss > 1000) {
											// TODO: Check if we should stop considering this field
											LOGGER.trace("Consider me");
										}
									}
								}
							}
						} catch (IllegalAccessException ex) {
							throw new RuntimeException(ex);
						}
					}
				}

				if (traversalDebugMessage != null) {
					traversalDebugMessage.append(ref.getClass().getName())
							.append("@")
							.append(System.identityHashCode(ref))
							.append("\n");
				}
			} else if (traversalDebugMessage != null) {
				traversalDebugMessage.append("  ignored\t")
						.append(ref.getClass().getName())
						.append("@")
						.append(System.identityHashCode(ref))
						.append("\n");
			}
			visited.put(ref, null);
		}

		if (traversalDebugMessage != null) {
			LOGGER.debug(traversalDebugMessage.toString());
		}
	}

	/**
	 * Returns the filtered fields for a particular type
	 *
	 * @param refClass
	 *            the type
	 * @return A collection of fields to be visited
	 */
	private Collection<Field> getFilteredFields(Class<?> refClass) {
		SoftReference<Collection<Field>> ref = fieldCache.get(refClass);
		Collection<Field> fieldList = ref != null ? ref.get() : null;
		if (fieldList != null) {
			return fieldList;
		} else {
			Collection<Field> result;
			result = sizeOfFilter.filterFields(refClass, getAllFields(refClass));
			if (USE_VERBOSE_DEBUG_LOGGING && LOGGER.isDebugEnabled()) {
				for (Field field : result) {
					if (Modifier.isTransient(field.getModifiers())) {
						LOGGER.debug("SizeOf engine walking transient field '{}' of class {}",
								field.getName(),
								refClass.getName());
					}
				}
			}
			fieldCache.put(refClass, new SoftReference<>(result));
			return result;
		}
	}

	private boolean shouldWalkClass(Class<?> refClass) {
		Boolean cached = classCache.get(refClass);
		if (cached == null) {
			cached = sizeOfFilter.filterClass(refClass);
			classCache.put(refClass, cached);
		}
		return cached;
	}

	private static boolean nullSafeAdd(final Deque<Object> toVisit, final Object o) {
		if (o != null) {
			toVisit.push(o);

			// TODO: Should we check if the ref have already been processed? It would prevent the Deque from growing but
			// would require more IdentityHashMap.get
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Returns all non-primitive fields for the entire class hierarchy of a type
	 *
	 * @param refClass
	 *            the type
	 * @return all fields for that type
	 */
	private static Collection<Field> getAllFields(Class<?> refClass) {
		Collection<Field> fields = new ArrayList<>();
		for (Class<?> klazz = refClass; klazz != null; klazz = klazz.getSuperclass()) {
			for (Field field : klazz.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()) && !field.getType().isPrimitive()) {
					try {
						field.setAccessible(true);
					} catch (SecurityException e) {
						LOGGER.error("Security settings prevent Ehcache from accessing the subgraph beneath '{}'"
								+ " - cache sizes may be underestimated as a result", field, e);
						continue;
					}
					fields.add(field);
				}
			}
		}
		return fields;
	}

	private boolean byPassIfFlyweight(Object obj) {
		if (bypassFlyweight) {
			FlyweightType type = FlyweightType.getFlyweightType(obj.getClass());
			return type != null && type.isShared(obj);
		}
		return false;
	}

}