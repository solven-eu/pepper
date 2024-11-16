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
package eu.solven.pepper.core;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Date.
 *
 * @author Benoit Lacelle
 */
public final class PepperDateHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(PepperDateHelper.class);

	private static final AtomicReference<ZoneId> REF_ZONE_ID = new AtomicReference<>(ZoneId.systemDefault());

	protected PepperDateHelper() {
		// hidden
	}

	/**
	 * Enables to programmatically change current ZoneId.
	 *
	 * It overloads the value set by -Duser.timezone (e.g. '-Duser.timezone=Pacific/Fiji')
	 *
	 * @param zoneId
	 */
	public static void setZoneId(ZoneId zoneId) {
		String propertyKey = "user.timezone";
		String previousZoneId = System.getProperty(propertyKey);

		LOGGER.info("Switching ZoneId from -D{}={} to {}", propertyKey, previousZoneId, zoneId);

		REF_ZONE_ID.set(zoneId);
	}

	public static ZoneId zoneId() {
		return REF_ZONE_ID.get();
	}

	public static LocalDateTime asUTCLocalDateTime(OffsetDateTime offsetDateTime) {
		return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
	}

	public static OffsetDateTime asUTCOffsetDateTime(Date lastModified) {
		return OffsetDateTime.ofInstant(lastModified.toInstant(), ZoneOffset.UTC);
	}

	public static OffsetDateTime now() {
		return OffsetDateTime.now(zoneId());
	}

	public static LocalDate today() {
		return now().toLocalDate();
	}
}
