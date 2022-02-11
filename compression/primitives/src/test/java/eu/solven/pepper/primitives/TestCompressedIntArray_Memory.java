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
package eu.solven.pepper.primitives;

import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.solven.pepper.memory.IPepperMemoryConstants;
import eu.solven.pepper.primitives.ConstantBitsIntArray;

public class TestCompressedIntArray_Memory {
	@Test
	public void testEmpty() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.empty());

		Assert.assertTrue(array.isEmpty());
	}

	@Test
	public void testOnly0() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(0));

		Assert.assertEquals(1, array.size());
		Assert.assertArrayEquals(new int[] { 0 }, array.toIntArray());
	}

	@Test
	public void testOnly1() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(1));

		Assert.assertEquals(1, array.size());
		Assert.assertArrayEquals(new int[] { 1 }, array.toIntArray());
	}

	@Test
	public void testZeroAndZero() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(0, 0));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 0, 0 }, array.toIntArray());
	}

	@Test
	public void testZeroAndOne() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(0, 1));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 0, 1 }, array.toIntArray());
	}

	@Test
	public void testOneAndZero() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(1, 0));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 1, 0 }, array.toIntArray());
	}

	@Test
	public void testZeroAndTwo() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(0, 2));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 0, 2 }, array.toIntArray());
	}

	@Test
	public void testTwoAndZero() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(2, 0));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 2, 0 }, array.toIntArray());
	}

	@Test
	public void testOneAndOne() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(1, 1));

		Assert.assertEquals(2, array.size());
		Assert.assertArrayEquals(new int[] { 1, 1 }, array.toIntArray());
	}

	@Test
	public void testZeroAndOneAndTwo() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(0, 1, 2));

		Assert.assertEquals(3, array.size());
		Assert.assertArrayEquals(new int[] { 0, 1, 2 }, array.toIntArray());
	}

	@Test
	public void testTwoAndOneAndZero() {
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(IntStream.of(2, 1, 0));

		Assert.assertEquals(3, array.size());
		Assert.assertArrayEquals(new int[] { 2, 1, 0 }, array.toIntArray());
	}

	// Generate an array of all ints having a single bit set to 1, others are set to 0
	@Ignore("TODO")
	@Test
	public void testAllSingleBit() {
		ConstantBitsIntArray array =
				ConstantBitsIntArray.fromIntStream(IntStream.range(0, Integer.SIZE).map(i -> Integer.rotateLeft(1, i)));

		Assert.assertEquals(32, array.size());
		Assert.assertEquals(1, array.getInt(0));
		Assert.assertEquals(Integer.MIN_VALUE, array.getInt(31));
	}

	// Generate an array of all ints having a single bit set to 1, others are set to 0, and last one is the same as the
	// first int
	@Ignore("TODO")
	@Test
	public void testAllSingleBit_WithOverflow() {
		ConstantBitsIntArray array = ConstantBitsIntArray
				.fromIntStream(IntStream.range(0, Integer.SIZE + 1).map(i -> Integer.rotateLeft(1, i)));

		Assert.assertEquals(33, array.size());
		Assert.assertEquals(1, array.getInt(0));
		Assert.assertEquals(Integer.MIN_VALUE, array.getInt(31));
		Assert.assertEquals(1, array.getInt(32));
	}

	@Ignore("TODO")
	@Test
	public void testFuzzy_small() {
		IntStream source = new Random(0).ints().limit(IPepperMemoryConstants.KB);
		ConstantBitsIntArray array = ConstantBitsIntArray.fromIntStream(source);

		PrimitiveIterator.OfInt source_2 = new Random(0).ints().limit(IPepperMemoryConstants.KB).iterator();

		for (int i = 0; i < array.size(); i++) {
			Assert.assertEquals(source_2.nextInt(), array.getInt(i));
		}

		if (source_2.hasNext()) {
			Assert.fail("The source have more elements not present in the compressed list");
		}
	}
}
