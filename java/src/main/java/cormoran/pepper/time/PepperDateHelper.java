package cormoran.pepper.time;

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

		LOGGER.info("Switching ZoneId from -D{}={} to {}", previousZoneId, previousZoneId, zoneId);

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
