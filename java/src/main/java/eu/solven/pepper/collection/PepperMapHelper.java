/**
 * The MIT License
 * Copyright (c) 2014-2024 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.collection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Various helpers for Map
 *
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings({ "PMD.GodClass", "PMD.AvoidDuplicateLiterals", "PMD.CouplingBetweenObjects" })
public class PepperMapHelper {
	protected PepperMapHelper() {
		// hidden
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

	@SuppressWarnings("PMD.GenericsNaming")
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
