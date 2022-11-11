/*******************************************************************************
 * Copyright (c) 2008,2019 SAP AG and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    Andrew Johnson - Xmx and thread numbers
 *******************************************************************************/
package org.eclipse.mat.tests.regression;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/*package*/class TestSuiteResult {
	private File snapshot;
	private String jvmFlags;
	private List<SingleTestResult> singleTestResult = new ArrayList<SingleTestResult>();
	private List<String> errorMessages = new ArrayList<String>();
	private List<PerfData> perfData = new ArrayList<PerfData>();

	public TestSuiteResult(File snapshot, String jvmFlags) {
		this.snapshot = snapshot;
		this.jvmFlags = jvmFlags;
	}

	public String getDumpName() {
		return snapshot.getName();
	}

	public File getSnapshot() {
		return snapshot;
	}

	public String getJVMflags() {
		return jvmFlags;
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
