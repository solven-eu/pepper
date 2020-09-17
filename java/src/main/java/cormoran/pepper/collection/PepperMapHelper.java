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
package cormoran.pepper.collection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import cormoran.pepper.logging.PepperLogHelper;

/**
 * Various helpers for Map
 * 
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings({ "PMD.GodClass", "PMD.AvoidDuplicateLiterals" })
public class PepperMapHelper {
	protected PepperMapHelper() {
		// hidden
	}

	public static <K1, V, K2 extends K1> Map<K1, V> transcodeColumns(BiMap<?, ? extends K2> mapping, Map<K1, V> map) {
		// https://stackoverflow.com/questions/24630963/java-8-nullpointerexception-in-collectors-tomap
		return map.entrySet().stream().collect(LinkedHashMap::new, (m, e) -> {
			K1 newKey;
			{

				if (mapping.containsKey(e.getKey())) {
					newKey = mapping.get(e.getKey());
				} else {
					newKey = e.getKey();
				}
			}
			m.put(newKey, e.getValue());
		}, Map::putAll);
	}

	public static <K, V> Map<K, V> fromLists(List<? extends K> keys, List<? extends V> values) {
		Objects.requireNonNull(keys);
		Objects.requireNonNull(values);

		if (keys.size() != values.size()) {
			throw new RuntimeException(keys.size() + " keys but " + values.size() + " values");
		}

		ImmutableMap.Builder<K, V> builder = ImmutableMap.<K, V>builder();

		for (int i = 0; i < keys.size(); i++) {
			builder.put(keys.get(i), values.get(i));
		}

		return builder.build();
	}

	/**
	 * 
	 * @param first
	 *            the first Map to resolve
	 * @param second
	 *            the second Map to resolve
	 * @return an Immutable Map where each .get is resolved by the first Map holding a non-null value for given key
	 */
	public static <K, V> Map<K, V> decoratePutAll(Map<? extends K, ? extends V> first,
			Map<? extends K, ? extends V> second) {
		Map<K, V> clone = new LinkedHashMap<>(second);

		// Give priority to first
		clone.putAll(first);

		return Collections.unmodifiableMap(clone);
	}

	/**
	 * 
	 * @param <T>
	 * @param body
	 * @param firstKey
	 * @param moreKeys
	 * @return the object mapped from body (generally a {@link Map}) following the chain of keys. This returns null if
	 *         the path leads to nothing. This fails if the path is invalid given the {@link Map} (e.g. path is 'k1.k2'
	 *         while the {@link Map} is ('k1': 'v1'))
	 */
	public static <T> T getAs(Object body, Object firstKey, Object... moreKeys) {
		if (body == null) {
			return null;
		}

		Object value = body;

		List<Object> allKeys = Lists.asList(firstKey, moreKeys);
		for (Object key : allKeys) {
			if (value == null) {
				return null;
			} else if (value instanceof Map<?, ?>) {
				value = ((Map<?, ?>) value).get(key);
			} else {
				throw new IllegalArgumentException("Can not process keys " + allKeys
						+ " on Map: "
						+ body
						+ " as value for key="
						+ key
						+ " is: "
						+ PepperLogHelper.getObjectAndClass(value));
			}
		}

		return (T) value;
	}

	/**
	 * This helps building deeply nested Maps. It is especially useful for unit-tests.
	 * 
	 * @param value
	 *            attached to the deepest key
	 * @param firstKey
	 *            a first required key
	 * @param secondKey
	 *            a second required key
	 * @param moreKeys
	 *            more optional keys
	 * @return a Map looking like: {'k1': {'k2': 'v'}}
	 */
	public static <T, U> Map<T, Map<U, ?>> imbricatedMap(Object value, T firstKey, U secondKey, Object... moreKeys) {
		Object nextValue = value;
		for (int i = moreKeys.length - 1; i >= 0; i--) {
			nextValue = Collections.singletonMap(moreKeys[i], nextValue);
		}

		Map<U, ?> subMap = Collections.singletonMap(secondKey, nextValue);

		return Collections.singletonMap(firstKey, subMap);
	}

	public static Optional<String> getOptionalString(Map<?, ?> map, Object firstKey, Object... moreKeys) {
		Object value = getAs(map, firstKey, moreKeys);

		if (value == null) {
			return Optional.empty();
		} else if (value instanceof String) {
			return Optional.of(value.toString());
		} else {
			throw new IllegalArgumentException("keys=" + Lists.asList(firstKey, moreKeys)
					+ " led to not-a-string: "
					+ PepperLogHelper.getObjectAndClass(value));
		}
	}

	public static String checkNonNullString(Object keyName, Object value) {
		if (value == null) {
			throw new IllegalArgumentException("'" + keyName + "' is required but null");
		} else if (!(value instanceof String)) {
			throw new IllegalArgumentException("'" + keyName + "' has invalid type: " + value.getClass());
		}

		return (String) value;
	}

	public static <S, T> Map<S, T> getRequiredMap(Map<?, ?> map, Object mainKey, Object... subKeys) {
		List<Object> allKeys = checkNullMap(map, mainKey, subKeys);

		return digForValue(map, allKeys, (currentKey, rawValue) -> {
			if (rawValue instanceof Map<?, ?>) {
				return (Map<S, T>) rawValue;
			} else {
				// Do not throw the value which might be a personal value
				throw new IllegalArgumentException("Not a Map<?,?> (" + rawValue
						.getClass() + ") for " + allKeys + ". Deepest Map<?,?> keys: " + map.keySet());
			}
		});
	}

	public static String getRequiredString(final Map<?, ?> map, Object mainKey, Object... subKeys) {
		List<Object> allKeys = checkNullMap(map, mainKey, subKeys);

		return digForValue(map, allKeys, (currentKey, rawValue) -> {
			if (rawValue instanceof String && Strings.isNullOrEmpty(rawValue.toString())) {
				throw new IllegalArgumentException(
						"We miss '" + currentKey + "' (empty string) amongst available: " + map.keySet());
			}

			return checkNonNullString(currentKey, rawValue);
		});
	}

	public static <T> T getRequiredAs(final Map<?, ?> map, String mainKey, String... subKeys) {
		List<Object> allKeys = checkNullMap(map, mainKey, subKeys);

		return digForValue(map, allKeys, (currentKey, rawValue) -> {
			return (T) rawValue;
		});
	}

	private static <T> T digForValue(final Map<?, ?> map, List<Object> allKeys, BiFunction<Object, Object, T> toValue) {
		Map<?, ?> currentMap = map;
		for (int i = 0; i < allKeys.size(); i++) {
			Object currentKey = allKeys.get(i);
			Object rawValue = currentMap.get(currentKey);
			if (i == allKeys.size() - 1) {
				if (rawValue == null) {
					if (currentMap.containsKey(currentKey)) {
						throw new IllegalArgumentException(
								"We miss '" + currentKey + "' (value is null) amongst available: " + map.keySet());
					} else {
						throw new IllegalArgumentException(
								"We miss '" + currentKey + "' (key not present) amongst available: " + map.keySet());
					}
				}

				return toValue.apply(currentKey, rawValue);
			} else if (rawValue instanceof Map<?, ?>) {
				currentMap = (Map<?, ?>) rawValue;
			} else {
				throw new IllegalArgumentException(
						"We miss '" + currentKey + "' as Map amongst available: " + currentMap.keySet());
			}
		}

		throw new IllegalStateException("Would never happen");
	}

	private static List<Object> checkNullMap(Map<?, ?> map, Object mainKey, Object... subKeys) {
		List<Object> keys = Lists.asList(mainKey, subKeys);
		if (map == null) {
			throw new IllegalArgumentException("Input Map<?,?> is null while requiring keys=" + keys);
		}
		return keys;
	}

	public static Number getRequiredNumber(Map<?, ?> map, String mainKey, String... subKeys) {
		List<Object> allKeys = checkNullMap(map, mainKey, subKeys);

		return digForValue(map, allKeys, (currentKey, rawValue) -> {
			if (rawValue instanceof Number) {
				return (Number) rawValue;
			} else {
				// Do not throw the value which might be a personal value
				throw new IllegalArgumentException("Not a number (" + rawValue
						.getClass() + ") for " + allKeys + ". Deepest Map<?,?> keys: " + map.keySet());
			}
		});
	}

	private static <T> Map<T, ?> hideKey(Map<T, ?> formRegisterApp, Object key) {
		if (formRegisterApp.containsKey(key)) {
			Map<T, ?> clone = new LinkedHashMap<>(formRegisterApp);
			clone.remove(key);
			return ImmutableMap.copyOf(clone);
		} else {
			throw new IllegalArgumentException("Can not hide a not present key: " + key);
		}
	}

	/**
	 * 
	 * @param <T>
	 *            the type of the keys
	 * @param mapToHide
	 *            the Map from which given keys has to be hidden
	 * @param key
	 *            the first key to hide
	 * @param moreKeys
	 *            more keys to Map
	 * @return from a Map like {'k1': 'v1', 'k2': 'v2'}, hiding k1 leads to the map {'k2': 'v2'}
	 */
	@SafeVarargs
	public static <T> Map<T, ?> hideKeys(Map<T, ?> mapToHide, Object key, Object... moreKeys) {
		AtomicReference<Map<T, ?>> hidden = new AtomicReference<>(mapToHide);

		Lists.asList(key, moreKeys).forEach(oneKey -> hidden.set(hideKey(hidden.get(), oneKey)));

		return hidden.get();
	}

	public static <T> void transferValue(T key, Map<? extends T, ?> source, Map<T, Object> output) {
		Object valueToWrite = source.get(key);

		if (valueToWrite != null) {
			output.put(key, valueToWrite);
		}
	}
}
