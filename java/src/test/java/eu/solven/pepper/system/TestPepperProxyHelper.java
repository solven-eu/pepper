/**
 * The MIT License
 * Copyright (c) 2023 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.system;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

public class TestPepperProxyHelper {
	final MockEnvironment env = new MockEnvironment();

	@BeforeEach
	@AfterEach
	public void clearEnvironment() {
		System.clearProperty("http.proxyHost");
		System.clearProperty("http.proxyPort");
		System.clearProperty("https.proxyHost");
		System.clearProperty("https.proxyPort");

		System.clearProperty("http.nonProxyHosts");
		System.clearProperty("java.net.useSystemProxies");
	}

	@Test
	public void testHttpProxy_HTTP_http_localhost() {
		env.setProperty("HTTP_PROXY", "http://localhost:3147");

		PepperProxyHelper.setupProxyFromEnvironment(env, false);

		Assertions.assertEquals("localhost", System.getProperty("http.proxyHost"));
		Assertions.assertEquals("3147", System.getProperty("http.proxyPort"));
		Assertions.assertEquals(null, System.getProperty("https.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("https.proxyPort"));
		Assertions.assertEquals(null, System.getProperty("http.nonProxyHosts"));
		Assertions.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}

	@Test
	public void testHttpProxy_HTTP_http_alreadySet() {
		env.setProperty("http.proxyHost", "someProxyHost");
		env.setProperty("HTTP_PROXY", "http://localhost:3147");

		PepperProxyHelper.setupProxyFromEnvironment(env, false);

		Assertions.assertEquals("someProxyHost", env.getProperty("http.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("http.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("http.proxyPort"));
		Assertions.assertEquals(null, System.getProperty("https.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("https.proxyPort"));
		Assertions.assertEquals(null, System.getProperty("http.nonProxyHosts"));
		Assertions.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}

	@Test
	public void testHttpProxy_HTTPS_https_remote() {
		env.setProperty("HTTPS_PROXY", "https://someproxy:1234");

		PepperProxyHelper.setupProxyFromEnvironment(env, false);

		Assertions.assertEquals(null, System.getProperty("http.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("http.proxyPort"));
		Assertions.assertEquals("someproxy", System.getProperty("https.proxyHost"));
		Assertions.assertEquals("1234", System.getProperty("https.proxyPort"));
		Assertions.assertEquals(null, System.getProperty("http.nonProxyHosts"));
		Assertions.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}

	@Test
	public void testHttpProxy_HTTPS_https_alreadySet() {
		env.setProperty("https.proxyHost", "someProxyHost");
		env.setProperty("HTTPS_PROXY", "https://someproxy:1234");

		PepperProxyHelper.setupProxyFromEnvironment(env, false);

		Assertions.assertEquals(null, System.getProperty("http.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("http.proxyPort"));

		Assertions.assertEquals("someProxyHost", env.getProperty("https.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("https.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("https.proxyPort"));
		Assertions.assertEquals(null, System.getProperty("http.nonProxyHosts"));
		Assertions.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}

	@Test
	public void testHttpProxy_noProxy() {
		env.setProperty("NO_PROXY", "a,b");

		PepperProxyHelper.setupProxyFromEnvironment(env, false);

		Assertions.assertEquals(null, System.getProperty("http.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("http.proxyPort"));
		Assertions.assertEquals(null, System.getProperty("https.proxyHost"));
		Assertions.assertEquals(null, System.getProperty("https.proxyPort"));
		Assertions.assertEquals("a|b", System.getProperty("http.nonProxyHosts"));
		Assertions.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}
}
