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
package cormoran.pepper.io;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * 
 * @author Benoit Lacelle
 * 
 */
public class TestPepperCSVLoadingHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestPepperCSVLoadingHelper.class);

	@Test
	public void testMakePathMatcher() {
		char backSlash = '\\';

		String fileInFolder = "a" + backSlash + "file.csv";
		String fileMatcher = "a" + backSlash + "file.{csv,txt}";
		PathMatcher pathMatcher = PepperFileHelper.makePathMatcher(fileMatcher, true);

		if (SystemUtils.IS_OS_WINDOWS) {
			// Accept Absolute path
			Assert.assertTrue(pathMatcher.matches(Paths.get("C:", "root", fileInFolder)));

			// Reject .csv.bak
			Assert.assertFalse(pathMatcher.matches(Paths.get("C:", "root", fileInFolder + ".bak")));
		} else {
			LOGGER.error("TODO Check this test under Linux");
		}
	}

	@Test
	public void testAdvancedMatch() {
		String fileMatcher = Joiner.on(File.separatorChar).join("[0-9][0-9][0-9][0-9]", "sub", "*.{csv,zip,gz}");
		String[] pathChain = { "root", "Data", "env", "2016", "sub", "name.csv" };

		Path path;
		if (SystemUtils.IS_OS_WINDOWS) {
			path = Paths.get("C:", pathChain);
		} else {
			path = Paths.get("/root", pathChain);
		}

		// Automatic handling of absolute pathes
		Assert.assertTrue(PepperFileHelper.makePathMatcher(fileMatcher, true).matches(path));

		// Path-matcher does not handle absolute pathes
		Assert.assertFalse(PepperFileHelper.makePathMatcher(fileMatcher, false).matches(path));
	}

	@Test
	public void testAdvancedMatchAlreadyAbsolute() {
		String fileMatcher = Joiner.on(File.separatorChar).join("**", "[0-9][0-9][0-9][0-9]", "sub", "*.{csv,zip,gz}");
		String[] pathChain = { "root", "Data", "env", "2016", "sub", "name.csv" };

		Path path;
		if (SystemUtils.IS_OS_WINDOWS) {
			path = Paths.get("C:", pathChain);
		} else {
			path = Paths.get("/root", pathChain);
		}

		Assert.assertTrue(PepperFileHelper.makePathMatcher(fileMatcher, false).matches(path));
		Assert.assertTrue(PepperFileHelper.makePathMatcher(fileMatcher, true).matches(path));
	}

	@Test
	public void testAdvancedMatchAlreadyAbsoluteWindowsMatcher() {
		// We force the use of '\' as separator in matcher
		String fileMatcher = Joiner.on('\\').join("**", "[0-9][0-9][0-9][0-9]", "sub", "*.{csv,zip,gz}");
		String[] pathChain = { "root", "Data", "env", "2016", "sub", "name.csv" };

		Path path;
		if (SystemUtils.IS_OS_WINDOWS) {
			path = Paths.get("C:", pathChain);
		} else {
			path = Paths.get("/root", pathChain);
		}

		Assert.assertTrue(PepperFileHelper.makePathMatcher(fileMatcher, false).matches(path));
		Assert.assertTrue(PepperFileHelper.makePathMatcher(fileMatcher, true).matches(path));
	}

	@Test
	public void testMatchAlreadyGlobbed() {
		String fileMatcher = "glob:*.{csv,zip,gz}";
		Path path = Paths.get("name.csv");

		PathMatcher pathMatcher = PepperFileHelper.makePathMatcher(fileMatcher, false);

		Assert.assertTrue(pathMatcher.matches(path));
	}

}
