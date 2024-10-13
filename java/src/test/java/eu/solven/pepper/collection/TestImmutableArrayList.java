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
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestImmutableArrayList {
	@Test
	public void testSubSet() {
		List<?> subset = new ImmutableArrayList<>(new Object[] { 13, 27 }, new int[1]);

		Assertions.assertEquals(1, subset.size());
		Assertions.assertEquals(13, subset.get(0));
		Assertions.assertEquals(Arrays.asList(13).hashCode(), subset.hashCode());

		// Check both equals
		Assertions.assertEquals(Arrays.asList(13), subset);
		Assertions.assertEquals(subset, Arrays.asList(13));
	}

	@Test
	public void testPlain() {
		List<?> subset = new ImmutableArrayList<>(new Object[] { 13, 27 });

		Assertions.assertEquals(2, subset.size());
		Assertions.assertEquals(13, subset.get(0));
		Assertions.assertEquals(27, subset.get(1));
		Assertions.assertEquals(Arrays.asList(13, 27).hashCode(), subset.hashCode());

		// Check both equals
		Assertions.assertEquals(Arrays.asList(13, 27), subset);
		Assertions.assertEquals(subset, Arrays.asList(13, 27));
	}
}
