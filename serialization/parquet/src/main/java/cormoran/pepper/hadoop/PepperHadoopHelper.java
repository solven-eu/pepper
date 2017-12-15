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
package cormoran.pepper.hadoop;

import java.io.FileNotFoundException;

import org.apache.hadoop.util.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Some basic utilities for Hadoop
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperHadoopHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperHadoopHelper.class);

	protected PepperHadoopHelper() {
		// hidden
	}

	/**
	 * 
	 * @return true if we already have the property env "hadoop.home.dir", or we succeed finding a good value for it
	 */
	public static boolean isHadoopReady() {
		if (Shell.WINDOWS) {
			try {
				if (Shell.getWinUtilsFile().isFile()) {
					return true;
				}
			} catch (FileNotFoundException e) {
				// https://wiki.apache.org/hadoop/WindowsProblems
				LOGGER.trace("Wintutils seems to be missing", e);
			}
		}

		// If we get here, it means winutils is missing
		LOGGER.error(
				"Haddop winutils seems not installed. They can be checked-out from 'git clone https://github.com/steveloughran/winutils.git'");
		return false;
	}
}
