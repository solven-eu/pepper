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

public class ForceNewBaselineApplication {
	private File dumpDir;

	public ForceNewBaselineApplication(File dumpDir) {
		this.dumpDir = dumpDir;
	}

	public void run() {

		List<File> dumps = RegTestUtils.collectDumps(dumpDir, new ArrayList<File>());
		for (File dump : dumps) {
			// delete old baseline
			File baselineFolder = new File(dump.getAbsolutePath() + RegTestUtils.BASELINE_EXTENSION);
			if (baselineFolder.exists()) {
				for (File file : baselineFolder.listFiles()) {
					RegTestUtils.removeFile(file);

				}
				RegTestUtils.removeFile(baselineFolder);
			} else {
				System.err.println("Info: Heap dump " + dump.getName() + "has no baseline");
			}

			// rename test result folder into baseline folder
			File testFolder = new File(dump.getAbsolutePath() + RegTestUtils.TEST_EXTENSION);
			if (testFolder.exists() && testFolder.listFiles().length > 0) {
				// create new baseline folder
				File newBaselineFolder = new File(dump.getAbsolutePath() + RegTestUtils.BASELINE_EXTENSION);
				newBaselineFolder.mkdir();
				File[] baselineFiles = testFolder.listFiles();
				for (File baselineFile : baselineFiles) {
					File newBaselineFile = new File(newBaselineFolder, baselineFile.getName());
					boolean succeed = baselineFile.renameTo(newBaselineFile);
					if (succeed) {
						System.out.println("New baseline was created for heap dump: " + dump.getName()
								+ " file: "
								+ baselineFile.getName());
					} else {
						System.err.println("ERROR: Failed overriding the baseline for heap dump: " + dump.getName()
								+ " file: "
								+ baselineFile.getName());
					}
				}
				testFolder.delete();

			} else {
				System.err.println("ERROR: Heap dump " + dump.getName() + " has no test results");
			}
		}

	}
}
