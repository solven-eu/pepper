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
package cormoran.pepper.core.io;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enable caching of regex resolution against a Path. It is especially useful when a directory with many files is
 * checked very regularly
 * 
 * @author Benoit Lacelle
 *
 */
public class CachedPathMatcher implements PathMatcher {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CachedPathMatcher.class);

	// We use String as cacheKey to prevent maintaining any FS object
	protected final Map<String, Boolean> alreadyLogged = new ConcurrentHashMap<>();

	protected final PathMatcher decorated;
	protected final String pattern;

	public CachedPathMatcher(PathMatcher decorated, String pattern) {
		this.decorated = decorated;
		this.pattern = pattern;
	}

	@Override
	public boolean matches(Path path) {
		String cacheKey = path.toString();

		boolean match = alreadyLogged.computeIfAbsent(cacheKey, key -> {
			boolean matches = decorated.matches(path);

			// Prevent logging too often: we log in debug only if adding in the cache
			LOGGER.debug("PathMatcher {} on {} returned {}", pattern, path, matches);

			return matches;
		});

		// Log in trace anyway (it will log twice on the first encounter)
		LOGGER.trace("PathMatcher {} on {} returned {}", pattern, path, match);

		return match;
	}

	@Override
	public String toString() {
		return pattern;
	}
}
