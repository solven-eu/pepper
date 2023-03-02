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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.tests.regression.Difference;
import org.eclipse.mat.util.MessageUtil;

public class CSVComparator implements IComparator {

	@Override
	public List<Difference> compare(File baseline, File testFile) throws Exception {
		String testName = baseline.getName().substring(0, baseline.getName().lastIndexOf('.'));

		System.out.println(MessageUtil.format("Comparing: {0}", testName));

		List<Difference> differences = new ArrayList<Difference>();
		if (baseline.length() < testFile.length()) {
			differences.add(new Difference("",
					"baseLine length: " + baseline.length(),
					"testFile length: " + testFile.length()));
			System.err.println(MessageUtil.format("ERROR: ({0}) Files have different lengths", testName));
			return differences;
		}
		BufferedReader baselineReader = null;
		BufferedReader testFileReader = null;
		try {
			baselineReader = new BufferedReader(new FileReader(baseline.getAbsolutePath()), 1_024);
			testFileReader = new BufferedReader(new FileReader(testFile.getAbsolutePath()), 1_024);

			String baseLine;
			String testLine;
			int lineNumber = 1;
			while ((baseLine = baselineReader.readLine()) != null) {
				if (!(baseLine).equals(testLine = testFileReader.readLine())) {
					differences.add(new Difference(Integer.toString(lineNumber), baseLine, testLine));
				}
				if (differences.size() == 10) // add only first 10 differences
					break;
				lineNumber = lineNumber + 1;
			}

			if (!differences.isEmpty())
				System.err.println(MessageUtil.format("ERROR: ({0}) Differences detected", testName));

		} catch (IOException e) {
			System.err.println(MessageUtil.format("ERROR: ({0}) Error reading file {0}", testName, e.getMessage()));
		} finally {
			try {
				if (testFileReader != null)
					testFileReader.close();
				if (baselineReader != null)
					baselineReader.close();
			} catch (IOException e) {
				System.err.println(MessageUtil.format("ERROR: ({0}) Error closing BufferedReader: {0}", //
						testName,
						e.getMessage()));
			}
		}
		return differences;
	}
}
