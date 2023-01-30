/**
 * The MIT License
 * Copyright (c) 2012-2013 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.tests.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.eclipse.mat.collect.IteratorInt;
import org.eclipse.mat.collect.IteratorLong;
import org.eclipse.mat.parser.index.IIndexReader;
import org.eclipse.mat.parser.index.IndexReader;
import org.eclipse.mat.parser.index.IndexWriter;
import org.eclipse.mat.parser.index.IndexWriter.Identifier;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestIndex1to1 {
	// Size of array
	long N = 600000;
	// Increase this for the huge tests
	long MAXELEMENTS = 30000000L;
	long MAXELEMENTS2 = 30000000L;
	static final boolean verbose = false;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 0 },
				{ 6 },
				// Test some boundary conditions for pages
				{ IndexWriter.PAGE_SIZE_INT - 1 },
				{ IndexWriter.PAGE_SIZE_INT },
				{ IndexWriter.PAGE_SIZE_INT + 1 },
				{ IndexWriter.PAGE_SIZE_INT * 2 - 1 },
				{ IndexWriter.PAGE_SIZE_INT * 2 },
				{ IndexWriter.PAGE_SIZE_INT * 2 + 1 },
				// medium
				{ 1000 },
				// <2G refs
				{ 3000 * 600000 },
				// 2G refs
				{ Integer.MAX_VALUE }, });
	}

	public TestIndex1to1(long n) {
		this.N = n;
	}

	@Test
	public void intIndex1() throws IOException {
		assumeTrue(N < MAXELEMENTS);
		long n = N;
		int n2 = (int) Math.min(n, Integer.MAX_VALUE);
		IndexWriter.IntIndexCollector ic = new IndexWriter.IntIndexCollector(n2, 31);
		for (int i = 0; i < n2; ++i) {
			ic.set(i, i);
		}

		for (int i = 0; i < n2; ++i) {
			int jj = ic.get(i);
			if (i != jj)
				assertEquals(i, jj);
		}
		ic.close();
	}

	@Test
	public void intIndex2() throws IOException {
		assumeTrue(N < MAXELEMENTS2);
		File indexFile = File.createTempFile("int1_", ".index");
		long n = N;
		int n2 = (int) Math.min(n, Integer.MAX_VALUE);
		IndexWriter.IntIndexCollector ic = new IndexWriter.IntIndexCollector(n2, 31);
		for (int i = 0; i < n2; ++i) {
			ic.set(i, i);
		}

		try {
			IIndexReader.IOne2OneIndex i2 = ic.writeTo(indexFile);
			try {
				for (int i = 0; i < n2; ++i) {
					int jj = i2.get(i);
					if (i != jj)
						assertEquals(i, jj);
				}
				if (n < Integer.MAX_VALUE)
					assertEquals(n, i2.size());
			} finally {
				i2.close();
			}
		} finally {
			assertTrue(indexFile.delete());
		}
	}

	@Test
	public void intIndex3() throws IOException {
		assumeTrue(N < MAXELEMENTS);
		File indexFile = File.createTempFile("int1_", ".index");
		final long n = N;
		final int n2 = (int) Math.min(n, Integer.MAX_VALUE);
		IndexWriter.IntIndexStreamer ic = new IndexWriter.IntIndexStreamer();

		try {
			IIndexReader.IOne2OneIndex i2 = ic.writeTo(indexFile, new IteratorInt() {
				long i;

				@Override
				public boolean hasNext() {
					return i < n;
				}

				@Override
				public int next() {
					return (int) i++;
				}

			});
			try {
				for (int i = 0; i < n2; ++i) {
					int in = i2.get(i);
					if (i != in)
						assertEquals(i, in);
				}
				if (n < Integer.MAX_VALUE)
					assertEquals(n, i2.size());
			} finally {
				i2.close();
			}
			IndexReader.IntIndexReader ir = new IndexReader.IntIndexReader(indexFile);
			try {
				long i = 0;

				for (IteratorInt it = ir.iterator(); it.hasNext(); ++i) {
					int in = it.next();
					if (i != in)
						assertEquals(i, in);
				}
				assertEquals(n, i);
				if (n < Integer.MAX_VALUE)
					assertEquals(n, i2.size());
			} finally {
				ir.close();
			}
		} finally {
			assertTrue(indexFile.delete());
		}
	}

	@Test
	public void intIndex4() throws IOException {
		assumeTrue(N < MAXELEMENTS2);
		long n = N;
		int n2 = (int) Math.min(n, Integer.MAX_VALUE);
		IndexWriter.SizeIndexCollectorUncompressed ic = new IndexWriter.SizeIndexCollectorUncompressed(n2);
		for (int i = 0; i < n2; ++i) {
			long v = i;
			ic.set(i, v);
		}

		for (int i = 0; i < n2; ++i) {
			int jj = ic.get(i);
			if (i != jj)
				assertEquals(i, jj);
		}
	}

	@Test
	public void intIndex5() throws IOException {
		assumeTrue(N < MAXELEMENTS2);
		File indexFile = File.createTempFile("int1_", ".index");
		long n = N;
		int n2 = (int) Math.min(n, Integer.MAX_VALUE);
		IndexWriter.SizeIndexCollectorUncompressed ic = new IndexWriter.SizeIndexCollectorUncompressed(n2);
		for (int i = 0; i < n2; ++i) {
			ic.set(i, i);
		}

		try {
			IIndexReader.IOne2SizeIndex i2 = ic.writeTo(indexFile);
			try {
				for (int i = 0; i < n2; ++i) {
					int jj = i2.get(i);
					if (i != jj)
						assertEquals(i, jj);
				}
				if (n < Integer.MAX_VALUE)
					assertEquals(n, i2.size());
			} finally {
				i2.close();
			}
		} finally {
			assertTrue(indexFile.delete());
		}
	}

	@Test
	public void intIndex6() throws IOException {
		assumeTrue(N < MAXELEMENTS2);
		File indexFile = File.createTempFile("int1_", ".index");
		long n = N;
		int n2 = (int) Math.min(n, Integer.MAX_VALUE);
		IndexWriter.SizeIndexCollectorUncompressed ic = new IndexWriter.SizeIndexCollectorUncompressed(n2);
		for (int i = 0; i < n2; ++i) {
			ic.set(i, i);
		}

		try {
			IIndexReader.IOne2SizeIndex i2 = ic.writeTo(indexFile);
			i2.close();
			i2 = new IndexReader.SizeIndexReader(indexFile);
			try {
				for (int i = 0; i < n2; ++i) {
					int jj = i2.get(i);
					if (i != jj)
						assertEquals(i, jj);
				}
				if (n < Integer.MAX_VALUE)
					assertEquals(n, i2.size());
			} finally {
				i2.close();
			}
		} finally {
			assertTrue(indexFile.delete());
		}
	}

	@Test
	public void intIdentifier1() {
		assumeTrue(N < MAXELEMENTS2);
		Identifier id = IndexWriter.newIdentifier();
		for (int i = 0; 0 <= i && i < N; ++i) {
			id.add(i + 0l);
		}
		for (int i = 0; 0 <= i && i < N; ++i) {
			assertEquals(i + 0l, id.get(i));
			assertEquals(i + 0l, id.getNext(i, 1)[0]);
		}
	}

	@Ignore("RoaringIdentifier sorts the longs: can not iterate in the same order as original insertion")
	@Test
	public void intIdentifier2() {
		assumeTrue(N < MAXELEMENTS2);
		Identifier id = IndexWriter.newIdentifier();
		Random r = new Random(N);
		for (int i = 0; 0 <= i && i < N; ++i) {
			id.add(r.nextLong());
		}
		r = new Random(N);
		for (int i = 0; 0 <= i && i < N; ++i) {
			long l1 = r.nextLong();
			assertEquals(l1, id.get(i));
			assertEquals(l1, id.getNext(i, 1)[0]);
		}
	}

	@Ignore("RoaringIdentifier sorts the longs: can not iterate in the same order as original insertion")
	@Test
	public void intIdentifier3() {
		assumeTrue(N < MAXELEMENTS2);
		Identifier id = IndexWriter.newIdentifier();
		Random r = new Random(N);
		for (int i = 0; 0 <= i && i < N; ++i) {
			id.add(r.nextLong());
		}
		r = new Random(N);
		for (IteratorLong l = id.iterator(); l.hasNext();) {
			assertEquals(r.nextLong(), l.next());
		}
	}

	@Test
	public void intIdentifier4() {
		assumeTrue(N < MAXELEMENTS2);
		assumeTrue(N > 0);
		Identifier id = IndexWriter.newIdentifier();
		Random r = new Random(N);
		for (int i = 0; 0 <= i && i < N; ++i) {
			long l1 = r.nextLong();
			id.add(l1);

		}

		// RoaringBitmap is already sorted
		id.sort();
		r = new Random(N);
		for (IteratorLong l = id.iterator(); l.hasNext(); l.next()) {
			long l1 = r.nextLong();
			int i = id.reverse(l1);
			assertEquals(l1, id.get(i));
			if (i > 0)
				assertTrue(id.get(i - 1) <= l1);
			if (i < id.size() - 1)
				assertTrue(l1 <= id.get(i + 1));
		}
	}
}