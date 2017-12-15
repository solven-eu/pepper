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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestImmutableArrayList {
	@Test
	public void testSubSet() {
		List<?> subset = new ImmutableArrayList<>(new Object[] { 13, 27 }, new int[1]);

		Assert.assertEquals(1, subset.size());
		Assert.assertEquals(13, subset.get(0));
		Assert.assertEquals(Arrays.asList(13).hashCode(), subset.hashCode());

		// Check both equals
		Assert.assertEquals(Arrays.asList(13), subset);
		Assert.assertEquals(subset, Arrays.asList(13));
	}

	@Test
	public void testPlain() {
		List<?> subset = new ImmutableArrayList<>(new Object[] { 13, 27 });

		Assert.assertEquals(2, subset.size());
		Assert.assertEquals(13, subset.get(0));
		Assert.assertEquals(27, subset.get(1));
		Assert.assertEquals(Arrays.asList(13, 27).hashCode(), subset.hashCode());

		// Check both equals
		Assert.assertEquals(Arrays.asList(13, 27), subset);
		Assert.assertEquals(subset, Arrays.asList(13, 27));
	}
}
