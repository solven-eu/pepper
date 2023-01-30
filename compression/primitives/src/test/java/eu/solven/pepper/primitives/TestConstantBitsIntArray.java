/**
 * The MIT License
 * Copyright (c) 2023 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.primitives;

import org.junit.Assert;
import org.junit.Test;

public class TestConstantBitsIntArray {
	@Test
	public void testEmty() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[0]);

		Assert.assertEquals(0, compressed.size());
	}

	@Test
	public void testHolds0() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 0 });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(0, compressed.getInt(0));
	}

	@Test
	public void testHolds1() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 1 });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(1, compressed.getInt(0));
	}

	@Test
	public void testHolds2() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 2 });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(2, compressed.getInt(0));
	}

	@Test
	public void testHoldsMinus1() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { -1 });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(-1, compressed.getInt(0));
	}

	@Test
	public void testHoldsMaxValue() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { Integer.MAX_VALUE });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(Integer.MAX_VALUE, compressed.getInt(0));
	}

	@Test
	public void testHoldsMinValue() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { Integer.MIN_VALUE });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(Integer.MIN_VALUE, compressed.getInt(0));
	}

	@Test
	public void testHolds0_2() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 0, 2 });

		Assert.assertEquals(2, compressed.size());
		Assert.assertEquals(0, compressed.getInt(0));
		Assert.assertEquals(2, compressed.getInt(1));
	}

	@Test
	public void testHolds0_1_2() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 0, 1, 2 });

		Assert.assertEquals(3, compressed.size());
		Assert.assertEquals(0, compressed.getInt(0));
		Assert.assertEquals(1, compressed.getInt(1));
		Assert.assertEquals(2, compressed.getInt(2));
	}
}
