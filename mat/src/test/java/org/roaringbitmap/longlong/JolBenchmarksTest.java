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
package org.roaringbitmap.longlong;

import org.openjdk.jol.info.GraphLayout;

/**
 * This runs benchmarks over {@link Roaring64NavigableMap} memory layout, as suggested in
 * https://github.com/RoaringBitmap/RoaringBitmap/issues/346
 */
// https://github.com/openjdk/jol
// https://github.com/RoaringBitmap/RoaringBitmap/issues/346
public class JolBenchmarksTest {
	@SuppressWarnings("restriction")
	public static void main(String[] args) {
		distinctHigherRadices();
		sameHigherRadix();
	}

	private static void distinctHigherRadices() {
		int valuesPerRadix = 1 << 16;
		int distance = 2000;
		Roaring64NavigableMap bitmap = new Roaring64NavigableMap();
		long[] radices = new long[1024];
		for (int i = 0; i < radices.length; ++i) {
			radices[i] = ((long) i) << 48;
		}
		for (int i = 0; i < radices.length; i++) {
			for (int j = 0; j < valuesPerRadix; ++j) {
				bitmap.addLong(radices[i] | (j * distance));
			}
		}

		long[] array = bitmap.toArray();

		Roaring64Bitmap bitmapOpt = new Roaring64Bitmap();
		bitmapOpt.add(array);

		System.out.println("---distinctHigherRadices---");
		System.out.println(GraphLayout.parseInstance(array).toFootprint());

		System.out.println("---");
		System.out.println(GraphLayout.parseInstance(bitmap).toFootprint());
		bitmap.runOptimize();
		System.out.println(GraphLayout.parseInstance(bitmap).toFootprint());

		System.out.println("---");
		System.out.println(GraphLayout.parseInstance(bitmapOpt).toFootprint());
		bitmapOpt.runOptimize();
		System.out.println(GraphLayout.parseInstance(bitmapOpt).toFootprint());
	}

	private static void sameHigherRadix() {
		int numValues = (1 << 16) * 1024;
		int distance = 2000;
		Roaring64NavigableMap bitmap = new Roaring64NavigableMap();

		long x = 0L;
		for (int i = 0; i < numValues; i++) {
			bitmap.addLong(x);
			x += distance;
		}

		long[] array = bitmap.toArray();

		Roaring64Bitmap bitmapOpt = new Roaring64Bitmap();
		bitmapOpt.add(array);

		System.out.println("---sameHigherRadix---");
		System.out.println(GraphLayout.parseInstance(array).toFootprint());

		System.out.println("---");
		System.out.println(GraphLayout.parseInstance(bitmap).toFootprint());
		bitmap.runOptimize();
		System.out.println(GraphLayout.parseInstance(bitmap).toFootprint());

		System.out.println("---");
		System.out.println(GraphLayout.parseInstance(bitmapOpt).toFootprint());
		bitmapOpt.runOptimize();
		System.out.println(GraphLayout.parseInstance(bitmapOpt).toFootprint());

	}
}
