/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle
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
package cormoran.pepper.spark;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Assume;

/**
 * Help writing tests for Spark procedure
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperSparkTestHelper {
	protected PepperSparkTestHelper() {
		// hidden
	}

	public static void assumeHadoopEnv() {
		try {
			File winutilsFile = org.apache.hadoop.util.Shell.getWinUtilsFile();
			// If null, then we are missing Haddop env
			Assume.assumeTrue("We seem to be lacking 'hadoop.home.dir' in env property", winutilsFile.isFile());
		} catch (FileNotFoundException e) {
			Assume.assumeFalse("We seem to be lacking 'hadoop.home.dir' in env property", true);
		}

	}
}
