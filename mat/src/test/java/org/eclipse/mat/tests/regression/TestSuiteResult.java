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
package org.eclipse.mat.tests.regression;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*package*/class TestSuiteResult {
	private File snapshot;
	private List<SingleTestResult> singleTestResult = new ArrayList<SingleTestResult>();
	private List<String> errorMessages = new ArrayList<String>();
	private List<PerfData> perfData = new ArrayList<PerfData>();

	public TestSuiteResult(File snapshot) {
		this.snapshot = snapshot;
	}

	public String getDumpName() {
		return snapshot.getName();
	}

	public File getSnapshot() {
		return snapshot;
	}

	public List<SingleTestResult> getTestData() {
		return singleTestResult;
	}

	public List<String> getErrorMessages() {
		return errorMessages;
	}

	public void addErrorMessage(String message) {
		errorMessages.add(message);
	}

	public void addTestData(SingleTestResult data) {
		singleTestResult.add(data);
	}

	public void addPerfData(PerfData data) {
		perfData.add(data);
	}

	public List<PerfData> getPerfData() {
		return perfData;
	}

	public boolean isSuccessful() {
		if (!errorMessages.isEmpty())
			return false;

		for (SingleTestResult result : singleTestResult) {
			if (!result.isSuccessful())
				return false;
		}

		return true;
	}
}
