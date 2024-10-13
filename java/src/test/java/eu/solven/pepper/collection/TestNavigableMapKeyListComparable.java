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
package eu.solven.pepper.collection;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestNavigableMapKeyListComparable {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Comparator<NavigableMap> makeComparator() {
		return (Comparator) new NavigableMapListValueComparator();
	}

	@Test
	public void testNavigableMapListValueComparatorDepth1() {
		@SuppressWarnings("rawtypes")
		Comparator<NavigableMap> c = makeComparator();

		NavigableMap<String, List<String>> aa = new TreeMap<>();
		{
			aa.put("a", Arrays.asList("a"));
		}

		Assertions.assertEquals(0, c.compare(aa, aa));

		NavigableMap<String, List<String>> zz = new TreeMap<>();
		{
			zz.put("z", Arrays.asList("z"));
		}

		Assertions.assertTrue(-1 >= c.compare(aa, zz));
		Assertions.assertTrue(1 <= c.compare(zz, aa));

		NavigableMap<String, List<String>> az = new TreeMap<>();
		{
			az.put("a", Arrays.asList("z"));
		}

		Assertions.assertTrue(-1 >= c.compare(aa, az));
		Assertions.assertTrue(1 <= c.compare(az, aa));

		NavigableMap<String, List<String>> za = new TreeMap<>();
		{
			za.put("z", Arrays.asList("a"));
		}

		Assertions.assertTrue(-1 >= c.compare(az, za));
		Assertions.assertTrue(1 <= c.compare(za, az));
	}

	@Test
	public void testNavigableMapListValueComparatorDepth2() {
		@SuppressWarnings("rawtypes")
		Comparator<NavigableMap> c = makeComparator();

		NavigableMap<String, List<String>> a_ab = new TreeMap<>();
		{
			a_ab.put("a", Arrays.asList("a", "b"));
		}

		Assertions.assertEquals(0, c.compare(a_ab, a_ab));

		NavigableMap<String, List<String>> z_ac = new TreeMap<>();
		{
			z_ac.put("z", Arrays.asList("a", "c"));
		}

		Assertions.assertTrue(-1 >= c.compare(a_ab, z_ac));
		Assertions.assertTrue(1 <= c.compare(z_ac, a_ab));

		NavigableMap<String, List<String>> a_ac = new TreeMap<>();
		{
			a_ac.put("a", Arrays.asList("a", "c"));
		}

		Assertions.assertTrue(-1 >= c.compare(a_ab, a_ac));
		Assertions.assertTrue(1 <= c.compare(a_ac, a_ab));

		NavigableMap<String, List<String>> z_ab = new TreeMap<>();
		{
			z_ab.put("z", Arrays.asList("a", "b"));
		}

		Assertions.assertTrue(-1 >= c.compare(a_ac, z_ab));
		Assertions.assertTrue(1 <= c.compare(z_ab, a_ac));
	}

	@Test
	public void testNavigableMapListValueComparatorDepth1And2() {
		@SuppressWarnings("rawtypes")
		Comparator<NavigableMap> c = makeComparator();

		NavigableMap<String, List<String>> a_a = new TreeMap<>();
		{
			a_a.put("a", Arrays.asList("a"));
		}

		Assertions.assertEquals(0, c.compare(a_a, a_a));

		NavigableMap<String, List<String>> z_ac = new TreeMap<>();
		{
			z_ac.put("z", Arrays.asList("a", "c"));
		}

		Assertions.assertTrue(-1 >= c.compare(a_a, z_ac));
		Assertions.assertTrue(1 <= c.compare(z_ac, a_a));

		NavigableMap<String, List<String>> a_ac = new TreeMap<>();
		{
			a_ac.put("a", Arrays.asList("a", "c"));
		}

		Assertions.assertTrue(-1 >= c.compare(a_a, a_ac));
		Assertions.assertTrue(1 <= c.compare(a_ac, a_a));

		NavigableMap<String, List<String>> z_b = new TreeMap<>();
		{
			z_b.put("z", Arrays.asList("b"));
		}

		Assertions.assertTrue(-1 >= c.compare(a_ac, z_b));
		Assertions.assertTrue(1 <= c.compare(z_b, a_ac));
	}

	@Test
	public void testNavigableMapListKey() {
		@SuppressWarnings("rawtypes")
		Comparator<NavigableMap> c = makeComparator();

		NavigableMap<String, List<String>> a_a = new TreeMap<>();
		{
			a_a.put("a", Arrays.asList("a"));
		}

		Assertions.assertEquals(0, c.compare(a_a, a_a));

		NavigableMap<String, List<String>> a_a_b_a = new TreeMap<>();
		{
			a_a_b_a.put("a", Arrays.asList("a"));
			a_a_b_a.put("b", Arrays.asList("a"));
		}

		Assertions.assertTrue(-1 >= c.compare(a_a, a_a_b_a));
		Assertions.assertTrue(1 <= c.compare(a_a_b_a, a_a));
	}
}
