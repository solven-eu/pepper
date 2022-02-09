package eu.solven.pepper.proxy;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import eu.solven.pepper.proxy.PepperProxyHelper;

public class TestPepperProxyHelper {
	final MockEnvironment env = new MockEnvironment();

	@Before
	@After
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

		Assert.assertEquals("localhost", System.getProperty("http.proxyHost"));
		Assert.assertEquals("3147", System.getProperty("http.proxyPort"));
		Assert.assertEquals(null, System.getProperty("https.proxyHost"));
		Assert.assertEquals(null, System.getProperty("https.proxyPort"));
		Assert.assertEquals(null, System.getProperty("http.nonProxyHosts"));
		Assert.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}

	@Test
	public void testHttpProxy_HTTP_http_alreadySet() {
		env.setProperty("http.proxyHost", "someProxyHost");
		env.setProperty("HTTP_PROXY", "http://localhost:3147");

		PepperProxyHelper.setupProxyFromEnvironment(env, false);

		Assert.assertEquals("someProxyHost", env.getProperty("http.proxyHost"));
		Assert.assertEquals(null, System.getProperty("http.proxyHost"));
		Assert.assertEquals(null, System.getProperty("http.proxyPort"));
		Assert.assertEquals(null, System.getProperty("https.proxyHost"));
		Assert.assertEquals(null, System.getProperty("https.proxyPort"));
		Assert.assertEquals(null, System.getProperty("http.nonProxyHosts"));
		Assert.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}

	@Test
	public void testHttpProxy_HTTPS_https_remote() {
		env.setProperty("HTTPS_PROXY", "https://someproxy:1234");

		PepperProxyHelper.setupProxyFromEnvironment(env, false);

		Assert.assertEquals(null, System.getProperty("http.proxyHost"));
		Assert.assertEquals(null, System.getProperty("http.proxyPort"));
		Assert.assertEquals("someproxy", System.getProperty("https.proxyHost"));
		Assert.assertEquals("1234", System.getProperty("https.proxyPort"));
		Assert.assertEquals(null, System.getProperty("http.nonProxyHosts"));
		Assert.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}

	@Test
	public void testHttpProxy_HTTPS_https_alreadySet() {
		env.setProperty("https.proxyHost", "someProxyHost");
		env.setProperty("HTTPS_PROXY", "https://someproxy:1234");

		PepperProxyHelper.setupProxyFromEnvironment(env, false);

		Assert.assertEquals(null, System.getProperty("http.proxyHost"));
		Assert.assertEquals(null, System.getProperty("http.proxyPort"));

		Assert.assertEquals("someProxyHost", env.getProperty("https.proxyHost"));
		Assert.assertEquals(null, System.getProperty("https.proxyHost"));
		Assert.assertEquals(null, System.getProperty("https.proxyPort"));
		Assert.assertEquals(null, System.getProperty("http.nonProxyHosts"));
		Assert.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}

	@Test
	public void testHttpProxy_noProxy() {
		env.setProperty("NO_PROXY", "a,b");

		PepperProxyHelper.setupProxyFromEnvironment(env, false);

		Assert.assertEquals(null, System.getProperty("http.proxyHost"));
		Assert.assertEquals(null, System.getProperty("http.proxyPort"));
		Assert.assertEquals(null, System.getProperty("https.proxyHost"));
		Assert.assertEquals(null, System.getProperty("https.proxyPort"));
		Assert.assertEquals("a|b", System.getProperty("http.nonProxyHosts"));
		Assert.assertEquals("true", System.getProperty("java.net.useSystemProxies"));
	}
}
