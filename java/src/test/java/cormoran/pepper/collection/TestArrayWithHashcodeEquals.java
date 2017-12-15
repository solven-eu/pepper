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

import org.junit.Assert;
import org.junit.Test;

public class TestArrayWithHashcodeEquals {
	@Test
	public void testHashCodeEquals() {
		ArrayWithHashcodeEquals first = new ArrayWithHashcodeEquals(new Object[] { "a", "b" });
		ArrayWithHashcodeEquals second = new ArrayWithHashcodeEquals(new Object[] { new String("a"), "b" });

		// NotSame
		Assert.assertNotSame(first, second);
		Assert.assertNotSame(first.array, second.array);
		Assert.assertNotSame(first.array[0], second.array[0]);

		// But equals
		Assert.assertEquals(first.hashCode(), second.hashCode());
		Assert.assertEquals(first, second);
	}

}
