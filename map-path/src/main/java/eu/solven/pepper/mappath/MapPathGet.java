/**
 * The MIT License
 * Copyright (c) 2014-2025 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.mappath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Helps reading through a recursive {@link Map}
 *
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings({ "PMD.GodClass", "PMD.AvoidDuplicateLiterals", "PMD.CouplingBetweenObjects" })
public class MapPathGet {
	protected MapPathGet() {
		// hidden
	}

	/**
	 *
	 * @param <T>
	 * @param mapOrList
	 * @param keys
	 * @return the object mapped from body (generally a {@link Map}) following the chain of keys. This returns null if
	 *         the path leads to nothing. This fails if the path is invalid given the {@link Map} (e.g. path is 'k1.k2'
	 *         while the {@link Map} is ('k1': 'v1'))
	 */
	@Deprecated(since = "Prefer .getOptionalAs or .getRequiredAs")
	private static <T> T rawGetAs(Object mapOrList, List<?> keys) {
		Object value = mapOrList;

		for (Object key : keys) {
			if (value == null) {
				return null;
			} else if (value instanceof Map<?, ?>) {
				value = ((Map<?, ?>) value).get(key);
			} else if (key instanceof Number && value instanceof List<?>) {
				List<?> valueAsList = (List<?>) value;
				int indexAsInt = ((Number) key).intValue();
				if (indexAsInt < 0 || indexAsInt >= valueAsList.size()) {
					return null;
				}
				value = valueAsList.get(indexAsInt);
			} else {
				throw new IllegalArgumentException("Can not process keys " + keys
						+ " on mapOrList: "
						+ mapOrList
						+ " as value for key="
						+ key
						+ " is: "
						+ MapPath.getObjectAndClass(value));
			}
		}

		return (T) value;
	}

	public static <T> Optional<T> getOptionalAs(Object mapOrList, Object firstKey, Object... moreKeys) {
		return getOptionalAs(mapOrList, asList(firstKey, moreKeys));
	}

	static List<Object> asList(Object firstKey, Object... moreKeys) {
		List<Object> asList = new ArrayList<>(1 + moreKeys.length);

		asList.add(firstKey);
		asList.addAll(Arrays.asList(moreKeys));

		return asList;
	}

	public static <T> Optional<T> getOptionalAs(Object mapOrList, List<?> keys) {
		T value = rawGetAs(mapOrList, keys);

		if (value == null) {
			return Optional.empty();
		} else {
			return Optional.of(value);
		}
	}

	public static <T> T getRequiredAs(final Object mapOrList, Object mainKey, Object... subKeys) {
		List<Object> allKeys = checkNullMap(mapOrList, mainKey, subKeys);

		return digForValue(mapOrList, allKeys, (rawValue) -> {
			return (T) rawValue;
		});
	}

	public static Optional<String> getOptionalString(Object mapOrList, Object firstKey, Object... moreKeys) {
		return getOptionalString(mapOrList, asList(firstKey, moreKeys));
	}

	public static Optional<String> getOptionalString(Object mapOrList, List<?> keys) {
		Optional<Object> optValue = getOptionalAs(mapOrList, keys);

		if (optValue.isPresent()) {
			Object rawNotNull = optValue.get();
			if (rawNotNull instanceof String) {
				String valueString = rawNotNull.toString();
				if (valueString.isEmpty()) {
					return Optional.empty();
				} else {
					return Optional.of(valueString);
				}
			} else {
				throw new IllegalArgumentException(
						"keys=" + keys + " led to not-a-string: " + MapPath.getObjectAndClass(rawNotNull));
			}
		} else {
			return Optional.empty();
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

	public static <S, T> Map<S, T> getRequiredMap(Object mapOrList, Object mainKey, Object... subKeys) {
		List<Object> allKeys = checkNullMap(mapOrList, mainKey, subKeys);

		return digForValue(mapOrList, allKeys, (rawValue) -> {
			if (rawValue instanceof Map<?, ?>) {
				return (Map<S, T>) rawValue;
			} else {
				throw new IllegalArgumentException("'" + allKeys
						+ "' is attached to "
						+ MapPath.getObjectAndClass(rawValue)
						+ " which is not a Map");
			}
		});
	}

	public static String getRequiredString(final Object mapOrList, Object mainKey, Object... subKeys) {
		List<Object> allKeys = checkNullMap(mapOrList, mainKey, subKeys);

		return MapPathGet.<String>digForValue(mapOrList, allKeys, (rawValue) -> {
			if (rawValue instanceof String && rawValue.toString().isEmpty()) {
				throw new IllegalArgumentException(
						"'" + allKeys + "' is attached to an (empty string). Was digging through" + allKeys);
			}

			Object lastKey = allKeys.get(allKeys.size() - 1);
			return checkNonNullString(lastKey.toString(), rawValue);
		});
	}

	@SuppressWarnings("PMD.CognitiveComplexity")
	private static <T> T digForValue(final Object mapOrList, List<Object> allKeys, Function<Object, T> toValue) {
		if (allKeys.isEmpty()) {
			return (T) mapOrList;
		}

		// Map<?, ?> currentMap = map;
		// We'll have a specific logic when processing the last key
		int deepestKeyIndex = allKeys.size() - 1;

		Object currentHolder = mapOrList;
		for (int i = 0; i < allKeys.size(); i++) {
			final Object currentKey = allKeys.get(i);
			final Object rawValue;

			if (currentKey instanceof Number && currentHolder instanceof List<?>) {
				List<?> holderAsList = (List<?>) currentHolder;
				int currentKeyAsInt = ((Number) currentKey).intValue();

				int listSize = holderAsList.size();
				if (currentKeyAsInt < 0 || currentKeyAsInt >= listSize) {
					throw new IllegalArgumentException(
							"Invalid index=" + currentKeyAsInt + " over list of size=" + listSize);
				}

				rawValue = holderAsList.get(currentKeyAsInt);
			} else {
				rawValue = ((Map<?, ?>) currentHolder).get(currentKey);
			}

			if (rawValue == null) {
				if (currentHolder instanceof Map<?, ?>) {
					Map<?, ?> currentMap = (Map<?, ?>) currentHolder;
					Set<?> keySet = currentMap.keySet();
					if (currentMap.containsKey(currentKey)) {
						throw new IllegalArgumentException(
								"We miss '" + currentKey + "' (value is null) amongst available: " + keySet);
					} else {
						throw new IllegalArgumentException(
								"We miss '" + currentKey + "' (key not present) amongst available: " + keySet);
					}
				} else if (currentHolder instanceof List<?>) {
					throw new IllegalArgumentException(
							"Value in list at index=" + MapPath.getObjectAndClass(currentKey) + " is null");
				}
			}

			if (i == deepestKeyIndex) {
				// We are processed the last key
				return toValue.apply(rawValue);
			} else if (rawValue instanceof Map<?, ?> || rawValue instanceof List<?>) {
				currentHolder = rawValue;
			} else {
				throw new IllegalArgumentException(
						"Not managed intermediate type: " + MapPath.getObjectAndClass(rawValue));
			}
		}

		throw new IllegalStateException("Would never happen");
	}

	private static List<Object> checkNullMap(Object mapOrList, Object mainKey, Object... subKeys) {
		List<Object> keys = asList(mainKey, subKeys);
		if (mapOrList == null) {
			throw new IllegalArgumentException("Input mapOrList is null while requiring keys=" + keys);
		}
		return keys;
	}

	public static Number getRequiredNumber(Map<?, ?> map, Object mainKey, Object... subKeys) {
		List<Object> allKeys = checkNullMap(map, mainKey, subKeys);

		return digForValue(map, allKeys, (rawValue) -> {
			if (rawValue instanceof Number) {
				return (Number) rawValue;
			} else {
				// Do not throw the value which might be a personal value
				throw new IllegalArgumentException("Not a number (" + rawValue
						.getClass() + ") for " + allKeys + ". Deepest Map<?,?> keys: " + map.keySet());
			}
		});
	}

	public static boolean getRequiredBoolean(Map<?, ?> map, Object mainKey, Object... subKeys) {
		List<Object> allKeys = checkNullMap(map, mainKey, subKeys);

		return digForValue(map, allKeys, (rawValue) -> {
			if (rawValue instanceof Boolean) {
				return (Boolean) rawValue;
			} else {
				// Do not throw the value which might be a personal value
				throw new IllegalArgumentException("Not a boolean (" + rawValue
						.getClass() + ") for " + allKeys + ". Deepest Map<?,?> keys: " + map.keySet());
			}
		});
	}
}
