/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;

import eu.solven.pepper.logging.PepperLogHelper;

/**
 * Enable converting a standard/recursive {@link Map} or {@link List} into a {@link Map} in a flatten format. By
 * flatten, we mean a {@link Map} with no level of recursivity. The flatten {@link Map} can be seen as a {@link List} of
 * entry, with a JsonPath (see https://github.com/json-path/JsonPath) as key and the primitive value as value.
 *
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings({ "PMD.GodClass", "PMD.AvoidDuplicateLiterals" })
public class MapPathHelper {
	// This will be used to represent a null-reference as Value.
	// In recursiveFromFlatten, it will used for comparison by reference
	// It means the whole class is targetting same-JVM use.
	private static final AtomicReference<Object> MARKER_NULL = new AtomicReference<>();

	public static void setNullMarker(Object nullMarker) {
		MARKER_NULL.set(nullMarker);
	}

	protected MapPathHelper() {
		// hidden
	}

	/**
	 * The key convention follows mostly (i.e. tries to be a subset of) JsonPath syntax.
	 * 
	 * @param map
	 * @return a mutable {@link NavigableMap} expressing each primitive null-null value associated to a key following
	 *         the path to this value. No value would be a {@link Map} or a {@link Collection}
	 * 
	 * @see https://github.com/json-path/JsonPath#path-examples
	 */
	public static NavigableMap<String, Object> flatten(Map<?, ?> map) {
		// Do not merge List in Set, to enable building back to recursive
		boolean distinctOnCollection = false;

		return flatten(Function.identity(), map, distinctOnCollection);
	}

	public static NavigableMap<String, Object> flatten(Collection<?> collection) {
		// Do not merge List in Set, to enable building back to recursive
		boolean distinctOnCollection = false;

		return flatten(Function.identity(), collection, distinctOnCollection);
	}

	/**
	 * We replace '.' by '..'. It enables accepting keys with a '.'
	 *
	 * @param k
	 * @return
	 */
	private static String toFlattenKeyFragment(Object k) {
		String kAsString = String.valueOf(k);

		Pattern regex = Pattern.compile("[^a-zA-Z0-9_]");
		Matcher matcher = regex.matcher(kAsString);
		if (matcher.find()) {
			// This is a complex name

			String escaped = kAsString.replaceAll("(?:['\\[\\]])", "\\\\$0");
			return "['" + escaped + "']";
		} else {
			// This is a simple name
			return "." + kAsString;
		}
	}

	public static NavigableMap<String, Object> flatten(Function<Object, Object> onValues,
			Map<?, ?> map,
			boolean distinctOnCollection) {
		return innerFlatten(true, onValues, map, distinctOnCollection);
	}

	private static NavigableMap<String, Object> innerFlatten(boolean root,
			Function<Object, Object> onValues,
			Map<?, ?> map,
			boolean distinctOnCollection) {
		NavigableMap<String, Object> flattenKeys = new TreeMap<>();
		map.forEach((key, value) -> {
			String flattenKeySuffix = toFlattenKeyFragment(key);

			String flattenKey;
			if (root) {
				flattenKey = "$" + flattenKeySuffix;
			} else {
				flattenKey = flattenKeySuffix;
			}

			if (value == null) {
				flattenKeys.put(flattenKey, MARKER_NULL.get());
			} else if (value instanceof Map<?, ?>) {
				Map<?, ?> valueAsMap = (Map<?, ?>) value;
				Map<String, Object> subFlattenKeys = innerFlatten(false, onValues, valueAsMap, distinctOnCollection);

				// '.k' is appended the the path
				subFlattenKeys.forEach((subKey, subValue) -> flattenKeys.put(flattenKey + subKey, subValue));
			} else if (value instanceof List<?>) {
				List<?> valueAsList = (List<?>) value;
				Map<String, ?> subFlattenKeys = innerFlatten(false, onValues, valueAsList, distinctOnCollection);

				// '[i]' is appended the the path
				subFlattenKeys.forEach((subKey, subValue) -> flattenKeys.put(flattenKey + subKey, subValue));
			} else {
				flattenKeys.put(flattenKey, onValues.apply(value));
			}
		});
		return flattenKeys;
	}

	public static NavigableMap<String, Object> flatten(Function<Object, Object> onValues,
			Iterable<?> list,
			boolean distinctOnList) {
		return innerFlatten(true, onValues, list, distinctOnList);
	}

	private static NavigableMap<String, Object> innerFlatten(boolean root,
			Function<Object, Object> onValues,
			Iterable<?> list,
			boolean distinctOnList) {
		NavigableMap<String, Object> flatten = new TreeMap<>();

		int i = -1;
		for (Object o : list) {
			i++;

			String flattenKeySuffix = "[" + i + "]";

			String flattenKey;
			if (root) {
				flattenKey = "$" + flattenKeySuffix;
			} else {
				flattenKey = flattenKeySuffix;
			}

			if (o == null) {
				flatten.put(flattenKey, MARKER_NULL.get());
			} else if (o instanceof Map<?, ?>) {
				Map<String, Object> flattenMap = innerFlatten(false, onValues, (Map<?, ?>) o, distinctOnList);

				flattenMap.forEach((k, v) -> {
					flatten.put(flattenKey + k, v);
				});
			} else if (o instanceof Collection<?>) {
				Map<String, Object> flattenMap = innerFlatten(false, onValues, (Collection<?>) o, distinctOnList);

				flattenMap.forEach((k, v) -> {
					flatten.put(flattenKey + k, v);
				});
			} else {
				Object processedValue = onValues.apply(o);

				flatten.put(flattenKey, processedValue);
			}
		}

		return flatten;
	}

	/**
	 * Sets a value, creating any missing parents
	 * 
	 * @param context
	 * @param path
	 *            supports only "definite" paths in the simple format {@code $.a.b.c}.
	 * @param value
	 *            value to set
	 */
	// https://github.com/json-path/JsonPath/issues/83#issuecomment-728251374
	// https://github.com/json-path/JsonPath/issues/83#issuecomment-1247614513
	public static void set(DocumentContext context, String path, Object value) {
		String parentPath;
		String key;
		int propertyIndex;
		// parse the path ending
		boolean endsWithBracket = path.endsWith("]");

		if (endsWithBracket) {
			int pos = path.lastIndexOf('[');

			if ('\'' == path.charAt(path.length() - 2)) {
				while ('\\' == path.charAt(pos - 1)) {
					// This is an escaped '[': search for the previous one
					pos = path.lastIndexOf('[', pos - 1);
				}

				// A path like `$.k['weird.Property']`
				key = path.substring(pos + 2, path.length() - 2);

				// Remove the escape character '\'
				key = key.replaceAll("\\\\(?<escaped>.)", "$1");

				propertyIndex = Integer.MIN_VALUE;
			} else {
				// A path like `$.k[7]`
				key = path.substring(pos + 1, path.length() - 1);
				try {
					propertyIndex = Integer.parseInt(key);
				} catch (NumberFormatException e) {
					String msg = "Unsupported value \"" + key
							+ "\" for index, only non-negative integers are expected; path: \""
							+ path
							+ "\"";
					throw new IllegalArgumentException(msg, e);
				}
			}
			parentPath = path.substring(0, pos);
		} else {
			// A path like `$.k.property`
			int pos = path.lastIndexOf('.');
			if (pos < 0) {
				throw new IllegalArgumentException("Invalid jsonPath: " + path + ". It seems not to start with '$.'");
			}
			parentPath = path.substring(0, pos);
			key = path.substring(pos + 1);
			propertyIndex = Integer.MIN_VALUE;
		}
		ensureParentExists(context, parentPath, propertyIndex);

		// set the value
		if (propertyIndex == Integer.MIN_VALUE) {
			context.put(parentPath, key, value);
		} else {
			List<Object> parent = context.read(parentPath);
			if (propertyIndex < parent.size()) {
				context.set(path, value);
			} else {
				for (int i = parent.size(); i < propertyIndex; i++) {
					parent.add(null);
				}
				parent.add(value);
			}
		}
	}

	private static void ensureParentExists(DocumentContext context, String parentPath, Integer index) {
		try {
			context.read(parentPath);
		} catch (PathNotFoundException e) {
			// Programming by Exception
			// Quite bad, but it does the job through JasonPath
			if (index == Integer.MIN_VALUE) {
				set(context, parentPath, new LinkedHashMap<>());
			} else {
				set(context, parentPath, new ArrayList<>(index));
			}
		}
	}

	/**
	 * This will not always succeed restoring the initial {@link Map}. Especially to various edge-cases: null-reference
	 * management, empty {@link Map}, etc
	 * 
	 * @param flatten
	 * @return a recursive {@link Map} given a flatten {@link Map}.
	 */
	@SuppressWarnings("PMD.NullAssignment")
	public static Map<String, Object> recurse(Map<String, ?> flatten) {
		Configuration conf = Configuration.defaultConfiguration();
		DocumentContext emptyJson = JsonPath.using(conf).parse("{}");

		flatten.forEach((k, v) -> {
			if (v instanceof List<?> || v instanceof Map<?, ?>) {
				throw new IllegalArgumentException(
						"A flatten Map should neither have a Map nor Collection value. value="
								+ PepperLogHelper.getObjectAndClass(v));
			}

			if (MARKER_NULL.get() == v) {
				v = null;
			}
			set(emptyJson, k, v);
		});

		return emptyJson.json();
	}
}
