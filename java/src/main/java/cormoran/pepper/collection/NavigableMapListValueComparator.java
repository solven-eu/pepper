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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Comparator} for {@link NavigableMap} representing a location coordinate
 * 
 * @author Benoit Lacelle
 *
 */
public class NavigableMapListValueComparator implements
		Comparator<NavigableMap<? extends Comparable<?>, ? extends List<? extends Comparable<?>>>>, Serializable {

	// http://findbugs.sourceforge.net/bugDescriptions.html#SE_COMPARATOR_SHOULD_BE_SERIALIZABLE
	private static final long serialVersionUID = 7928339315645573854L;

	protected static final Logger LOGGER = LoggerFactory.getLogger(NavigableMapComparator.class);

	@Override
	public int compare(NavigableMap<? extends Comparable<?>, ? extends List<? extends Comparable<?>>> o1,
			NavigableMap<? extends Comparable<?>, ? extends List<? extends Comparable<?>>> o2) {
		// Anyway, Comparing Comparable<?> is unsafe as String an Integer are
		// both Comparable<?> but not comparable together
		return staticCompare(o1,
				o2,
				UnsafeNavigableMapListValueComparator.KEY_COMPARATOR,
				UnsafeNavigableMapListValueComparator.LIST_VALUES_COMPARATOR);
	}

	public static <T> int staticCompare(NavigableMap<? extends T, ? extends List<? extends T>> o1,
			NavigableMap<? extends T, ? extends List<? extends T>> o2,
			Comparator<? super T> keyComparator,
			Comparator<? super List<? extends T>> listValueComparator) {
		Iterator<? extends Entry<? extends T, ? extends List<? extends T>>> itKey1 = o1.entrySet().iterator();
		Iterator<? extends Entry<? extends T, ? extends List<? extends T>>> itKey2 = o2.entrySet().iterator();

		// For now, we guess values are equals
		int valueCompare = 0;

		while (itKey1.hasNext() && itKey2.hasNext()) {
			Entry<? extends T, ? extends List<? extends T>> next1 = itKey1.next();
			Entry<? extends T, ? extends List<? extends T>> next2 = itKey2.next();

			int keyCompare = keyComparator.compare(next1.getKey(), next2.getKey());

			if (keyCompare != 0) {
				// key1 is before key2: then map1 is before map2
				return keyCompare;
			}

			// Check for valueComparison only if they are still considered
			// equals
			if (valueCompare == 0) {
				// Prepare the value comparison even if the key comparison has
				// priority: most of the time, the diff will be in the values,
				// and it prevents looping twice through the entries
				valueCompare = listValueComparator.compare(next1.getValue(), next2.getValue());
			}
		}

		if (itKey1.hasNext()) {
			// key1 is like [A,B] while key2 is like [A]: key2 is before key1
			return 1;
		} else if (itKey2.hasNext()) {
			// then it1 does not have next: it1 is bigger: it2 is smaller
			return -1;
		} else {
			// both it1 and it2 does not have next: then, their keys are equal:
			// do the same for values
			if (valueCompare == 0) {
				// All keys equals and all values equals: Map are equals
				assert o1.equals(o2);

				return 0;
			} else {
				return valueCompare;
			}
		}
	}
}
