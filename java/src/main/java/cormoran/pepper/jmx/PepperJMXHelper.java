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
package cormoran.pepper.jmx;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.google.common.base.Functions;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cormoran.pepper.io.PepperSerializationHelper;
import cormoran.pepper.stream.PepperStreamHelper;

/**
 * Various utility methods specific to JMX
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperJMXHelper {

	// By default, the JConsole prefill String arguments with this value
	public static final String JMX_DEFAULT_STRING = "String";

	// This is by what the default JMX String is replaced by
	public static final String STANDARD_DEFAULT_STRING = "";

	// By default, the JConsole prefill String arguments with this value
	public static final int JMX_DEFAULT_INT = 0;

	// If we query a huge result, by default it will be truncated to this result size
	public static final int DEFAULT_LIMIT = 400;

	protected PepperJMXHelper() {
		// hidden
	}

	public static String convertToString(String asString) {
		if (asString == null || asString.isEmpty() || PepperJMXHelper.JMX_DEFAULT_STRING.equals(asString)) {
			// If the user left the default JMX String, we consider he expects a
			// full-range search
			return STANDARD_DEFAULT_STRING;
		} else {
			// We prefer to clean whitespaces as it is typically input provided
			// by a human
			return asString.trim();
		}
	}

	public static Map<String, String> convertToMap(String asString) {
		asString = convertToString(asString);

		if (asString.isEmpty()) {
			// If the user left the default JMX String, we consider he expects a
			// full-range search
			return Collections.emptyMap();
		} else {
			return PepperSerializationHelper.convertToMapStringString(asString);
		}
	}

	public static Map<String, List<String>> convertToMapList(String asString) {
		asString = convertToString(asString);

		if (asString.isEmpty()) {
			// If the user left the default JMX String, we consider he expects a
			// full-range search
			return Collections.emptyMap();
		} else {
			return PepperSerializationHelper.convertToMapStringListString(asString);
		}
	}

	public static List<? extends Map<String, String>> convertToJMXListMapString(
			Iterable<? extends Map<String, ?>> iterator) {
		// Convert to brand HashMap of String for JMX compatibility
		// Lexicographical order over String

		// Cast to Map to workaround eclipse 3.7 compiler issue
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=519539
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Iterable<? extends Map<String, String>> asString = Iterables.transform(iterator,
				input -> new TreeMap<>(Maps.transformValues((Map) input, String::valueOf)));

		// Convert to brand ArrayList for JMX compatibility
		return Lists.newArrayList(asString);

	}

	public static List<String> convertToList(String asString) {
		asString = convertToString(asString);

		if (asString.isEmpty()) {
			// If the user left the default JMX String, we consider he expects a
			// full-range search
			return Collections.emptyList();
		} else {
			return PepperSerializationHelper.convertToListString(asString);
		}
	}

	public static Set<?> convertToSet(String asString) {
		asString = convertToString(asString);

		if (asString.isEmpty()) {
			return Collections.emptySet();
		} else {
			return PepperSerializationHelper.convertToSet(asString);
		}
	}

	public static Set<? extends String> convertToSetString(String asString) {
		asString = convertToString(asString);

		if (asString.isEmpty()) {
			return Collections.emptySet();
		} else {
			return PepperSerializationHelper.convertToSetString(asString);
		}
	}

	public static Map<String, String> convertToJMXMapString(Map<?, ?> someMap) {
		// Maintain order, and use a JMX compatible implementation
		Map<String, String> idToQueryString = new LinkedHashMap<>();

		for (Entry<?, ?> entry : someMap.entrySet()) {
			idToQueryString.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
		}

		return idToQueryString;
	}

	public static <T> Map<String, T> convertToJMXMapKeyString(Map<?, T> someMap) {
		// Maintain order, and use a JMX compatible implementation
		Map<String, T> idToQueryString = new LinkedHashMap<>();

		for (Entry<?, T> entry : someMap.entrySet()) {
			idToQueryString.put(String.valueOf(entry.getKey()), entry.getValue());
		}

		return idToQueryString;
	}

	public static <S, T> Map<S, T> convertToJMXMap(Map<S, T> asMap) {
		Map<S, T> cleanerMap;

		// We accept to break ordering, and prefer return a user-friendly order
		try {
			cleanerMap = new TreeMap<S, T>(asMap);
		} catch (RuntimeException e) {
			// TreeMap ctor could fail if the key is not comparable (e.g. if the
			// key is itself a Map)
			cleanerMap = asMap;
		}
		return convertToJMXValueOrderedMap(cleanerMap, Optional.empty());
	}

	public static <S, T extends Comparable<T>> Map<S, T> convertToJMXValueOrderedMap(Map<S, T> map) {
		return convertToJMXValueOrderedMap(map, false);
	}

	public static <S, T extends Comparable<T>> Map<S, T> convertToJMXValueOrderedMap(Map<S, T> map,
			final boolean reverse) {

		Comparator<Entry<S, T>> comparator;

		if (reverse) {
			comparator = Map.Entry.<S, T>comparingByValue().reversed();
		} else {
			comparator = Map.Entry.comparingByValue();
		}

		return convertToJMXValueOrderedMap(map, Optional.of(comparator));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static <S, T> Map<S, T> convertToJMXValueOrderedMap(Map<S, T> map,
			Optional<? extends Comparator<? super Entry<S, T>>> comparator) {

		Stream<Entry<S, T>> entries = map.entrySet().stream();

		Supplier<Map<S, T>> mapSupplier;
		if (comparator.isPresent()) {
			entries = entries.sorted(comparator.get());
			mapSupplier = LinkedHashMap::new;
		} else {
			if (map instanceof TreeMap) {
				mapSupplier = TreeMap::new;
			} else {
				mapSupplier = LinkedHashMap::new;
			}
		}

		// http://stackoverflow.com/questions/29567575/sort-map-by-value-using-java-8

		return entries.collect(PepperStreamHelper.toMap(e -> {
			if (e.getKey() instanceof List<?>) {
				return (S) convertToJMXList((List) e.getKey());
			} else {
				return e.getKey();
			}
			// LinkedHashMap to maintain order (as defined by values), and JMX
			// Compatible
		}, Entry::getValue, mapSupplier));
	}

	public static <T extends Comparable<T>> Set<T> convertToJMXSet(Iterable<? extends T> elements) {
		// TreeSet: JMX-compatible and lexicographically ordered
		return Sets.newTreeSet(elements);
	}

	public static <T> List<T> convertToJMXList(Iterable<? extends T> elements) {
		// ArrayList: JMX-compatible
		return Lists.newArrayList(elements);
	}

	public static <T> Collection<T> convertToJMXCollection(Iterable<? extends T> elements) {
		if (elements instanceof Set<?>) {
			return Sets.newLinkedHashSet(elements);
		} else {
			// ArrayList: JMX-compatible
			return Lists.newArrayList(elements);
		}
	}

	public static List<String> convertToJMXStringList(Iterable<?> elements) {
		return convertToJMXList(Iterables.transform(elements, Functions.toStringFunction()));
	}

	public static List<String> convertToJMXStringSet(Set<?> elements) {
		return convertToJMXList(Iterables.transform(elements, Functions.toStringFunction()));
	}

	public static int convertToLimit(int limit) {
		if (limit == PepperJMXHelper.JMX_DEFAULT_INT) {
			return PepperJMXHelper.DEFAULT_LIMIT;
		} else {
			return limit;
		}
	}

	public static URL convertToURL(String url) throws MalformedURLException {
		url = PepperJMXHelper.convertToString(url);

		if (Strings.isNullOrEmpty(url)) {
			return null;
		}

		Objects.requireNonNull(url);

		if (new File(url).exists()) {
			return new File(url).toURI().toURL();
		} else {
			// https://stackoverflow.com/questions/5442658/spaces-in-urls
			return new URL(url.replaceAll(" ", "%20"));
		}
	}

	public static Path convertToPath(String path) {
		path = PepperJMXHelper.convertToString(path);

		if (Strings.isNullOrEmpty(path)) {
			return null;
		}

		return Paths.get(path);
	}
}
