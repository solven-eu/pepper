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
package cormoran.pepper.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods for Properties
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperPropertiesHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperPropertiesHelper.class);

	protected PepperPropertiesHelper() {
		// hidden
	}

	public static void checkProperties(Properties properties) {
		checkProperties(properties, Collections.emptySet());
	}

	public static void checkProperties(Properties properties, Set<String> exceptionsKeys) {
		if (properties != null) {
			try {
				Enumeration<?> keys = properties.propertyNames();

				while (keys.hasMoreElements()) {
					Object key = keys.nextElement();
					Object value = properties.get(key);

					if (value == null) {
						if (key instanceof String) {
							// Try to find the value in default properties
							String valueAsString = properties.getProperty((String) key);

							if (valueAsString == null) {
								// The not-String object in the default
								// properties
								// is not accessible
								LOGGER.warn(
										"It is unsafe to associate the key {} to !String (in base or default Properties)",
										key);
							}
						} else {
							LOGGER.warn("It is unsafe to associate the key {} to null", key);
						}
					} else if (!(value instanceof String)) {
						if (exceptionsKeys.contains(key)) {
							LOGGER.debug("It is unsafe to associate the key {} to !String: {}({})",
									key,
									value.getClass(),
									value);
						} else {
							LOGGER.warn("It is unsafe to associate the key {} to !String: {}({})",
									key,
									value.getClass(),
									value);
						}
					}
				}
			} catch (Exception e) {
				LOGGER.warn("Something is wrong in properties: " + properties, e);
			}
		}
	}
}
