/*
 * Copyright © 2021 M-iTrust (cto@m-itrust.com). Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential
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
