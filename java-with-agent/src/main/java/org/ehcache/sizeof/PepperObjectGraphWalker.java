/**
 * The MIT License
 * Copyright (c) 2024 Benoit Lacelle - SOLVEN
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

import static java.util.Collections.newSetFromMap;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;

import org.ehcache.sizeof.filters.SizeOfFilter;
import org.ehcache.sizeof.util.WeakIdentityConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will walk an object graph and let you execute some "function" along the way
 *
 * @author Alex Snaps
 */
// https://github.com/ehcache/sizeof/blob/master/src/main/java/org/ehcache/sizeof/ObjectGraphWalker.java
// HACK Set public
@SuppressWarnings({ "PMD.GodClass",
		"PMD.AvoidThrowingNullPointerException",
		"PMD.CognitiveComplexity",
		"PMD.NPathComplexity",
		"PMD.InsufficientStringBufferDeclaration",
		"PMD.AppendCharacterWithChar",
		"PMD.NullAssignment",
		"PMD.AvoidAccessibilityAlteration",
		"PMD.BooleanGetMethodName",
		"PMD.UseLocaleWithCaseConversions" })
public final class PepperObjectGraphWalker {

	private static final Logger LOG = LoggerFactory.getLogger(PepperObjectGraphWalker.class);
	private static final String VERBOSE_DEBUG_LOGGING = "org.ehcache.sizeof.verboseDebugLogging";
	private static final boolean USE_VERBOSE_DEBUG_LOGGING;

	private final WeakIdentityConcurrentMap<Class<?>, SoftReference<Collection<Field>>> fieldCache =
			new WeakIdentityConcurrentMap<>();
	private final WeakIdentityConcurrentMap<Class<?>, Boolean> classCache = new WeakIdentityConcurrentMap<>();

	private final boolean bypassFlyweight;
	private final SizeOfFilter sizeOfFilter;

	private final Visitor visitor;

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
	// HACK Set public
	public PepperObjectGraphWalker(Visitor visitor, SizeOfFilter filter, final boolean bypassFlyweight) {
		if (visitor == null) {
			throw new NullPointerException("Visitor can't be null");
		}
		if (filter == null) {
			throw new NullPointerException("SizeOfFilter can't be null");
		}
		this.visitor = visitor;
		this.sizeOfFilter = filter;
		this.bypassFlyweight = bypassFlyweight;
	}

	private static boolean getVerboseSizeOfDebugLogging() {

		String verboseString = System.getProperty(VERBOSE_DEBUG_LOGGING, "false").toLowerCase();

		return "true".equals(verboseString);
	}

	/**
	 * The visitor to execute the function on each node of the graph This is only to be used for the sizing of an object
	 * graph in memory!
	 */
	// HACK Public
	public interface Visitor {
		/**
		 * The visit method executed on each node
		 *
		 * @param object
		 *            the reference at that node
		 * @return a long for you to do things with...
		 */
		long visit(Object object);
	}

	/**
	 * Walk the graph and call into the "visitor"
	 *
	 * @param root
	 *            the roots of the objects (a shared graph will only be visited once)
	 * @return the sum of all Visitor#visit returned values
	 */
	// HACK Public
	public long walk(Object... root) {
		return walk(null, root);
	}

	/**
	 * Walk the graph and call into the "visitor"
	 *
	 * @param visitorListener
	 *            A decorator for the Visitor
	 * @param root
	 *            the roots of the objects (a shared graph will only be visited once)
	 * @return the sum of all Visitor#visit returned values
	 */
	// HACK Public
	public long walk(VisitorListener visitorListener, Object... root) {
		final StringBuilder traversalDebugMessage;
		if (USE_VERBOSE_DEBUG_LOGGING && LOG.isDebugEnabled()) {
			traversalDebugMessage = new StringBuilder();
		} else {
			traversalDebugMessage = null;
		}
		long result = 0;
		Deque<Object> toVisit = new ArrayDeque<>();
		Set<Object> visited = newSetFromMap(new IdentityHashMap<>());

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

			if (visited.add(ref)) {
				Class<?> refClass = ref.getClass();
				if (!byPassIfFlyweight(ref) && shouldWalkClass(refClass)) {
					if (refClass.isArray() && !refClass.getComponentType().isPrimitive()) {
						for (int i = 0; i < Array.getLength(ref); i++) {
							nullSafeAdd(toVisit, Array.get(ref, i));
						}
					} else {
						for (Field field : getFilteredFields(refClass)) {
							try {
								nullSafeAdd(toVisit, field.get(ref));
							} catch (IllegalAccessException ex) {
								throw new RuntimeException(ex);
							}
						}
					}

					final long visitSize = visitor.visit(ref);
					if (visitorListener != null) {
						visitorListener.visited(ref, visitSize);
					}
					if (traversalDebugMessage != null) {
						traversalDebugMessage.append("  ")
								.append(visitSize)
								.append("b\t\t")
								.append(ref.getClass().getName())
								.append("@")
								.append(System.identityHashCode(ref))
								.append("\n");
					}
					result += visitSize;
				} else if (traversalDebugMessage != null) {
					traversalDebugMessage.append("  ignored\t")
							.append(ref.getClass().getName())
							.append("@")
							.append(System.identityHashCode(ref))
							.append("\n");
				}
			}
		}

		if (traversalDebugMessage != null) {
			traversalDebugMessage.append("Total size: ").append(result).append(" bytes\n");
			LOG.debug(traversalDebugMessage.toString());
		}
		return result;
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
		Collection<Field> fieldList;
		if (ref != null) {
			fieldList = ref.get();
		} else {
			fieldList = null;
		}
		if (fieldList != null) {
			return fieldList;
		} else {
			Collection<Field> result;
			result = sizeOfFilter.filterFields(refClass, getAllFields(refClass));
			if (USE_VERBOSE_DEBUG_LOGGING && LOG.isDebugEnabled()) {
				for (Field field : result) {
					if (Modifier.isTransient(field.getModifiers())) {
						LOG.debug("SizeOf engine walking transient field '{}' of class {}",
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

	private static void nullSafeAdd(final Deque<Object> toVisit, final Object o) {
		if (o != null) {
			toVisit.push(o);
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
						LOG.error("Security settings prevent Ehcache from accessing the subgraph beneath '{}'"
								+ " - cache sizes may be underestimated as a result", field, e);
						continue;
					} catch (RuntimeException e) {
						LOG.warn("The JVM is preventing Ehcache from accessing the subgraph beneath '{}'"
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