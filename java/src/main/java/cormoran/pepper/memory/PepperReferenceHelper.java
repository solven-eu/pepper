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
package cormoran.pepper.memory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.AtomicLongMap;

/**
 * Various utilities related to references
 * 
 * @author Benoit Lacelle
 *
 */
// TODO The dictionary should be maintain in a dedicated data-structure
@Beta
public class PepperReferenceHelper {

	private static final long HUNDRED = 100L;

	private static final int NB_ALLOWED_RESET = 10;

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperReferenceHelper.class);

	// TODO: Enable a user adding its own safe classes
	// Add class guaranteed to be immutable and expected to have a high-level of redundancy
	private static final Set<Class<?>> CLASS_TO_DICTIONARIZE = ImmutableSet.of(String.class,
			java.time.LocalDate.class,
			java.time.LocalDateTime.class,
			java.time.OffsetDateTime.class,
			java.time.LocalTime.class,
			java.lang.Integer.class,
			java.lang.Long.class,
			java.lang.Float.class,
			java.lang.Double.class);

	// Map each class requested for interning, and its eligible Fields
	private static final ConcurrentMap<Class<?>, List<Field>> CLASS_TO_ELECTED_FIELDS = new ConcurrentHashMap<>();

	// We use a Map to enable easy addAndGet
	@VisibleForTesting
	static final ConcurrentMap<Field, ConcurrentMap<Object, Object>> DICTIONARY_FIELDS = new ConcurrentHashMap<>();

	@VisibleForTesting
	static final ConcurrentMap<Class<?>, ConcurrentMap<Object, Object>> DICTIONARY_ARRAY = new ConcurrentHashMap<>();

	private static final AtomicLongMap<Field> FIELD_TO_CACHE_HIT = AtomicLongMap.create();
	private static final AtomicLongMap<Class<?>> CLASS_TO_CACHE_HIT = AtomicLongMap.create();

	private static final AtomicLongMap<Field> FIELD_TO_NB_RESET = AtomicLongMap.create();

	protected PepperReferenceHelper() {
		// hidden
	}

	/**
	 * Clear all data-structures used to maintain an inner set of references
	 */
	public static void clear() {
		CLASS_TO_ELECTED_FIELDS.clear();
		DICTIONARY_FIELDS.clear();
		FIELD_TO_CACHE_HIT.clear();
		FIELD_TO_NB_RESET.clear();

		DICTIONARY_ARRAY.clear();
		CLASS_TO_CACHE_HIT.clear();
	}

	/**
	 * This method will replace fields of given object with reference used by other objects. It is typically useful to
	 * POJO which, except the spaceKey, have low cardinality. It also handles Object arrays (Object[])
	 * 
	 * @param data
	 */
	public static void internalizeFields(Object data) {
		if (data == null) {
			return;
		} else if (data instanceof Object[]) {
			internalizeArray((Object[]) data);
		} else {
			Class<?> clazz = data.getClass();

			List<Field> fields = computeDictionarizableFields(clazz);

			if (fields.isEmpty()) {
				// This class has no field to dictionarize: quite early to skip Iterator instantiation
				return;
			}

			for (Field f : fields) {
				internFieldsFieldValue(f, data);
			}
		}
	}

	/**
	 * 
	 * @param field
	 * @param object
	 */
	protected static void internFieldsFieldValue(Field field, Object object) {
		if (field == null || object == null) {
			return;
		}

		ConcurrentMap<Object, Object> fieldDictionary =
				DICTIONARY_FIELDS.computeIfAbsent(field, k -> new ConcurrentHashMap<Object, Object>());

		try {
			// Retrieve the object currently referenced by the object to inflate
			Object currentRef = field.get(object);

			if (currentRef != null) {
				// Try to find an existing object equal to the new object
				Object existingRef = fieldDictionary.putIfAbsent(currentRef, currentRef);
				if (existingRef != null && existingRef != currentRef) {
					// We have found an existing equals object, but with a different ref: share the existing ref
					field.set(object, existingRef);
				} else {
					// Either existingRef == null or existingRef == currentRef
					if (existingRef == null) {
						// fieldDictionary has grown
						if (shouldCheckDictionary(field, fieldDictionary)) {
							// More than 1000 different values and less than 1" of cache-hit: the known values seems
							// useless
							if (FIELD_TO_NB_RESET.incrementAndGet(field) < NB_ALLOWED_RESET) {
								// clear the dictionary hoping the next encountered values will be more relevant
								fieldDictionary.clear();
							} else {
								// We have cleared the dictionary 10 times: stop considering this field
								LOGGER.warn(
										"Stop dictionarizing " + field + " as it seems to have very-high cardinality");
								stopDictionarizing(object.getClass(), field);
							}
						}
					} else {
						FIELD_TO_CACHE_HIT.incrementAndGet(field);
					}

				}
			}

		} catch (IllegalArgumentException e) {
			stopDictionarizing(object.getClass(), field);
		} catch (IllegalAccessException e) {
			stopDictionarizing(object.getClass(), field);
		}
	}

	protected static boolean shouldCheckDictionary(Field field, Map<?, ?> fieldDictionary) {
		int dSize = fieldDictionary.size();
		return dSize > IPepperMemoryConstants.KB_INT && FIELD_TO_CACHE_HIT.get(field) * HUNDRED < dSize;
	}

	protected static boolean shouldCheckDictionary(Class<?> clazz, Map<?, ?> clazzDictionary) {
		int dSize = clazzDictionary.size();
		return dSize > IPepperMemoryConstants.KB_INT && CLASS_TO_CACHE_HIT.get(clazz) * HUNDRED < dSize;
	}

	protected static void stopDictionarizing(Class<?> clazz, Field field) {
		if (clazz == null || field == null) {
			return;
		}

		// This field is not candidate for dictionarisation anymore
		List<Field> electedFields = CLASS_TO_ELECTED_FIELDS.get(clazz);
		if (electedFields != null) {
			// Might be null if CLASS_TO_ELECTED_FIELDS cleared concurrently
			electedFields.remove(field);
		}
		DICTIONARY_FIELDS.remove(field);
	}

	protected static List<Field> computeDictionarizableFields(Class<?> clazz) {
		List<Field> fields = CLASS_TO_ELECTED_FIELDS.get(clazz);

		if (fields != null) {
			// Already computed
			return fields;
		}

		// First encounter of given class
		// CopyOnWriteArrayList as some Field may be removed later
		final List<Field> preparingFields = new CopyOnWriteArrayList<Field>();

		ReflectionUtils.doWithFields(clazz, field -> {
			// Make accessible as we will read and write it
			ReflectionUtils.makeAccessible(field);

			preparingFields.add(field);
		}, field -> {
			if (Modifier.isStatic(field.getModifiers())) {
				// Do not touch static fields
				return false;
			}

			if (!CLASS_TO_DICTIONARIZE.contains(field.getType())) {
				LOGGER.debug("Do not ");
				return false;
			}

			return true;
		});

		CLASS_TO_ELECTED_FIELDS.putIfAbsent(clazz, preparingFields);

		// handle concurrent fields computations
		return CLASS_TO_ELECTED_FIELDS.get(clazz);
	}

	public static <T> void internalizeArray(T[] array) {
		if (array == null) {
			return;
		}

		for (int i = 0; i < array.length; i++) {
			T item = array[i];
			if (item != null) {
				T newRef = internalize(item);

				if (newRef != item) {
					// Replace the reference as an equals object has been referenced before
					array[i] = newRef;
				}
			}
		}
	}

	/**
	 * 
	 * @param item
	 * @return the input item after having internalized its fields
	 */
	public static <T> T internalize(T item) {
		if (item == null) {
			return null;
		} else if (item instanceof Object[]) {
			internalizeArray((Object[]) item);

			return item;
		} else {
			Class<?> clazz = item.getClass();

			if (CLASS_TO_DICTIONARIZE.contains(clazz)) {
				return internalizeRef(item);
			} else {
				internalizeFields(item);

				return item;
			}
		}
	}

	public static <T> T internalizeRef(T item) {
		if (item == null) {
			return null;
		}

		Class<?> clazz = item.getClass();

		@SuppressWarnings("unchecked")
		ConcurrentMap<T, T> d =
				(ConcurrentMap<T, T>) DICTIONARY_ARRAY.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());

		T previous = d.putIfAbsent(item, item);

		if (previous == null) {
			if (shouldCheckDictionary(clazz, d)) {
				// clear the dictionary hoping the next encountered values will be more relevant
				d.clear();
			}

			// New ref
			return item;
		} else {
			CLASS_TO_CACHE_HIT.incrementAndGet(clazz);

			return previous;
		}
	}

	public static void dictionarizeIterable(Iterable<?> iterable) {
		if (iterable == null) {
			return;
		}

		for (Object item : iterable) {
			internalizeFields(item);
		}
	}
}
