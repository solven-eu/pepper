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

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link Comparator} enable the comparison of {@link NavigableMap}
 * 
 * @author Benoit Lacelle
 * 
 */
public class UnsafeNavigableMapListValueComparator
		implements Comparator<NavigableMap<?, ? extends List<?>>>, Serializable {

	// http://findbugs.sourceforge.net/bugDescriptions.html#SE_COMPARATOR_SHOULD_BE_SERIALIZABLE
	private static final long serialVersionUID = 7928339315645573854L;

	protected static final Logger LOGGER = LoggerFactory.getLogger(NavigableMapComparator.class);

	@Override
	public int compare(NavigableMap<?, ? extends List<?>> o1, NavigableMap<?, ? extends List<?>> o2) {
		return NavigableMapListValueComparator.staticCompare(o1, o2, KEY_COMPARATOR, LIST_VALUES_COMPARATOR);
	}

	protected static final Comparator<Object> KEY_COMPARATOR = new Comparator<Object>() {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public int compare(Object key, Object key2) {
			if (key == key2) {
				// Fast check
				return 0;
			} else if (key instanceof Comparable<?>) {
				if (key2 instanceof Comparable<?>) {
					return ((Comparable) key).compareTo(key2);
				} else {
					throw new RuntimeException(key2 + " is not Comparable");
				}
			} else {
				throw new RuntimeException(key + " is not Comparable");
			}
		}
	};

	protected static final Comparator<List<?>> LIST_VALUES_COMPARATOR = new Comparator<List<?>>() {

		@Override
		public int compare(List<?> key, List<?> key2) {
			int commonDepth = Math.min(key.size(), key2.size());
			for (int i = 0; i < commonDepth; i++) {
				int compareResult = KEY_COMPARATOR.compare(key.get(i), key2.get(i));

				if (compareResult != 0) {
					// [A,B,C] is before [A,D] as B is before D
					return compareResult;
				}
			}

			if (key.size() == key2.size()) {
				// Same lists
				return 0;
			} else {
				// We consider as first the shorter path ([A] is before [A,B])
				return key.size() - key2.size();
			}
		}
	};
}
