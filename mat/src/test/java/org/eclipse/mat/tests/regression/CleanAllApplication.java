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

import java.io.File;

public class CleanAllApplication {
	private File dumpDir;

	public CleanAllApplication(File dumpDir) {
		this.dumpDir = dumpDir;
	}

	public void run() throws Exception {
		remove(dumpDir);
	}

	private void remove(File dir) {
		File[] filesToRemove = dir.listFiles(RegTestUtils.cleanupFilter);
		for (File file : filesToRemove) {
			if (file.isDirectory()) {
				remove(file);
				if (file.listFiles().length == 0)
					RegTestUtils.removeFile(file);
			} else {
				RegTestUtils.removeFile(file);
			}
		}
	}

}
