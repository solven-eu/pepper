package cormoran.pepper.time;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Utility class for Date.
 * 
 * @author Benoit Lacelle
 */
public final class PepperDateHelper {

	private PepperDateHelper() {
	}

	public static LocalDateTime asUTCLocalDateTime(OffsetDateTime offsetDateTime) {
		return offsetDateTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
	}

	public static OffsetDateTime asUTCOffsetDateTime(Date lastModified) {
		return OffsetDateTime.ofInstant(lastModified.toInstant(), ZoneOffset.UTC);
	}
}
