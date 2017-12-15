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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cormoran.pepper.logging.PepperLogHelper;
import it.unimi.dsi.fastutil.ints.IntList;

public class TestCompressedIntArray_Performance {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TestCompressedIntArray_Performance.class);

	@Test
	public void testCompresseeDecompress_Smalls() {
		int size = 8 * 1024 * 1024;

		long start = System.currentTimeMillis();
		IntList array = CompressedIntArrays.compress(IntStream.range(0, size).map(i -> i % 1024));
		long compressed = System.currentTimeMillis();

		AtomicLong sum = new AtomicLong(0);
		array.iterator().forEachRemaining((IntConsumer) i -> sum.addAndGet(i));
		long decompressed = System.currentTimeMillis();

		LOGGER.info("Time to compress: {}, Time to decompress: {}",
				PepperLogHelper.getNiceTime(compressed - start),
				PepperLogHelper.getNiceTime(decompressed - start));
	}

}
