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

import java.nio.file.Path;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TestPepperFileHelperInSpringContext.class)
public class TestPepperFileHelperInSpringContext {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestPepperFileHelperInSpringContext.class);

	@Autowired
	private ResourceLoader resourceLoader;

	// Nothing is magic: a folder alone is resolved relatively to process root path
	@Test
	public void testResolveToPath_FolderInResources() {
		Path path = PepperFileHelper.resolveToPath(resourceLoader, "TEST_DATA");
		Assert.assertTrue("Failed on " + path, path.toFile().isDirectory());
	}

	@Test
	public void testResolveToPath_FolderInResources_FromProcessRoot() {
		Path path = PepperFileHelper.resolveToPath(resourceLoader, "src/test/resources/TEST_DATA");
		Assert.assertTrue("Failed on " + path, path.toFile().isDirectory());
	}

	@Test
	public void testResolveToPath_SpringClassPath_folder() {
		Path path = PepperFileHelper.resolveToPath(resourceLoader, "classpath:TEST_DATA");
		Assert.assertTrue("Failed on " + path, path.toFile().isDirectory());
	}

	@Test
	public void testResolveToPath_SpringClassPath_file() {
		Path path = PepperFileHelper.resolveToPath(resourceLoader, "classpath:TEST_DATA/empty.csv");
		Assert.assertTrue("Failed on " + path, path.toFile().isFile());
	}

	@Test
	public void testResolveToPath_SpringClassPath_PrefixSlash_folder() {
		Path path = PepperFileHelper.resolveToPath(resourceLoader, "classpath:/TEST_DATA");
		Assert.assertTrue("Failed on " + path, path.toFile().isDirectory());
	}

	@Test
	public void testResolveToPath_SpringClassPath_PrefixSlash_file() {
		Path path = PepperFileHelper.resolveToPath(resourceLoader, "classpath:/TEST_DATA/empty.csv");
		Assert.assertTrue("Failed on " + path, path.toFile().isFile());
	}
}
