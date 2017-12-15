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

import java.util.Optional;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

public class PepperHostDescriptor {
	protected final String host;
	protected final boolean hostIsIp;
	protected final boolean hostIsValid;

	protected PepperHostDescriptor(String host, boolean hostIsIp, boolean hostIsValid) {
		this.host = host;
		this.hostIsIp = hostIsIp;
		this.hostIsValid = hostIsValid;
	}

	// http://stackoverflow.com/questions/10306690/domain-name-validation-with-regex
	// public static final Pattern DOMAIN_PATTERN =
	// Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}$");
	// We encounter some very long extentions like ".website", ".reviews",
	// "marketing", "properties", "international"
	private static final Pattern DOMAIN_PATTERN = Pattern.compile("^((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,}$");

	// https://stackoverflow.com/questions/15875013/extract-ip-addresses-from-strings-using-regex?rq=1
	private static final Pattern IP_PATTERN = Pattern
			.compile("(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");

	public static Optional<PepperHostDescriptor> parseHost(String host) {
		if (Strings.isNullOrEmpty(host)) {
			// Happens on 'mailto:' for instance
			return Optional.empty();
		}

		boolean hostIsValid = DOMAIN_PATTERN.matcher(host).matches();
		boolean hostIsIp = IP_PATTERN.matcher(host).matches();

		// https://bugs.openjdk.java.net/browse/JDK-8050208
		if (host.endsWith("#")) {
			host = host.substring(0, host.length() - 1);
		}

		String lowerCaseHost = host.toLowerCase();
		return Optional.of(new PepperHostDescriptor(lowerCaseHost, hostIsIp, hostIsValid));
	}

	public boolean getIsIP() {
		return hostIsIp;
	}

	/**
	 * 
	 * @return true if the host is a valid hostname. For instance, a host is not valid if it holds more less than 3
	 *         characters
	 */
	public boolean getIsValid() {
		return hostIsValid;
	}

	public String getHost() {
		return host;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PepperHostDescriptor other = (PepperHostDescriptor) obj;
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "PepperHostDescriptor [host=" + host + ", hostIsIp=" + hostIsIp + ", hostIsValid=" + hostIsValid + "]";
	}

	/**
	 * 
	 * @return the space domain. Given 'http://www.corman.io/youpi', it would return 'cormoran.io'
	 */
	public Optional<String> getHostSpace() {
		if (!getIsValid() || getIsIP()) {
			return Optional.empty();
		}

		String domain = getHost();

		int lastDot = domain.lastIndexOf('.');
		if (lastDot < 0) {
			return Optional.of(domain);
		}
		int beforeLastDot = domain.lastIndexOf('.', lastDot - 1);
		if (beforeLastDot < 0) {
			return Optional.of(domain);
		} else {
			return Optional.of(domain.substring(beforeLastDot + 1));
		}
	}

}
