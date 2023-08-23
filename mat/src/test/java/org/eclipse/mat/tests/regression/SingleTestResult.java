/*******************************************************************************
 * Copyright (c) 2008, 2023 SAP AG, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - additional debug information
 *    Netflix (Jason Koch) - refactors for increased performance and concurrency
 *******************************************************************************/
package org.eclipse.mat.tests.regression;

import java.util.ArrayList;
import java.util.List;

/*package*/class SingleTestResult {
	private String testName;
	private String result;
	private List<Difference> differences;

	public SingleTestResult(String testName, String result, List<Difference> differences) {
		this.testName = testName;
		this.result = result;
		this.differences = differences;
	}

	public String getTestName() {
		return testName;
	}

	public String getResult() {
		return result;
	}

	public List<Difference> getDifferences() {
		if (differences == null) {
			return new ArrayList<Difference>(0);
		} else {
			return differences;
		}
	}

	public boolean isSuccessful() {
		return differences == null || differences.isEmpty();
	}

}
