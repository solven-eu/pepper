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
package eu.solven.pepper.hack;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Help management of hacked classes. Such classes should be very rare, but sometimes, it appears necessary or much
 * simpler than alternatives
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperHackHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(PepperHackHelper.class);

	protected PepperHackHelper() {
		// hidden
	}

	public static void checkHackedFile(Class<?> clazz, String file, String hackedFile) throws Error {
		String actualFile = new File(file).getName();

		if (actualFile.contains("classes")) {
			LOGGER.debug("Unit-test?");
		} else if (!actualFile.startsWith(hackedFile)) {
			// Under Azure/Linux, we may receive a actualFile as hackedFile suffixed by '!'
			throw new Error(clazz + " has been hacked over '" + hackedFile + "' but the jar is '" + actualFile + "'");
		}
	}

}
