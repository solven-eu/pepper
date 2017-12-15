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
import java.util.Map.Entry;
import java.util.NavigableMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Comparator} which compare first the keyset, then the values
 * 
 * @author Benoit Lacelle
 * 
 */
public class NavigableMapComparator implements Comparator<NavigableMap<String, String>>, Serializable {
	// http://findbugs.sourceforge.net/bugDescriptions.html#SE_COMPARATOR_SHOULD_BE_SERIALIZABLE
	private static final long serialVersionUID = 7928339315645573854L;

	protected static final Logger LOGGER = LoggerFactory.getLogger(NavigableMapComparator.class);

	@Override
	public int compare(NavigableMap<String, String> o1, NavigableMap<String, String> o2) {
		Iterator<Entry<String, String>> itKey1 = o1.entrySet().iterator();
		Iterator<Entry<String, String>> itKey2 = o2.entrySet().iterator();

		while (itKey1.hasNext() && itKey2.hasNext()) {
			Entry<String, String> next1 = itKey1.next();
			Entry<String, String> next2 = itKey2.next();

			int keyCompare = next1.getKey().compareTo(next2.getKey());

			if (keyCompare != 0) {
				// key1 is before key2: then map1 is before map2
				return keyCompare;
			}
		}

		if (itKey1.hasNext()) {
			// then it2 does not have next: it2 is bigger: it1 is smaller
			return -1;
		} else if (itKey2.hasNext()) {
			// then it1 does not have next: it1 is bigger: it2 is smaller
			return 1;
		} else {
			// both it1 and it2 does not have next: then, their keys are equal:
			// do the same for values

			Iterator<Entry<String, String>> itValue1 = o1.entrySet().iterator();
			Iterator<Entry<String, String>> itValue2 = o2.entrySet().iterator();

			while (itValue1.hasNext() && itValue2.hasNext()) {
				Entry<String, String> next1 = itValue1.next();
				Entry<String, String> next2 = itValue2.next();

				int valueCompare = next1.getValue().compareTo(next2.getValue());

				if (valueCompare != 0) {
					return valueCompare;
				}
			}

			// All keys equals and all values equals: Map are equals
			assert o1.equals(o2);

			return 0;
		}
	}
}
