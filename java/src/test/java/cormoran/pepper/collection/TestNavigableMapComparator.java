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

import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Assert;
import org.junit.Test;

public class TestNavigableMapComparator {
	@Test
	public void testComparator() {
		NavigableMapComparator c = new NavigableMapComparator();

		NavigableMap<String, String> aa = new TreeMap<>();
		{
			aa.put("a", "a");
		}

		Assert.assertEquals(0, c.compare(aa, aa));

		NavigableMap<String, String> zz = new TreeMap<>();
		{
			zz.put("z", "z");
		}

		Assert.assertTrue(-1 >= c.compare(aa, zz));
		Assert.assertTrue(1 <= c.compare(zz, aa));

		NavigableMap<String, String> az = new TreeMap<>();
		{
			az.put("a", "z");
		}

		Assert.assertTrue(-1 >= c.compare(aa, az));
		Assert.assertTrue(1 <= c.compare(az, aa));

		NavigableMap<String, String> za = new TreeMap<>();
		{
			za.put("z", "a");
		}

		Assert.assertTrue(-1 >= c.compare(az, za));
		Assert.assertTrue(1 <= c.compare(za, az));
	}
}
