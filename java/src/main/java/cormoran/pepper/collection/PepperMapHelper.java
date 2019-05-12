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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Various helpers for Map
 * 
 * @author Benoit Lacelle
 *
 */
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

	public static <T> T getAs(Object body, String firstKey, String... moreKeys) {
		Object value = body;

		List<String> allKeys = Lists.asList(firstKey, moreKeys);
		for (String key : allKeys) {
			if (value == null) {
				return null;
			} else if (value instanceof Map<?, ?>) {
				value = ((Map<?, ?>) value).get(key);
			} else {
				throw new IllegalArgumentException("Can not process keys" + allKeys
						+ " on Map: "
						+ body
						+ " as value has type: "
						+ value.getClass().getSimpleName());
			}
		}

		return (T) value;
	}

	public static Map<?, ?> imbricatedMap(Object value, String firstKey, String... moreKeys) {
		for (int i = moreKeys.length - 1; i >= 0; i--) {
			value = Collections.singletonMap(moreKeys[i], value);
		}

		return Collections.singletonMap(firstKey, value);
	}

	public static Map<?, ? extends Map<?, ?>> imbricatedMap2(Object value,
			String firstKey,
			String secondKey,
			String... moreKeys) {
		Map<?, ?> subMap = imbricatedMap(value, secondKey, moreKeys);

		return Collections.singletonMap(firstKey, subMap);
	}

	public static Optional<String> getOptionalString(Map<?, ?> map, String key) {
		if (map == null) {
			throw new IllegalArgumentException("Null Map while requiring key=" + key);
		}

		Object rawValue = map.get(key);

		if (rawValue == null) {
			return Optional.empty();
		} else if (rawValue instanceof String) {
			String rawString = rawValue.toString();
			if (Strings.isNullOrEmpty(rawString)) {
				return Optional.empty();
			} else {
				return Optional.of(rawString);
			}
		} else {
			throw new IllegalArgumentException("We have a not-String value for '" + key + "' in " + map);
		}

	}

	public static String checkNonNullString(String keyName, Object value) {
		if (value == null) {
			throw new IllegalArgumentException("'" + keyName + "' is required but null");
		} else if (!(value instanceof String)) {
			throw new IllegalArgumentException("'" + keyName + "' has invalid type: " + value.getClass());
		}

		return (String) value;
	}

	public static Map<?, ?> getRequiredMap(Map<?, ?> map, String key) {
		if (map == null) {
			throw new IllegalArgumentException("Null Map while requiring key=" + key);
		}

		Object rawValue = map.get(key);

		if (rawValue == null || !(rawValue instanceof Map)) {
			throw new IllegalArgumentException("We miss '" + key + "' amongst available: " + map.keySet());
		}

		return (Map<?, ?>) rawValue;
	}

	public static String getRequiredString(final Map<?, ?> map, String mainKey, String... subKeys) {
		if (map == null) {
			throw new IllegalArgumentException("Null Map while requiring key=" + mainKey);
		}

		List<String> allKeys = Lists.asList(mainKey, subKeys);

		Map<?, ?> currentMap = map;
		for (int i = 0; i < allKeys.size(); i++) {
			String currentKey = allKeys.get(i);
			Object rawValue = currentMap.get(currentKey);
			if (i == allKeys.size() - 1) {
				if (rawValue == null || rawValue instanceof String && Strings.isNullOrEmpty(rawValue.toString())) {
					throw new IllegalArgumentException(
							"We miss '" + currentKey + "' amongst available: " + map.keySet());
				}

				return checkNonNullString(currentKey, rawValue);
			} else {
				if (rawValue == null || !(rawValue instanceof Map<?, ?>)) {
					throw new IllegalArgumentException(
							"We miss '" + currentKey + "' as Map amongst available: " + currentMap.keySet());
				}
				currentMap = (Map<?, ?>) rawValue;
			}
		}

		throw new IllegalStateException("Would never happen");
	}

	public static Number getRequiredNumber(Map<?, ?> map, String key) {
		if (map == null) {
			throw new IllegalArgumentException("Null Map while requiring key=" + key);
		}

		Object rawValue = map.get(key);

		if (rawValue == null || !(rawValue instanceof Number)) {
			throw new IllegalArgumentException("We miss '" + key + "' amongst available: " + map.keySet());
		}

		return (Number) rawValue;
	}

	public static <T> Map<T, ?> hideKey(Map<T, ?> formRegisterApp, String key) {
		if (formRegisterApp.containsKey(key)) {
			Map<T, ?> clone = new HashMap<>(formRegisterApp);
			clone.remove(key);
			return ImmutableMap.copyOf(clone);
		} else {
			throw new IllegalArgumentException("Can not hide a not present key: " + key);
		}
	}

	public static <T> Map<T, ?> hideKeys(Map<T, ?> mapToHide, String key, String... moreKeys) {
		AtomicReference<Map<T, ?>> hidden = new AtomicReference<>(mapToHide);

		Lists.asList(key, moreKeys).forEach(oneKey -> {
			hidden.set(hideKey(hidden.get(), oneKey));
		});

		return hidden.get();
	}

	public static <T> void transferValue(T key, Map<? extends T, ?> source, Map<T, Object> output) {
		Object valueToWrite = source.get(key);

		if (valueToWrite != null) {
			output.put(key, valueToWrite);
		}
	}
}
