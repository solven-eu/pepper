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
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.List;
import java.util.regex.Pattern;

public class RegTestUtils {
	static final String BASELINE_EXTENSION = "_baseline";
	static final String TEST_EXTENSION = "_test";
	static final String RESULT_FILENAME = "result.xml";
	public static final String SEPARATOR = ";";

	private static FileFilter filter = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return (file.getName().endsWith(".hprof") || file.getName().endsWith(".dtfj")
					|| file.getName().endsWith(".dmp.zip"));
		}
	};

	public static final FilenameFilter cleanupFilter = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			Pattern hprofPattern = Pattern.compile(".*\\.hprof");
			Pattern dtfjPattern = Pattern.compile(".*\\.dtfj|.*\\.dmp.zip|.*\\.phd|javacore.*\\.txt");
			Pattern resultFilePattern = Pattern.compile("performanceResults.*\\.csv");
			return !hprofPattern.matcher(name).matches() && !name.endsWith(BASELINE_EXTENSION)
					&& !dtfjPattern.matcher(name).matches()
					&& !resultFilePattern.matcher(name).matches();
		}
	};

	protected static void removeFile(File file) {
		// delete old index and report files, throw exception if fails
		if (!file.delete()) {

			System.err.println(
					"ERROR: Failed to remove file " + file.getName() + " from the directory " + file.getParent());
		}
	}

	protected static List<File> collectDumps(File dumpsFolder, List<File> dumpList) {
		File[] dumps = dumpsFolder.listFiles(filter);
		for (File file : dumps) {
			dumpList.add(file);
		}

		// check whether sub-folders contain heap dumps

		File[] directories = dumpsFolder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});

		for (File dir : directories) {
			collectDumps(dir, dumpList);
		}

		return dumpList;
	}

}
