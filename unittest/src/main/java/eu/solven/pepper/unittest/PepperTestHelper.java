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
package eu.solven.pepper.unittest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.jupiter.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Helpers for Unit-tests
 *
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings("PMD.MoreThanOneLogger")
@Slf4j
public class PepperTestHelper {
	protected PepperTestHelper() {
		// hidden
	}

	/**
	 * This will check internet (global) is available by checking the connectivity to a global resource expected to be
	 * always UP (e.g. google.com)
	 *
	 * @return true if the internet is available
	 */
	public static boolean assumeInternetIsAvailable() {
		URL url;
		try {
			url = new URL("https://google.com");
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Issue parsing an https url", e);
		}
		return assumeDomainIsAvailable(url);
	}

	public static boolean assumeDomainIsAvailable(URL url) {
		try {
			URLConnection connection = url.openConnection();
			Assumptions.assumeFalse(connection == null, "null URLConnection");
			// We check some data from the connection as we may receive a not connection connection
			Assumptions.assumeFalse(null == connection.getContentType());

			return true;
		} catch (RuntimeException | IOException e) {
			log.trace("Internet is not available", e);
			Assumptions.abort("Internet is not available");
			return false;
		}
	}

	public static ILogDisabler disableLog(Class<?> clazz) {
		return disableLogbackLoggerLevel(clazz, "OFF");
	}

	@SuppressWarnings("PMD.CloseResource")
	public static void setLogbackLoggerLevel(Class<?> clazz, String levelToSet) {
		ILogDisabler logDisabler = disableLogbackLoggerLevel(clazz, levelToSet);

		log.trace("One will have to restore the log level manually ({})", logDisabler);
	}

	public static ILogDisabler disableLogbackLoggerLevel(Class<?> clazz, String levelToSet) {
		Logger slf4jLogger = LoggerFactory.getLogger(clazz);

		return disableLogbackLoggerLevel(levelToSet, slf4jLogger);
	}

	public static ILogDisabler disableLogbackLoggerLevel(String levelToSet, Logger slf4jLogger) {
		String loggerClassName = slf4jLogger.getClass().getName();
		if ("ch.qos.logback.classic.Logger".equals(loggerClassName)) {
			try {
				// Get current level
				Method getLevelMethod = slf4jLogger.getClass().getMethod("getLevel");
				Object originalLevel = getLevelMethod.invoke(slf4jLogger);

				// Apply requested level
				Class<?> logbackLevelClass = Class.forName("ch.qos.logback.classic.Level");
				Method setLevelMethod = slf4jLogger.getClass().getMethod("setLevel", logbackLevelClass);
				setLevelMethod.invoke(slf4jLogger, logbackLevelClass.getField(levelToSet).get(null));

				return new ILogDisabler() {

					@Override
					public void close() {
						// Restore original level
						try {
							setLevelMethod.invoke(slf4jLogger, originalLevel);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							throw new IllegalArgumentException(
									"Issue while restoring the level of " + slf4jLogger + " to: " + originalLevel,
									e);
						}
					}
				};
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException
					| NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		} else {
			log.info("This work only with LogBack while it was: {}", loggerClassName);

			return new ILogDisabler() {

				@Override
				public void close() {
					log.trace("There is no level to restore");
				}
			};
		}
	}

	public static void enableLogToInfo(Class<?> clazz) {
		setLogbackLoggerLevel(clazz, "INFO");
	}
}
