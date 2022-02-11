package eu.solven.pepper.unittest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Assume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helpers for Unit-tests
 * 
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings("PMD.MoreThanOneLogger")
public class PepperTestHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(PepperTestHelper.class);

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
			Assume.assumeNotNull(connection);
			// We check some data from the connection as we may receive a not connection connection
			Assume.assumeNotNull(connection.getContentType());

			return true;
		} catch (RuntimeException | IOException e) {
			Assume.assumeNoException("Internet is not available", e);
			return false;
		}
	}

	public static ILogDisabler disableLog(Class<?> clazz) {
		return disableLogbackLoggerLevel(clazz, "OFF");
	}

	@SuppressWarnings("PMD.CloseResource")
	public static void setLogbackLoggerLevel(Class<?> clazz, String levelToSet) {
		ILogDisabler logDisabler = disableLogbackLoggerLevel(clazz, levelToSet);

		LOGGER.trace("One will have to restore the log level manually ({})", logDisabler);
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
			LOGGER.info("This work only with LogBack while it was: {}", loggerClassName);

			return new ILogDisabler() {

				@Override
				public void close() {
					LOGGER.trace("There is no level to restore");
				}
			};
		}
	}

	public static void enableLogToInfo(Class<?> clazz) {
		setLogbackLoggerLevel(clazz, "INFO");
	}

	/**
	 * Enables using a try-with statement in order to deactivate logs for a given section of code.
	 * 
	 * @param clazz
	 * @return
	 */
	@Deprecated
	public static AutoCloseable logDisabled(Class<?> clazz) {
		disableLog(clazz);

		return new AutoCloseable() {

			@Override
			public void close() {
				enableLogToInfo(clazz);
			}
		};
	}
}
