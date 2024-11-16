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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import com.google.common.base.Strings;

/**
 * This class enables filling -Dhttp.proxy and related JVM variables from standard Environment variables
 *
 * @author Benoit Lacelle
 * @see java.net.Proxy
 * @see java.net.Authenticator
 */
public class PepperProxyHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(PepperProxyHelper.class);

	private static final String AFTER_SCHEME = "://";

	protected PepperProxyHelper() {
		// hidden
	}

	public static void setupProxyFromEnvironment() {
		setupProxyFromEnvironment(new StandardEnvironment(), true);
	}

	// https://docs.microsoft.com/fr-fr/azure/developer/java/sdk/proxying
	// Relates with com.microsoft.azure.storage.OperationContext.setProxy(Proxy)
	// Relates with com.azure.core.http.ProxyOptions.attemptToLoadAzureProxy(Configuration, String)
	public static void setupProxyFromEnvironment(Environment env, boolean checkProxy) {
		// This seems not enough within SG environment
		// Still, it seems mandatory to get proxy configuring itself
		// see https://docs.microsoft.com/fr-fr/azure/developer/java/sdk/proxying
		// see reactor.netty.tcp.ProxyProvider.findProxySupport(Bootstrap)
		System.setProperty("java.net.useSystemProxies", "true");

		// https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html
		// https://stackoverflow.com/questions/58559109/difference-between-http-proxy-and-https-proxy
		Stream.of("http", "https").forEach(scheme -> {
			String envKey = scheme.toUpperCase(Locale.US) + "_PROXY";
			String standardProxy = env.getProperty(envKey);
			if (Strings.isNullOrEmpty(standardProxy)) {
				LOGGER.debug("There is no proxy info for scheme={} in environment variables (key={})", scheme, envKey);
			} else if (!Strings.isNullOrEmpty(env.getProperty(scheme + ".proxyHost"))) {
				LOGGER.debug("There is already proxy info in java properties for scheme={}", scheme);
			} else {
				String rawUrl;
				if (standardProxy.contains(AFTER_SCHEME)) {
					rawUrl = standardProxy;
				} else {
					// com.azure.core.http.ProxyOptions.attemptToLoadAzureProxy(Configuration,
					// String)
					// Some proxy interpreter requires a scheme in HTTP_PROXY
					rawUrl = scheme + AFTER_SCHEME + standardProxy;
				}

				URL proxy;
				try {
					proxy = new URL(rawUrl);
				} catch (MalformedURLException e) {
					throw new IllegalStateException(e);
				}
				String host = proxy.getHost();
				System.setProperty(scheme + ".proxyHost", host);
				int port = proxy.getPort();
				System.setProperty(scheme + ".proxyPort", Integer.toString(proxy.getPort()));

				if (checkProxy) {
					queryProxyDirectly(scheme, host, port);
				}
			}
		});

		// https://azure.github.io/azure-sdk/general_azurecore.html
		// We may also configure ALL_PROXY

		// https://docs.oracle.com/javase/8/docs/technotes/guides/net/proxies.html
		// Both http and https proxies relies on the same http variable for no_proxy
		{
			String noProxyHosts = env.getProperty("NO_PROXY");
			if (!Strings.isNullOrEmpty(noProxyHosts) && Strings.isNullOrEmpty(env.getProperty("http.nonProxyHosts"))) {
				String[] proxy = noProxyHosts.split(",");
				System.setProperty("http.nonProxyHosts", Stream.of(proxy).collect(Collectors.joining("|")));
			}
		}
	}

	public static void checkConnectivity(String rawUrl) {
		try {
			LOGGER.debug("Before checking proxy over url={}", rawUrl);

			URL url = new URL(rawUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			connection.getInputStream();
			LOGGER.info("OK checking proxy over url={}", rawUrl);
		} catch (IOException e) {
			if (e.getMessage().contains("403")) {
				LOGGER.trace("Expected 403 as KeyVault is protected", e);
				LOGGER.info("OK checking proxy over url={}", rawUrl);
			} else {
				throw new UncheckedIOException("Unclear if Proxy configuration is OK", e);
			}
		}
	}

	private static void queryProxyDirectly(String scheme, String proxyHost, int proxyPort) {
		LOGGER.info("Before proxy call: {} {} {}", scheme, proxyHost, proxyPort);
		URL proxyUrl;
		try {
			proxyUrl = new URL(scheme + AFTER_SCHEME + proxyHost + ":" + proxyPort);
		} catch (MalformedURLException e1) {
			throw new IllegalArgumentException(e1);
		}
		LOGGER.info("Testing proxy: {}", proxyUrl.toExternalForm());

		try {
			proxyUrl.openConnection().getInputStream();
			LOGGER.info("Validated proxy: {}", proxyUrl.toExternalForm());
		} catch (SocketException e) {
			if ("Unexpected end of file from server".equals(e.getMessage())) {
				LOGGER.trace("Expected issue while querying the proxy directly", e);
			} else {
				LOGGER.warn("Unclear if expected issue while querying the proxy directly", e);
			}
		} catch (SSLException e) {
			// In https, we receive an SSLException when connecting the proxy: it is
			// relevant as we query without SSL, but it then does not guarantee the
			// configuration is OK
			LOGGER.info("Seemingly validated proxy: {}", proxyUrl.toExternalForm());
		} catch (IOException e) {
			// Typically happens while doing https through an http proxy
			LOGGER.warn("Unclear if expected issue while querying the proxy directly", e);
		}
	}
}
