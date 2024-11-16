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
package eu.solven.pepper.mappath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enables doing `.put` operations referring pathes a-la-MapPath.
 * 
 * @author Benoit Lacelle
 *
 */
public class MapPathPut {
	private static final Logger LOGGER = LoggerFactory.getLogger(MapPath.class);

	protected MapPathPut() {
		// hidden
	}

	public static void mergedPutAll(Map<?, ?> whereToWrite, Map<?, ?> whereToRead) {
		whereToRead.forEach((k, v) -> putEntry(whereToWrite, v, k));
	}

	public static void putEntry(Map<?, ?> whereToWrite, Object value, Object firstKey, Object... moreKeys) {
		putEntry(whereToWrite, value, MapPathGet.asList(firstKey, moreKeys));
	}

	// Very similar to https://github.com/json-path/JsonPath#set-a-value
	public static void putEntry(Map<?, ?> whereToWrite, Object value, List<?> keys) {
		rawPutEntry(false, whereToWrite, value, keys);
	}

	/**
	 * If we encounter a List in the path, we expect the associated key is an Integer. This index is the REPLACED
	 * element, and NOT the index where a new element would be inserted.
	 *
	 * @param whereToWrite
	 * @param value
	 * @param firstKey
	 * @param moreKeys
	 */
	public static void putEntry(List<?> whereToWrite, Object value, Object firstKey, Object... moreKeys) {
		List<Object> keys = MapPathGet.asList(firstKey, moreKeys);

		rawPutEntry(false, whereToWrite, value, keys);
	}

	@SuppressWarnings({ "unchecked", "PMD.CognitiveComplexity" })
	static void rawPutEntry(boolean forceMapAsIs, Object whereToWrite, Object value, List<?> keys) {
		if (!forceMapAsIs && value instanceof Map<?, ?>) {
			((Map<?, ?>) value).forEach((k, v) -> {
				List<Object> values = new ArrayList<>(keys.size() + 1);
				values.addAll(keys);
				values.add(k);
				rawPutEntry(false, whereToWrite, v, values);
			});
			return;
		}

		var currentMapOrList = whereToWrite;
		for (var i = 0; i < keys.size() - 1; i++) {
			Object currentKey = keys.get(i);
			Object rawCurrentValue;
			if (currentMapOrList instanceof Map<?, ?>) {
				rawCurrentValue = ((Map<?, ?>) currentMapOrList).get(currentKey);
			} else {
				if (!(currentKey instanceof Integer)) {
					throw new IllegalArgumentException("Need an Integer to browse a List. Was: " + currentKey);
				}
				var currentKeyAsInt = (int) currentKey;
				if (currentKeyAsInt < 0) {
					throw new IllegalArgumentException("Need an positive Integer to browse a List. Was: " + currentKey);
				}
				ensureSize((List<?>) currentMapOrList, currentKeyAsInt);
				rawCurrentValue = ((List<?>) currentMapOrList).get(currentKeyAsInt);
			}

			if (rawCurrentValue == null) {
				// This intermediate key is new. Is it a list or a Map ?
				rawCurrentValue = instantiateNewValueForPath(keys, i);
				rawPutEntry(true, whereToWrite, rawCurrentValue, keys.subList(0, i + 1));
			} else if (!(rawCurrentValue instanceof Map<?, ?>) && !(rawCurrentValue instanceof List<?>)) {
				var suffix = " i=" + i + " in key-chain: " + keys;
				if (currentMapOrList instanceof Map<?, ?>) {
					throw new IllegalStateException("Can not replace a not-Map value (" + rawCurrentValue.getClass()
							+ ") by a Map/List. Ill key: '"
							+ currentKey
							+ "' in target map with keySet="
							+ ((Map<?, ?>) currentMapOrList).keySet()
							+ suffix);

				} else {
					throw new IllegalStateException("Can not replace a not-List value (" + rawCurrentValue.getClass()
							+ ") by a Map/List. Ill key: '"
							+ currentKey
							+ "' in target List with size="
							+ ((List<?>) currentMapOrList).size()
							+ suffix);
				}
			}
			currentMapOrList = rawCurrentValue;
		}

		Object deepestKey = keys.get(keys.size() - 1);
		try {
			if (currentMapOrList instanceof List<?>) {
				List<Object> asList = (List<Object>) currentMapOrList;
				var deepestKeyAsInteger = (Integer) deepestKey;
				ensureSize((List<?>) currentMapOrList, deepestKeyAsInteger);
				asList.set(deepestKeyAsInteger, value);
			} else {
				((Map<Object, Object>) currentMapOrList).put(deepestKey, value);
			}
		} catch (RuntimeException e) {
			LOGGER.debug("The List/Map seems not writable", e);
			if (keys.size() == 1) {
				// the root is not writable
				throw new RuntimeException("The root is not writable", e);
			} else if (currentMapOrList instanceof Map<?, ?>) {
				// Convert to a writable map
				Map<?, ?> mutableMap = convertToMutableMap((Map<?, ?>) currentMapOrList);
				((Map<Object, Object>) mutableMap).put(keys.get(keys.size() - 1), value);
				// Inject the mutable Map in its parent Map
				rawPutEntry(true, whereToWrite, mutableMap, keys.subList(0, keys.size() - 1));
			} else {
				throw new RuntimeException("The deepest vaue is not writable", e);
			}
		}
	}

	/** Instantiates either a Map or a List, depending on the following key. It is never called on the deepest key. */
	private static Object instantiateNewValueForPath(List<?> keys, int currentKeyIndex) {
		var nextKeyIndex = currentKeyIndex + 1;
		if (nextKeyIndex > keys.size()) {
			throw new IllegalStateException("We should never consider an index out of the keys");
		}
		var isArray = keys.get(nextKeyIndex) instanceof Integer;
		if (isArray) {
			return new ArrayList<>();
		} else {
			return new LinkedHashMap<>();
		}
	}

	/** Fills a list with nulls until it reaches a size sufficient to access indexToReach */
	private static void ensureSize(List<?> currentMapOrList, int indexToReach) {
		if (indexToReach >= currentMapOrList.size()) {
			List<Object> nullsOnly = Arrays.asList(new Object[1 + indexToReach - currentMapOrList.size()]);
			((List<Object>) currentMapOrList).addAll(nullsOnly);
		}
	}

	/**
	 * 
	 * @param <T>
	 * @param <S>
	 * @param currentMap
	 * @return a mutable shallow-copy of given Map
	 */
	static <T, S> Map<T, S> convertToMutableMap(Map<T, S> currentMap) {
		// Linked to preserve ordering
		// HashMap to enable mutability
		return new LinkedHashMap<>(currentMap);
	}
}
