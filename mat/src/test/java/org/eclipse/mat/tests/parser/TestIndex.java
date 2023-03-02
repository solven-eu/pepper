/**
 * The MIT License
 * Copyright (c) 2012 Benoit Lacelle - SOLVEN
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

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import org.eclipse.mat.parser.index.IIndexReader.IOne2ManyIndex;
import org.eclipse.mat.parser.index.IIndexReader.IOne2ManyObjectsIndex;
import org.eclipse.mat.parser.index.IndexReader;
import org.eclipse.mat.parser.index.IndexWriter;
import org.eclipse.mat.parser.index.IndexWriter.KeyWriter;
import org.eclipse.mat.util.VoidProgressListener;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestIndex {
	// Number of arrays
	int M = 6_000;
	// Size of array
	int N = 600_000;
	// Variation in size
	int P = 6;
	// Increase this for the huge tests
	long MAXELEMENTS = 30000000L;
	// Increase this for the huge tests
	long MAXELEMENTS2 = 30000000L;
	static final boolean verbose = false;

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 0, 0, 0 },
				{ 1, 0, 0 },
				{ 6, 6, 0 },
				// Test some boundary conditions for pages
				{ 1, IndexWriter.PAGE_SIZE_INT - 1, 0 },
				{ 1, IndexWriter.PAGE_SIZE_INT, 0 },
				{ 1, IndexWriter.PAGE_SIZE_INT + 1, 0 },
				{ 1, IndexWriter.PAGE_SIZE_INT * 2 - 1, 0 },
				{ 1, IndexWriter.PAGE_SIZE_INT * 2, 0 },
				{ 1, IndexWriter.PAGE_SIZE_INT * 2 + 1, 0 },
				{ IndexWriter.PAGE_SIZE_INT - 1, 1, 0 },
				{ IndexWriter.PAGE_SIZE_INT, 1, 0 },
				{ IndexWriter.PAGE_SIZE_INT + 1, 1, 0 },
				{ IndexWriter.PAGE_SIZE_INT * 2 - 1, 1, 0 },
				{ IndexWriter.PAGE_SIZE_INT * 2, 1, 0 },
				{ IndexWriter.PAGE_SIZE_INT * 2 + 1, 1, 0 },
				// medium
				{ 100, 1_000, 11 },
				// <2G refs
				{ 3_000, 600_000, 11 },
				// >2G <4G refs
				{ 6_000, 600_000, 11 },
				// >4G refs
				{ 9_000, 600_000, 11 }, });
	}

	public TestIndex(int m, int n, int p) {
		this.M = m;
		this.N = n;
		this.P = p;
	}

	@Test
	public void test1ToN() throws IOException {
		assumeTrue((long) M * N < MAXELEMENTS2);
		int ii[][] = new int[P + 1][];
		for (int p = 0; p < P + 1; p++) {
			int nn = N + p;
			ii[p] = new int[nn];
			for (int i = 0; i < nn; ++i) {
				ii[p][i] = i;
			}
		}
		File indexFile = Files.createTempFile("1toN", ".index").toFile();
		try {
			IndexWriter.IntArray1NWriter f = new IndexWriter.IntArray1NWriter(M, indexFile);
			for (int j = 0; j < M; ++j) {
				// Vary the length a little
				int p = j % (P + 1);
				if (verbose)
					System.out.println("Writing " + j + "/" + M);
				f.log(j, ii[p]);
			}
			IOne2ManyIndex i2 = f.flush();
			try {
				for (int j = 0; j < M; ++j) {
					if (verbose)
						System.out.println("Reading " + j + "/" + M);
					int i3[] = i2.get(j);
					int p = j % (P + 1);
					// Junit array comparison is too slow
					if (!Arrays.equals(ii[p], i3))
						Assert.assertArrayEquals(ii[p], i3);
				}
			} finally {
				i2.close();
			}
		} finally {
			assertTrue(indexFile.delete());
		}
	}

	@Test
	public void testInbound() throws IOException {
		assumeTrue((long) M * N < MAXELEMENTS);
		int ii[][] = new int[P + 1][];
		for (int p = 0; p < P + 1; p++) {
			int nn = N + p;
			ii[p] = new int[nn];
			for (int i = 0; i < nn; ++i) {
				ii[p][i] = i;
			}
		}
		int mx = Math.max(M, N + P);
		File indexFile = Files.createTempFile("Inbound", ".index").toFile();
		try {
			IndexWriter.InboundWriter f = new IndexWriter.InboundWriter(mx, indexFile);
			for (int j = 0; j < M; ++j) {
				// Vary the length a little
				int p = j % (P + 1);
				if (verbose)
					System.out.println("Writing " + j + "/" + M);
				for (int k = 0; k < ii[p].length; ++k) {
					f.log(j, ii[p][k], k == 0);
				}
			}
			KeyWriter kw = new KeyWriter() {

				@Override
				public void storeKey(int index, Serializable key) {
					// TODO Auto-generated method stub

				}

			};
			IOne2ManyObjectsIndex z = f.flush(new VoidProgressListener(), kw);
			try {
				for (int j = 0; j < M; ++j) {
					if (verbose)
						System.out.println("Reading " + j + "/" + M);
					int p = j % (P + 1);
					int i2[] = z.get(j);
					for (int i : i2) {
						assertTrue(i >= 0);
					}
					if (!Arrays.equals(ii[p], i2)) {
						Assert.assertArrayEquals(ii[p], i2);
					}
				}
			} finally {
				z.close();
			}
		} finally {
			assertTrue(indexFile.delete());
		}
	}

	@Test
	public void testLong() throws IOException {
		assumeTrue((long) M * N < MAXELEMENTS);
		Random r = new Random();
		long ii[][] = new long[P + 1][];
		for (int p = 0; p < P + 1; p++) {
			int nn = N + p;
			ii[p] = new long[nn];
			for (int i = 0; i < nn; ++i) {
				ii[p][i] = r.nextLong();
			}
		}
		File indexFile = Files.createTempFile("LongOutbound", ".index").toFile();
		try {
			IndexWriter.LongArray1NWriter f = new IndexWriter.LongArray1NWriter(M, indexFile);
			for (int j = 0; j < M; ++j) {
				// Vary the length a little
				int p = j % (P + 1);
				if (verbose)
					System.out.println("Writing " + j + "/" + M);
				f.log(j, ii[p]);
			}
			f.flush();

			IndexReader.LongIndex1NReader i2 = new IndexReader.LongIndex1NReader(indexFile);
			try {
				for (int j = 0; j < M; ++j) {
					if (verbose)
						System.out.println("Reading " + j + "/" + M);
					long i3[] = i2.get(j);
					int p = j % (P + 1);
					// Junit array comparison is too slow
					if (!Arrays.equals(ii[p], i3))
						Assert.assertArrayEquals(ii[p], i3);
				}
			} finally {
				i2.close();
			}
		} finally {
			assertTrue(indexFile.delete());
		}
	}

	@Test
	public void test1ToNSorted() throws IOException {
		assumeTrue((long) M * N < MAXELEMENTS2);
		int ii[][] = new int[P + 1][];
		for (int p = 0; p < P + 1; p++) {
			int nn = N + p;
			ii[p] = new int[nn];
			for (int i = 0; i < nn; ++i) {
				ii[p][i] = i;
			}
		}
		File indexFile = Files.createTempFile("1toN", ".index").toFile();
		try {
			IndexWriter.IntArray1NSortedWriter f = new IndexWriter.IntArray1NSortedWriter(M, indexFile);
			for (int j = 0; j < M; ++j) {
				// Vary the length a little
				int p = j % (P + 1);
				if (verbose)
					System.out.println("Writing " + j + "/" + M);
				f.log(j, ii[p]);
			}
			IOne2ManyIndex i2 = f.flush();
			try {
				for (int j = 0; j < M; ++j) {
					if (verbose)
						System.out.println("Reading " + j + "/" + M);
					int i3[] = i2.get(j);
					int p = j % (P + 1);
					// Junit array comparison is too slow
					if (!Arrays.equals(ii[p], i3))
						Assert.assertArrayEquals(ii[p], i3);
				}
			} finally {
				i2.close();
			}
		} finally {
			assertTrue(indexFile.delete());
		}
	}

	@Test
	public void test1ToNSortedReader() throws IOException {
		assumeTrue((long) M * N < MAXELEMENTS2);
		int ii[][] = new int[P + 1][];
		for (int p = 0; p < P + 1; p++) {
			int nn = N + p;
			ii[p] = new int[nn];
			for (int i = 0; i < nn; ++i) {
				ii[p][i] = i;
			}
		}
		File indexFile = Files.createTempFile("1toN", ".index").toFile();
		try {
			IndexWriter.IntArray1NSortedWriter f = new IndexWriter.IntArray1NSortedWriter(M, indexFile);
			for (int j = 0; j < M; ++j) {
				// Vary the length a little
				int p = j % (P + 1);
				if (verbose)
					System.out.println("Writing " + j + "/" + M);
				f.log(j, ii[p]);
			}
			IOne2ManyIndex i2 = f.flush();
			i2.close();
			i2 = new IndexReader.IntIndex1NSortedReader(indexFile);
			try {
				for (int j = 0; j < M; ++j) {
					if (verbose)
						System.out.println("Reading " + j + "/" + M);
					int i3[] = i2.get(j);
					int p = j % (P + 1);
					// Junit array comparison is too slow
					if (!Arrays.equals(ii[p], i3))
						Assert.assertArrayEquals(ii[p], i3);
				}
			} finally {
				i2.close();
			}
		} finally {
			assertTrue(indexFile.delete());
		}
	}
}
