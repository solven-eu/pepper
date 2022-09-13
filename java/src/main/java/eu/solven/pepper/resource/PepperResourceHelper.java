/**
 * The MIT License
 * Copyright (c) 2022 Benoit Lacelle
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
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ClassUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;

/**
 * Helpers for Unit-tests
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperResourceHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(PepperResourceHelper.class);

	protected PepperResourceHelper() {
		// hidden
	}

	@SuppressWarnings("unchecked")
	public static <T> Resource findResourceForceClassLoader(ClassLoader initialClassLoader, String resourcePath) {
		try {
			Optional<Resource> optResource = Optional.of(new ClassPathResource(resourcePath, initialClassLoader));

			if (!optResource.isPresent() || !optResource.get().exists()) {
				// https://dzone.com/articles/spring-boot-classloader-and-class-override
				// For an unknown reason, in SpringBoot jar, searching for json does not work with (default)
				// AppClassLoader, but it succeeds with its parent
				// org.springframework.boot.loader.LaunchedURLClassLoader
				ClassLoader classLoader = initialClassLoader;
				while (classLoader != null) {
					classLoader = classLoader.getParent();
					optResource = Optional.of(new ClassPathResource(resourcePath, classLoader));

					if (optResource.isPresent() && optResource.get().exists()) {
						LOGGER.info("We loaded {} with {}", resourcePath, classLoader);
						break;
					}
				}
			}

			if (!optResource.isPresent() || !optResource.get().exists()) {
				optResource = tryPath(initialClassLoader, "classpath:" + resourcePath);
			}

			if (!optResource.isPresent() || !optResource.get().exists()) {
				optResource = tryPath(initialClassLoader, "classpath*:" + resourcePath);
			}

			if (!optResource.isPresent() || !optResource.get().exists()) {
				optResource = tryPath(initialClassLoader, "classpath*:" + resourcePath + "*");
			}

			if (!optResource.isPresent() || !optResource.get().exists()) {
				throw new IllegalArgumentException("Can not find: " + resourcePath);
			}

			return optResource.get();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@VisibleForTesting
	static Optional<Resource> tryPath(ClassLoader initialClassLoader, String resourcePath) throws IOException {
		ClassLoader classLoader = initialClassLoader;
		Resource resource = null;
		while (classLoader != null) {
			Resource[] resources = new PathMatchingResourcePatternResolver(classLoader).getResources(resourcePath);
			if (resources.length >= 1) {
				resource = resources[0];
				if (resource.exists()) {
					LOGGER.info("We loaded {} with {}", resourcePath, classLoader);
					break;
				}
				break;
			} else {
				classLoader = classLoader.getParent();
			}
		}
		return Optional.ofNullable(resource);
	}

	public static String loadAsString(String resourcePath) {
		return loadAsString(resourcePath, StandardCharsets.UTF_8);
	}

	public static String loadAsString(String resourcePath, Charset charset) {
		return new String(loadAsBinary(resourcePath), charset);
	}

	public static byte[] loadAsBinary(String resourcePath) {
		ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
		return loadAsBinary(classLoader, resourcePath);
	}

	public static byte[] loadAsBinary(ClassLoader classLoader, String resourcePath) {
		return loadAsBinary(findResourceForceClassLoader(classLoader, resourcePath));
	}

	public static byte[] loadAsBinary(Resource resource) {
		try {
			return ByteStreams.toByteArray(resource.getInputStream());
		} catch (IOException e) {
			throw new UncheckedIOException("Issue on: " + resource, e);
		}
	}
}
