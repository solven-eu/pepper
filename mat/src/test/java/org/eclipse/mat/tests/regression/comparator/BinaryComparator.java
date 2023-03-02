/**
 * The MIT License
 * Copyright (c) 2008 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.tests.regression.comparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.mat.tests.regression.Difference;
import org.eclipse.mat.util.MessageUtil;

public class BinaryComparator implements IComparator {
	private static final int BUFFSIZE = 1_024;
	private byte baselineBuffer[] = new byte[BUFFSIZE];
	private byte testBuffer[] = new byte[BUFFSIZE];

	private final String FAILED_MESSAGE = "Test result differs from the baseline";

	@Override
	public List<Difference> compare(File baseline, File testFile) throws Exception {
		String testName = baseline.getName().substring(0, baseline.getName().lastIndexOf('.'));
		System.out.println(MessageUtil.format("Comparing: {0}", testName));

		List<Difference> differences = new ArrayList<Difference>();

		InputStream baselineStream = null;
		InputStream testStream = null;
		if (baseline.length() != testFile.length()) {
			String errorMessage = MessageUtil.format(
					"Files have different lengths: baseline file length = {0}, test file length = {1}",
					baseline.length(),
					testFile.length());
			differences.add(new Difference(errorMessage));
			System.err.println(MessageUtil.format("ERROR: ({0}) {1}", testName, errorMessage));
			return differences;
		}

		try {
			baselineStream = new FileInputStream(baseline);
			testStream = new FileInputStream(testFile);

			if (inputStreamEquals(testName, baselineStream, testStream)) {
				return null;
			} else {
				differences.add(new Difference(FAILED_MESSAGE));
				System.err.println(MessageUtil.format("ERROR: ({0}) {1}", testName, FAILED_MESSAGE));
				return differences;
			}

		} catch (Exception e) {
			System.err.println(
					MessageUtil.format("ERROR: ({0}) Error comparing binary files: {0}", testName, e.getMessage()));
			return null;
		} finally {
			try {
				if (baselineStream != null)
					baselineStream.close();
				if (testStream != null)
					testStream.close();
			} catch (Exception ex) {
			}
		}
	}

	private boolean inputStreamEquals(String testName, InputStream baselineStream, InputStream testStream) {
		if (baselineStream == testStream)
			return true;
		if (baselineStream == null && testStream == null)
			return true;
		if (baselineStream == null || testStream == null)
			return false;
		try {
			int readBaseline = -1;
			int readTest = -1;

			do {
				int baselineOffset = 0;
				while (baselineOffset < BUFFSIZE && (readBaseline =
						baselineStream.read(baselineBuffer, baselineOffset, BUFFSIZE - baselineOffset)) >= 0) {
					baselineOffset = baselineOffset + readBaseline;
				}

				int testOffset = 0;
				while (testOffset < BUFFSIZE
						&& (readTest = testStream.read(testBuffer, testOffset, BUFFSIZE - testOffset)) >= 0) {
					testOffset = testOffset + readTest;
				}
				if (baselineOffset != testOffset)
					return false;

				if (baselineOffset != BUFFSIZE) {
					Arrays.fill(baselineBuffer, baselineOffset, BUFFSIZE, (byte) 0);
					Arrays.fill(testBuffer, testOffset, BUFFSIZE, (byte) 0);
				}
				if (!Arrays.equals(baselineBuffer, testBuffer))
					return false;
			} while (readBaseline >= 0 && readTest >= 0);

			if (readBaseline < 0 && readTest < 0)
				return true; // EOF for both

			return false;

		} catch (Exception e) {
			System.err.println(
					MessageUtil.format("ERROR: ({0}) Error comparing binary files: {0}", testName, e.getMessage()));
			return false;
		}
	}

}
