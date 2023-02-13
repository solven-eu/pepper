/**
 * The MIT License
 * Copyright (c) 2008-2012 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.tests.snapshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.tests.TestSnapshots;
import org.eclipse.mat.util.MessageUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class TestInstanceSizes {
	private static final Pattern PATTERN_OBJ_ARRAY = Pattern.compile("^(\\[+)L(.*);$");
	private static final Pattern PATTERN_PRIMITIVE_ARRAY = Pattern.compile("^(\\[+)(.)$");

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(
				new Object[][] { { TestSnapshots.HISTOGRAM_SUN_JDK5_13_32BIT, TestSnapshots.SUN_JDK5_13_32BIT, 1 },
						{ TestSnapshots.HISTOGRAM_SUN_JDK6_18_32BIT, TestSnapshots.SUN_JDK6_18_32BIT, 0 },
						{ TestSnapshots.HISTOGRAM_SUN_JDK6_18_64BIT, TestSnapshots.SUN_JDK6_18_64BIT, 0 },
						{ TestSnapshots.HISTOGRAM_SUN_JDK6_30_64BIT_COMPRESSED_OOPS,
								TestSnapshots.SUN_JDK6_30_64BIT_COMPRESSED_OOPS,
								0 },
						{ TestSnapshots.HISTOGRAM_SUN_JDK6_30_64BIT_NOCOMPRESSED_OOPS,
								TestSnapshots.SUN_JDK6_30_64BIT_NOCOMPRESSED_OOPS,
								0 }, });
	}

	String hist;
	String dump;
	int expectedErrors;
	private File histogramFile;
	private ISnapshot snapshot;

	public TestInstanceSizes(String histogram, String fn, int e) {
		hist = histogram;
		dump = fn;
		histogramFile = TestSnapshots.getResourceFile(hist);
		expectedErrors = e;
		Map<String, String> options = new HashMap<String, String>();
		options.put("keep_unreachable_objects", "true");
		snapshot = TestSnapshots.getSnapshot(dump, options, true);
	}

	@Test
	public void testHistogramSizes() throws Exception {
		doTest(histogramFile, snapshot);
	}

	/**
	 * Convert jmap histogram class name to MAT name See {@link org.eclipse.mat.hprof.Pass1Parser}.
	 *
	 * @param className
	 * @return
	 */
	private String fixArrayName(String className) {
		if (className.charAt(0) == '[') // quick check if array at hand
		{
			// fix object class names
			Matcher matcher = PATTERN_OBJ_ARRAY.matcher(className);
			if (matcher.matches()) {
				int l = matcher.group(1).length();
				className = matcher.group(2);
				for (int ii = 0; ii < l; ii++)
					className += "[]";
			}

			// primitive arrays
			matcher = PATTERN_PRIMITIVE_ARRAY.matcher(className);
			if (matcher.matches()) {
				int count = matcher.group(1).length() - 1;
				className = "unknown[]";

				char signature = matcher.group(2).charAt(0);
				for (int ii = 0; ii < IPrimitiveArray.SIGNATURES.length; ii++) {
					if (IPrimitiveArray.SIGNATURES[ii] == (byte) signature) {
						className = IPrimitiveArray.TYPE[ii];
						break;
					}
				}

				for (int ii = 0; ii < count; ii++)
					className += "[]";
			}
		}
		return className;
	}

	private void doTest(File histogramFile, ISnapshot snapshot) throws Exception {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(histogramFile));

			String line;
			int errorCount = 0;
			StringBuilder errorMessage = new StringBuilder();

			while ((line = in.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				String firstToken = tokenizer.nextToken();
				if (firstToken.indexOf(':') != -1)
					firstToken = tokenizer.nextToken();
				int numObjects = Integer.parseInt(firstToken);
				long shallowSize = Long.parseLong(tokenizer.nextToken());
				long instanceSize = shallowSize / numObjects;
				String className = tokenizer.nextToken();
				// System.out.println(className + " " + instanceSize);
				className = fixArrayName(className);

				if (className.startsWith("<") || "java.lang.Class".equals(className))
					continue;

				Collection<IClass> classes = snapshot.getClassesByName(className, false);
				if (classes == null || classes.size() == 0) {
					System.out.println(MessageUtil.format("Cannot find class [{0}] in heap dump", className));
					continue;
				}
				IClass clazz = classes.iterator().next();
				if (clazz.isArrayType()) {
					if (numObjects == clazz.getNumberOfObjects()) {
						int o[] = clazz.getObjectIds();
						long actual = snapshot.getHeapSize(o);
						if (actual != shallowSize) {
							errorCount++;
							errorMessage.append(MessageUtil.format(
									"Array class {0} expected total instances size {1} but got {2}\r\n",
									className,
									shallowSize,
									actual));
						}
					}
				} else {
					if (clazz.getHeapSizePerInstance() != instanceSize) {
						errorCount++;
						errorMessage.append(MessageUtil.format("Class [{0}] expected size {1} but got {2}\r\n",
								className,
								instanceSize,
								clazz.getHeapSizePerInstance()));
					}
				}
			}

			Assert.assertEquals(
					"Unexpected number of failures: for the following classes the instance size isn't correct\r\n"
							+ errorMessage.toString(),
					expectedErrors,
					errorCount);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ignore) {
					// ignore
				}
			}

		}
	}

}
