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
package cormoran.pepper.logging;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import com.google.common.annotations.Beta;
import com.google.common.collect.Iterables;
import com.google.common.collect.Streams;

import cormoran.pepper.memory.IPepperMemoryConstants;

/**
 * Various helpers for logging
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperLogHelper {
	private static final long HUNDRED = 100L;

	/**
	 * We want to see at least 2 digits: after printing 9999B, we print 10KB
	 */
	protected static final int BARRIER_FOR_SIZE_IN_LOG = 10;

	public static final int THOUSAND = 1000;
	public static final int TEN_F = 10;

	protected PepperLogHelper() {
		// hidden
	}

	/**
	 * This methods facilitates using logging framework at trace level, without using .isTraceEnabled, as the .toString
	 * 
	 * @param toStringMe
	 *            the object from which a .toString should be computed lazily
	 * @return an Object which has as .toString the one provided by the Supplier
	 */
	public static Object lazyToString(Supplier<String> toStringMe) {
		return new Object() {
			@Override
			public String toString() {
				return toStringMe.get();
			}
		};
	}

	public static Object getNicePercentage(long progress, long max) {
		return lazyToString(() -> {
			if (progress < 0L || max <= 0L) {
				return "-%";
			} else {
				long ratio = progress * HUNDRED / max;

				if (ratio == 0) {
					long smallRatio = progress * THOUSAND / max;

					if (smallRatio == 0) {
						long verySmallRatio = progress * HUNDRED * HUNDRED / max;
						return "0.0" + Long.toString(verySmallRatio) + "%";
					} else {
						return "0." + Long.toString(smallRatio) + "%";
					}
				} else if (ratio < TEN_F) {
					// Only one digit before dot: we look for digits after dot
					long smallRatio = (progress * THOUSAND * TEN_F - ratio * max * HUNDRED) / max;

					// We prefer having at least 2 digits
					return ratio + "." + Long.toString(smallRatio) + "%";
				} else {
					return Long.toString(ratio) + "%";
				}
			}
		});
	}

	public static Object humanBytes(long size) {
		return lazyToString(() -> {
			long absSize = Math.abs(size);
			if (absSize < BARRIER_FOR_SIZE_IN_LOG * IPepperMemoryConstants.KB) {
				return size + "B";
			} else if (absSize < BARRIER_FOR_SIZE_IN_LOG * IPepperMemoryConstants.MB) {
				return (size / IPepperMemoryConstants.KB) + "KB";
			} else if (absSize < BARRIER_FOR_SIZE_IN_LOG * IPepperMemoryConstants.GB) {
				return (size / IPepperMemoryConstants.MB) + "MB";
			} else if (absSize < BARRIER_FOR_SIZE_IN_LOG * IPepperMemoryConstants.TB) {
				return (size / IPepperMemoryConstants.GB) + "GB";
			} else if (absSize < BARRIER_FOR_SIZE_IN_LOG * IPepperMemoryConstants.PB) {
				return (size / IPepperMemoryConstants.TB) + "TB";
			} else {
				return (size / IPepperMemoryConstants.PB) + "PB";
			}
		});
	}

	/**
	 * @deprecated Prefer using {@link #humanBytes(long)}
	 */
	@Deprecated
	public static Object getNiceMemory(long size) {
		return humanBytes(size);
	}

	public static Object getObjectAndClass(Object o) {
		return lazyToString(() -> {
			if (o == null) {
				return null + "(null)";
			} else if (o instanceof Map<?, ?>) {
				Map<?, ?> asMap = (Map<?, ?>) o;

				// see java.util.AbstractMap.toString()
				return asMap.entrySet().stream().map(e -> {
					if (e.getValue() == o) {
						return e.getKey() + "=" + "(this Map)";
					} else {
						return e.getKey() + "=" + getObjectAndClass(e.getValue());
					}
				}).collect(Collectors.joining(", ", "{", "}"));
			} else {
				return o.toString() + "(" + o.getClass().getName() + ")";
			}
		});
	}

	/**
	 * @deprecated prefer .getFirstChars
	 */
	@Deprecated
	public static Object getToStringWithLimit(Iterable<?> iterable, int limitSize) {
		return lazyToString(() -> {
			if (iterable == null) {
				return null + "(null)";
			} else {
				int size = Iterables.size(iterable);

				if (size <= limitSize) {
					return iterable.toString();
				} else {
					return "[" + Streams.stream(iterable).limit(limitSize).map(Object::toString).collect(
							Collectors.joining(", ")) + ", (" + (size - limitSize) + " more elements)]";
				}
			}
		});
	}

	public static Object getNiceDouble(Double value) {
		if (value == null) {
			return "null";
		} else {
			return lazyToString(() -> {
				final String pattern = ".##";
				// final String pattern = "###,###,##0.00";
				final DecimalFormat myFormatter = new DecimalFormat(pattern);

				return myFormatter.format(value);
			});
		}
	}

	public static Object getNiceTime(long timeInMs) {
		return getNiceTime(timeInMs, TimeUnit.MILLISECONDS);
	}

	private static final String DAYS_PREFIX = "days";
	private static final String HOURS_PREFIX = "hours";
	private static final String MINUTES_PREFIX = "min";
	private static final String SECONDS_PREFIX = "sec";
	private static final String MILLIS_PREFIX = "ms";
	private static final String NANOS_PREFIX = "ns";

	public static Object getNiceTime(long time, TimeUnit timeUnit) {
		return PepperLogHelper.lazyToString(() -> {
			long timeInMs = timeUnit.toMillis(time);

			if (timeInMs >= TimeUnit.DAYS.toMillis(1)) {
				String minString = TimeUnit.MILLISECONDS.toDays(timeInMs) + DAYS_PREFIX;

				long hours = timeInMs - TimeUnit.DAYS.toMillis(1) * TimeUnit.MILLISECONDS.toDays(timeInMs);

				if (hours > 0) {
					return minString + " " + TimeUnit.MILLISECONDS.toHours(hours) + HOURS_PREFIX;
				} else {
					return minString;
				}
			} else if (timeInMs >= TimeUnit.HOURS.toMillis(1)) {
				String minString = TimeUnit.MILLISECONDS.toHours(timeInMs) + HOURS_PREFIX;

				long minutes = timeInMs - TimeUnit.HOURS.toMillis(1) * TimeUnit.MILLISECONDS.toHours(timeInMs);

				if (minutes > 0) {
					return minString + " " + TimeUnit.MILLISECONDS.toMinutes(minutes) + MINUTES_PREFIX;
				} else {
					return minString;
				}
			} else if (timeInMs >= TimeUnit.MINUTES.toMillis(1)) {
				String minString = TimeUnit.MILLISECONDS.toMinutes(timeInMs) + MINUTES_PREFIX;

				long seconds = timeInMs - TimeUnit.MINUTES.toMillis(1) * TimeUnit.MILLISECONDS.toMinutes(timeInMs);

				if (seconds > 0) {
					return minString + " " + TimeUnit.MILLISECONDS.toSeconds(seconds) + SECONDS_PREFIX;
				} else {
					return minString;
				}

			} else if (timeInMs >= TimeUnit.SECONDS.toMillis(1)) {
				String minString = TimeUnit.MILLISECONDS.toSeconds(timeInMs) + SECONDS_PREFIX;

				long millis = timeInMs - TimeUnit.SECONDS.toMillis(1) * TimeUnit.MILLISECONDS.toSeconds(timeInMs);

				if (millis > 0) {
					return minString + " " + millis + MILLIS_PREFIX;
				} else {
					return minString;
				}
			} else {
				return timeInMs + MILLIS_PREFIX;
			}
		});
	}

	public static Object getNiceRate(long nbEntries, long time, TimeUnit timeUnit) {
		return lazyToString(() -> {
			return rawHumanRate(nbEntries, time, timeUnit);
		});
	}

	private static String rawHumanRate(long nbEntries, long time, TimeUnit timeUnit) {
		if (time <= 0) {
			// Edge case
			return nbEntries + "#/0" + timeUnit;
		} else if (nbEntries == 0) {
			// Edge case
			return "0#/" + MILLIS_PREFIX;
		}

		long nbPerNano = nbEntries * timeUnit.convert(1, TimeUnit.NANOSECONDS) / time;
		if (nbPerNano > 0) {
			return nbPerNano + "#/" + NANOS_PREFIX;
		} else if (timeUnit.toNanos(time) >= 0) {
			long nbPerNano2 = nbEntries / timeUnit.toNanos(time);
			if (nbPerNano2 > 0) {
				return nbPerNano2 + "#/" + NANOS_PREFIX;
			}
		}

		long nbPerMilli = nbEntries * timeUnit.convert(1, TimeUnit.MILLISECONDS) / time;
		if (nbPerMilli > 0) {
			return nbPerMilli + "#/" + MILLIS_PREFIX;
		} else if (timeUnit.toMillis(time) >= 0) {
			long nbPerMilli2 = nbEntries / timeUnit.toMillis(time);
			if (nbPerMilli2 > 0) {
				return nbPerMilli2 + "#/" + MILLIS_PREFIX;
			}
		}

		long nbPerSecond = nbEntries * timeUnit.convert(1, TimeUnit.SECONDS) / time;
		if (nbPerSecond > 0) {
			return nbPerSecond + "#/" + SECONDS_PREFIX;
		} else if (timeUnit.toMillis(time) >= 0) {
			long nbPerSecond2 = nbEntries / timeUnit.toSeconds(time);
			if (nbPerSecond2 > 0) {
				return nbPerSecond2 + "#/" + SECONDS_PREFIX;
			}
		}

		long nbPerMinute = nbEntries * timeUnit.convert(1, TimeUnit.MINUTES) / time;
		if (nbPerMinute > 0) {
			return nbPerMinute + "#/" + MINUTES_PREFIX;
		} else if (timeUnit.toMinutes(time) >= 0) {
			long nbPerMinute2 = nbEntries / timeUnit.toMinutes(time);
			if (nbPerMinute2 > 0) {
				return nbPerMinute2 + "#/" + MINUTES_PREFIX;
			}
		}

		long nbPerHour = nbEntries * timeUnit.convert(1, TimeUnit.HOURS) / time;
		if (nbPerHour > 0) {
			return nbPerHour + "#/" + HOURS_PREFIX;
		}

		long nbPerDay = nbEntries / timeUnit.toDays(time);
		return nbPerDay + "#/" + DAYS_PREFIX;
	}

	public static Object getFirstChars(Object toString, int limitChars) {
		if (toString == null) {
			// Stick to default behavior for null objects
			return String.valueOf(toString);
		}
		return lazyToString(() -> {
			String asString = toString.toString();
			if (asString.length() <= limitChars) {
				return asString;
			} else {
				return "'" + asString.substring(0, limitChars)
						+ "...("
						+ (asString.length() - limitChars)
						+ " more chars)'";
			}
		});
	}

	/**
	 * 
	 * @param toString
	 * @param removeEOL
	 *            if true, we replace end-of-line characters by a space, else we escape them
	 * @return a String which is guaranteed to hold on a single row
	 * @deprecated Prefer .removeNewLines or .escapeNewLines
	 */
	@Deprecated
	public static Object getSingleRow(Object toString, boolean removeEOL) {
		if (removeEOL) {
			return removeNewLines(toString);
		} else {
			return escapeNewLines(toString);
		}
	}

	public static Object removeNewLines(Object toString) {
		// Replace consecutive '\r\n' by a space (Windows), and then each individual by another space (Linux and
		// Mac)
		return lazyToString(() -> toString.toString().replaceAll("\r\n", " ").replaceAll("[\r\n]", " "));
	}

	public static Object escapeNewLines(Object toString) {
		return lazyToString(() -> toString.toString().replaceAll("\r", Matcher.quoteReplacement("\\r")).replaceAll("\n",
				Matcher.quoteReplacement("\\n")));
	}

	// TODO
	@Beta
	@Deprecated
	public static Object getFirstCharsInMap(Map<?, ?> toString, int limitChars) {
		// TODO: have a limit per key and value
		return getFirstChars(toString, limitChars);
	}
}
