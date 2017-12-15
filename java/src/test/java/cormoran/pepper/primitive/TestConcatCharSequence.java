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
package cormoran.pepper.primitive;

import org.junit.Assert;
import org.junit.Test;

public class TestConcatCharSequence {
	@Test
	public void testConcatCharSequence_subSequence() {
		Assert.assertEquals("a", new ConcatCharSequence("ab", "cd").subSequence(0, 1).toString());
		Assert.assertEquals("d", new ConcatCharSequence("ab", "cd").subSequence(3, 4).toString());
		Assert.assertEquals("abcd", new ConcatCharSequence("ab", "cd").subSequence(0, 4).toString());
		Assert.assertEquals("bc", new ConcatCharSequence("ab", "cd").subSequence(1, 3).toString());
	}

	@Test
	public void testConcatCharSequence_charIt() {
		Assert.assertEquals('a', new ConcatCharSequence("ab", "cd").charAt(0));
		Assert.assertEquals('b', new ConcatCharSequence("ab", "cd").charAt(1));
		Assert.assertEquals('c', new ConcatCharSequence("ab", "cd").charAt(2));
		Assert.assertEquals('d', new ConcatCharSequence("ab", "cd").charAt(3));
	}
}
