/**
 * The MIT License
 * Copyright (c) 2021 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.resource;

import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class TestPepperResourceHelper {
	private static final ClassLoader CLASS_LOADER = Thread.class.getClassLoader();

	@Test
	public void testLoadAsString() {
		Assertions.assertThat(PepperResourceHelper.loadAsString("/unittest/list.json")).contains("\"a\"", "\"b\"");
	}

	@Test
	public void loadAsString() {
		Assertions.assertThat(PepperResourceHelper.loadAsString("/unittest/map.json")).contains("\"k\"", "\"v\"");
	}

	@Test
	public void testLoadAsList_forceClassLoader() throws IOException {
		Resource resource = PepperResourceHelper.findResourceForceClassLoader(CLASS_LOADER, "/unittest/list.json");
		Assert.assertEquals("list.json", resource.getFilename());
		Assertions.assertThat(resource.getURI().toASCIIString()).endsWith("/unittest/list.json");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoadAsList_forceClassLoader_doesNotExist() {
		PepperResourceHelper.findResourceForceClassLoader(CLASS_LOADER, "/unittest/doesnotexist.json");
	}

	@Test
	public void testTryPath() throws IOException {
		// Just for coverage
		Assertions.assertThat(PepperResourceHelper.tryPath(CLASS_LOADER, "classpath:/unittest/list.json")).isEmpty();
	}

	@Test
	public void testLoadBinary() throws IOException {
		byte[] bytes = PepperResourceHelper.loadAsBinary("/unittest/list.json");
		if ("\r\n".equals(System.lineSeparator())) {
			// Git will checkout this file with '\r\n': binary will be longer
			Assertions.assertThat(bytes).hasSize(25);
		} else {
			Assertions.assertThat(bytes).hasSize(21);
		}
	}
}
