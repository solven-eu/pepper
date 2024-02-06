/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.util;

import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;

/**
 * Various helpers related to current environment.
 *
 * @author Benoit Lacelle
 *
 */
// @Beta as the class name and package may be inappropriate
@Beta
public class PepperEnvHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperEnvHelper.class);

	protected PepperEnvHelper() {
		// hidden
	}

	public static boolean inUnitTest() {
		// In maven: org.apache.maven.surefire.booter.ForkedBooter.exit(ForkedBooter.java:144)
		// Bean disposing is expected to be done in the main thread: does this main thread comes from junit or surefire?
		// SpringBootTest close the AppContext in a different thread, through an ApplicationShutdownHooks

		Optional<StackTraceElement> matching = Thread.getAllStackTraces()
				.values()
				.stream()
				.flatMap(Stream::of)
				// Cucumber uses `cucumber` as top-package: no leading `.`
				.filter(ste -> Stream.of(".surefire.", ".failsafe.", ".junit.", "cucumber.")
						.anyMatch(name -> ste.getClassName().contains(name)))
				.findAny();

		matching.ifPresent(ste -> LOGGER.info("We have detected a unit-test with: {}", ste));

		return matching.isPresent();
	}
}
