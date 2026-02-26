/**
 * The MIT License
 * Copyright (c) 2026 Benoit Lacelle - SOLVEN
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.experimental.UtilityClass;

/**
 * Unsafe as it accepts null values.
 * 
 * @author Benoit Lacelle
 */
@UtilityClass
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
public class MapWithNulls {

	public static <K, V> Map<K, V> of(K k, V v) {
		Map<K, V> map = new LinkedHashMap<>();

		map.put(k, v);

		return map;
	}

	public static <K, V> Map<K, V> of(K k, V v, K k2, V v2) {
		Map<K, V> map = new LinkedHashMap<>();

		map.put(k, v);
		map.put(k2, v2);

		return map;
	}

	public static <K, V> Map<K, V> of(K k, V v, K k2, V v2, K k3, V v3) {
		Map<K, V> map = new LinkedHashMap<>();

		map.put(k, v);
		map.put(k2, v2);
		map.put(k3, v3);

		return map;
	}

	public static <K, V> Map<K, V> of(K k, V v, K k2, V v2, K k3, V v3, K k4, V v4) {
		Map<K, V> map = new LinkedHashMap<>();

		map.put(k, v);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);

		return map;
	}

	public static <K, V> MapWithNullsBuilder<K, V> builder() {
		return new MapWithNullsBuilder<>();
	}

	/**
	 * Lombok @Builder
	 * 
	 * @param <K>
	 * @param <V>
	 */
	public static class MapWithNullsBuilder<K, V> {
		final Map<K, V> map = new LinkedHashMap<>();
		final AtomicBoolean built = new AtomicBoolean();

		public MapWithNullsBuilder<K, V> put(K k, V v) {
			if (built.get()) {
				throw new IllegalStateException("built already");
			}

			map.put(k, v);

			return this;
		}

		public Map<K, V> build() {
			built.set(true);

			return map;
		}
	}
}
