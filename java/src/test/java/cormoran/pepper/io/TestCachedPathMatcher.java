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

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TestCachedPathMatcher {
	@Test
	public void testCache() {
		PathMatcher decorated = Mockito.mock(PathMatcher.class);
		CachedPathMatcher cachedMatcher = new CachedPathMatcher(decorated, "someRegex");

		Path path = Mockito.mock(Path.class);

		// Return true then false: as we cache, we should always return true
		Mockito.when(decorated.matches(path)).thenReturn(true, false);

		// First try: we get true
		Assert.assertTrue(cachedMatcher.matches(path));

		// Second try: we get cached true
		Assert.assertTrue(cachedMatcher.matches(path));

		// Ensure underlying is actually false
		Assert.assertFalse(decorated.matches(path));
		Assert.assertTrue(cachedMatcher.matches(path));
	}

	@Test
	public void testHumanToString_Regex() {
		PathMatcher rawMatcher = FileSystems.getDefault().getPathMatcher("regex:");
		PathMatcher cachedPathMatcher = CachedPathMatcher.fromRegex("Prefix.*");

		// Demonstrate we provide a useful toString while jdk class misses a useful .toString
		Assert.assertFalse(rawMatcher.toString().contains("Prefix"));
		Assert.assertEquals("regex:Prefix.*", cachedPathMatcher.toString());
	}

	@Test
	public void testHumanToString_Glob() {
		PathMatcher rawMatcher = FileSystems.getDefault().getPathMatcher("regex:");
		PathMatcher cachedPathMatcher = CachedPathMatcher.fromRegex("Prefix.*");

		// Demonstrate we provide a useful toString while jdk class misses a useful .toString
		Assert.assertFalse(rawMatcher.toString().contains("Prefix"));
		Assert.assertEquals("regex:Prefix.*", cachedPathMatcher.toString());
	}
}
