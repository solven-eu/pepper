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

import java.util.Random;
import java.util.stream.IntStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cormoran.pepper.logging.PepperLogHelper;
import cormoran.pepper.memory.PepperMemoryHelper;
import it.unimi.dsi.fastutil.ints.IntList;

public class TestCompressedIntArray {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestCompressedIntArray.class);

	@Test
	public void testGrowingBy1() {
		int size = 1024 * 1024;

		IntList array = CompressedIntArrays.compress(IntStream.range(0, size));

		LOGGER.info("testGrowingBy1 CompressedSize: {}",
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.deepSize(array)));
		LOGGER.info("testGrowingBy1 RawSize: {}",
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.deepSize(IntStream.range(0, size).toArray())));
	}

	@Test
	public void testManyVerySmall() {
		int size = 1024 * 1024;

		IntList array = CompressedIntArrays.compress(IntStream.range(0, size).map(i -> i % 16));

		LOGGER.info("testManyVerySmall CompressedSize: {}",
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.deepSize(array)));
		LOGGER.info("testManyVerySmall RawSize: {}",
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.deepSize(IntStream.range(0, size).toArray())));
	}

	@Test
	public void testManySmall() {
		int size = 1024 * 1024;

		IntList array = CompressedIntArrays.compress(IntStream.range(0, size).map(i -> i % 1024));

		LOGGER.info("testManySmall CompressedSize: {}",
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.deepSize(array)));
		LOGGER.info("testManySmall RawSize: {}",
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.deepSize(IntStream.range(0, size).toArray())));
	}

	@Test
	public void testManyRandom() {
		int size = 1024 * 1024;
		Random r = new Random(0);

		IntList array = CompressedIntArrays.compress(IntStream.range(0, size).map(i -> r.nextInt()));

		LOGGER.info("testManySmall CompressedSize: {}",
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.deepSize(array)));
		LOGGER.info("testManySmall RawSize: {}",
				PepperLogHelper.getNiceMemory(PepperMemoryHelper.deepSize(IntStream.range(0, size).toArray())));
	}

}
