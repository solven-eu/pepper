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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;

public class TestPepperFileHelper {
	@Test
	public void testCreateTempPath() throws IOException {
		Path tmpFile = PepperFileHelper.createTempPath("apex.test", ".csv", true);

		// Check the path does not exist
		Assert.assertFalse(tmpFile.toFile().exists());
	}

	@Test
	public void testNoNewLine() {
		Assert.assertEquals("A B C D", PepperFileHelper.cleanWhitespaces("A\tB  C\rD"));
	}

	@Test
	public void testExpandJarToDisk() throws IOException, URISyntaxException {
		// Choose a class in a small jar so the test remains fast
		String pathToResourceInJar = "/org/slf4j/Logger.class";
		URL resource = PepperFileHelper.getResourceURL(pathToResourceInJar);

		Path jarPath = PepperFileHelper.getHoldingJarPath(resource.toURI()).get();

		Path tmpPath = PepperFileHelper.createTempPath("apex", "testExpandJarToDisk", true);
		PepperFileHelper.expandJarToDisk(jarPath, tmpPath);

		Assert.assertTrue(new File(tmpPath.toFile(), pathToResourceInJar).exists());
	}

	@Test
	public void testURISpecialCharacters() throws IOException, URISyntaxException {
		// '@' is a special characters leading to issues when converting back and forth to URL
		Path file = File.createTempFile("TestApexAgentHelper", "special@char").toPath();

		URI asURI = file.toUri();
		URL asURL = asURI.toURL();

		File backToFile = new File(asURI);
		File backToFile2 = new File(asURI.getPath());
		File backToFile3 = new File(asURL.toURI().getPath());

		Assert.assertEquals(file, backToFile.toPath());
		Assert.assertEquals(file, backToFile2.toPath());
		Assert.assertEquals(file, backToFile3.toPath());
	}
}
