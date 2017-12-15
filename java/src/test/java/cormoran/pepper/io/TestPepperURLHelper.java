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

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class TestPepperURLHelper {
	@Test
	public void testEqualsURL() throws MalformedURLException {
		URL left = new URL("http://youpi.com");
		URL right = new URL("http://youpi.com");
		Assert.assertTrue(PepperURLHelper.equalsUrl(left, right));
	}

	@Test
	public void testToUrl() throws MalformedURLException {
		Assert.assertEquals("http://youpi.com", PepperURLHelper.toHttpURL("youpi.com").toExternalForm());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToUrl_Null() throws MalformedURLException {
		PepperURLHelper.toHttpURL(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testToUrl_Empty() throws MalformedURLException {
		PepperURLHelper.toHttpURL("");
	}

	@Test
	public void testGetHost_lowerCase() throws MalformedURLException {
		PepperHostDescriptor host = PepperURLHelper.getHost("YOUpi.com").get();

		Assert.assertFalse(host.getIsIP());
		Assert.assertTrue(host.getIsValid());
		Assert.assertEquals("youpi.com", host.getHost());
	}

	@Test
	public void testGetHost_hashRightAfterHost() {
		// In some JDK version, "http://host.com#youpi" as parsed as URL "http://host.com##youpi". It seems
		// "http://host.com/#youpi" is parsed correctly

		PepperHostDescriptor host = PepperURLHelper.getHost("http://youpi.com#arf").get();

		Assert.assertFalse(host.getIsIP());
		Assert.assertTrue(host.getIsValid());
		Assert.assertEquals("youpi.com", host.getHost());
	}

	@Test
	public void testToUrl_mailto() {
		URL host = PepperURLHelper.toHttpURL("mailto:adresse@serveur.com");

		Assert.assertEquals("mailto:adresse@serveur.com", host.toExternalForm());
	}

	@Test
	public void testGetHost_mailto() {
		Assert.assertFalse(PepperURLHelper.getHost("mailto:adresse@serveur.com").isPresent());
	}

	@Test
	public void testExtractDomainNamespace() {
		Assert.assertEquals("amazon.fr", PepperURLHelper.getHost("www.amazon.fr").get().getHostSpace().get());
	}

	@Test
	public void testDomainIsNamespace() {
		Assert.assertEquals("amazon.fr", PepperURLHelper.getHost("www.amazon.fr").get().getHostSpace().get());
	}

	@Test
	public void testExtractDomainNamespaceStartWithDot() {
		Assert.assertFalse(PepperURLHelper.getHost(".www.amazon.fr").get().getHostSpace().isPresent());
	}

	@Test
	public void testRebuildLink_main() throws MalformedURLException {
		Assert.assertEquals("http://youpi.com/arg", PepperURLHelper.resolve("http://youpi.com/grumph", "arg"));
		Assert.assertEquals("http://youpi.com/arg", PepperURLHelper.resolve("http://youpi.com/grumph", "/arg"));
	}

	@Test
	public void testRebuildLink_folder() throws MalformedURLException {
		Assert.assertEquals("http://youpi.com/foo/arg", PepperURLHelper.resolve("http://youpi.com/foo/bar", "arg"));
		Assert.assertEquals("http://youpi.com/arg", PepperURLHelper.resolve("http://youpi.com/foo/bar?glu", "/arg"));
	}

}
