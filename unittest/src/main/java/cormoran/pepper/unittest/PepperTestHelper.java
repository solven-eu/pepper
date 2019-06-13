package cormoran.pepper.unittest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		try {
			URLConnection connection = new URL("https://google.com").openConnection();
			Assume.assumeNotNull(connection);
			// We check some data from the connection as we may receive a not connection connection
			Assume.assumeNotNull(connection.getContentType());

			return true;
		} catch (RuntimeException | IOException e) {
			Assume.assumeNoException("Internet is not available", e);
			return false;
		}
	}

	public static void disableLog(Class<?> clazz) {
		setLogbackLoggerLevel(clazz, "OFF");
	}

	public static void setLogbackLoggerLevel(Class<?> clazz, String levelToSet) {
		Logger slf4jLogger = LoggerFactory.getLogger(clazz);

		if ("ch.qos.logback.classic.Logger".equals(slf4jLogger.getClass().getName())) {
			try {
				Class<?> logbackLevelClass = Class.forName("ch.qos.logback.classic.Level");
				Method setLevelMethod = slf4jLogger.getClass().getMethod("setLevel", logbackLevelClass);

				setLevelMethod.invoke(slf4jLogger, logbackLevelClass.getField(levelToSet).get(null));
			} catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException
					| NoSuchFieldException e) {
				throw new RuntimeException(e);
			}
		} else {
			LOGGER.info("This work only with LogBack");
		}
	}

	public static void enableLogToInfo(Class<?> clazz) {
		setLogbackLoggerLevel(clazz, "INFO");
	}
}
