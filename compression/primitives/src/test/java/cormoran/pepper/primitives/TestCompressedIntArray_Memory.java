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
package cormoran.pepper.primitives;

import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import it.unimi.dsi.fastutil.ints.IntList;

public class TestCompressedIntArray_Memory {
	@Test
	public void testEmpty() {
		IntList array = CompressedIntArrays.compress(IntStream.empty());

		Assert.assertTrue(array.isEmpty());
	}

	@Test
	public void testOnly0() {
		IntList array = CompressedIntArrays.compress(IntStream.of(0));

		Assert.assertEquals(1, array.size());
		Assert.assertArrayEquals(new int[] { 0 }, array.toIntArray());
	}

	@Test
	public void testOnly1() {
		IntList array = CompressedIntArrays.compress(IntStream.of(1));

		Assert.assertEquals(1, array.size());
		Assert.assertArrayEquals(new int[] { 1 }, array.toIntArray());
	}

	@Test
	public void testZeroAndZero() {
		IntList array = CompressedIntArrays.compress(IntStream.of(0, 0));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 0, 0 }, array.toIntArray());
	}

	@Test
	public void testZeroAndOne() {
		IntList array = CompressedIntArrays.compress(IntStream.of(0, 1));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 0, 1 }, array.toIntArray());
	}

	@Test
	public void testOneAndZero() {
		IntList array = CompressedIntArrays.compress(IntStream.of(1, 0));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 1, 0 }, array.toIntArray());
	}

	@Test
	public void testZeroAndTwo() {
		IntList array = CompressedIntArrays.compress(IntStream.of(0, 2));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 0, 2 }, array.toIntArray());
	}

	@Test
	public void testTwoAndZero() {
		IntList array = CompressedIntArrays.compress(IntStream.of(2, 0));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 2, 0 }, array.toIntArray());
	}

	@Test
	public void testOneAndOne() {
		IntList array = CompressedIntArrays.compress(IntStream.of(1, 1));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 1, 1 }, array.toIntArray());
	}

	@Test
	public void testZeroAndOneAndTwo() {
		IntList array = CompressedIntArrays.compress(IntStream.of(0, 1, 2));

		Assert.assertEquals(3, array.size());
		Assert.assertArrayEquals(new int[] { 0, 1, 2 }, array.toIntArray());
	}

	@Test
	public void testTwoAndOneAndZero() {
		IntList array = CompressedIntArrays.compress(IntStream.of(2, 1, 0));

		Assert.assertEquals(3, array.size());
		Assert.assertArrayEquals(new int[] { 2, 1, 0 }, array.toIntArray());
	}

	@Test
	public void testAllSingleBit() {
		IntList array =
				CompressedIntArrays.compress(IntStream.range(0, Integer.SIZE).map(i -> Integer.rotateLeft(1, i)));

		Assert.assertEquals(32, array.size());
		Assert.assertEquals(1, array.getInt(0));
		Assert.assertEquals(Integer.MIN_VALUE, array.getInt(31));
	}

	@Test
	public void testAllSingleBit_WithOverflow() {
		IntList array =
				CompressedIntArrays.compress(IntStream.range(0, Integer.SIZE + 1).map(i -> Integer.rotateLeft(1, i)));

		Assert.assertEquals(33, array.size());
		Assert.assertEquals(1, array.getInt(0));
		Assert.assertEquals(Integer.MIN_VALUE, array.getInt(31));
		Assert.assertEquals(1, array.getInt(32));
	}
}
