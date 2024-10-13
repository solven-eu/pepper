/**
 * The MIT License
 * Copyright (c) 2014-2024 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.shared.logging;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.solven.pepper.logging.PepperLogHelper;

public class TestPepperLogHelper {

	@Test
	public void testLazyToString() {
		// Not the String
		Assertions.assertNotEquals("Youpi", PepperLogHelper.lazyToString(() -> "Youpi"));

		// But same .toString
		Assertions.assertEquals("Youpi", PepperLogHelper.lazyToString(() -> "Youpi").toString());
	}

	@Test
	public void testLazyToString_jackson() throws JsonProcessingException {
		Object lazyToString = PepperLogHelper.lazyToString(() -> "Youpi");

		ObjectMapper objectMapper = new ObjectMapper();
		// Check .lazyToString is compatible with Jackson (e.g. for logstash-encoder)
		Assertions.assertEquals("\"Youpi\"", objectMapper.writeValueAsString(lazyToString));
		// Check it is similar than over the initial String
		Assertions.assertEquals("\"Youpi\"", objectMapper.writeValueAsString("Youpi"));
	}

	@Test
	public void getPercentage() {
		Assertions.assertEquals("10%", PepperLogHelper.getNicePercentage(100, 1000).toString());
	}

	@Test
	public void getPercentageDivideBy0() {
		Assertions.assertEquals("-%", PepperLogHelper.getNicePercentage(100, 0).toString());
	}

	@Test
	public void getSmallPercentage() {
		Assertions.assertEquals("0.3%", PepperLogHelper.getNicePercentage(3, 1000).toString());
	}

	@Test
	public void getVerySmallPercentage() {
		Assertions.assertEquals("0.03%", PepperLogHelper.getNicePercentage(3, 10_000).toString());
	}

	@Test
	public void getMediumPercentage() {
		Assertions.assertEquals("2.66%", PepperLogHelper.getNicePercentage(6, 225).toString());
	}

	@Test
	public void getProgressAboveMax() {
		Assertions.assertEquals("1000%", PepperLogHelper.getNicePercentage(1000, 100).toString());
	}

	@Test
	public void testHuanRate_allTimeUnit() {
		Stream.of(TimeUnit.values()).forEach(tu -> {
			org.assertj.core.api.Assertions.assertThat(PepperLogHelper.humanRate(10, 10, tu).toString()).isNotEmpty();
		});
	}

	@Test
	public void testBigTimeLowRate() {
		Assertions.assertEquals("1#/days", PepperLogHelper.humanRate(10, 10, TimeUnit.DAYS).toString());
	}

	@Test
	public void testBigTimeVeryLowRate() {
		Assertions.assertEquals("10#/sec", PepperLogHelper.humanRate(1, 100, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testBigTimeVeryLowRate1() {
		Assertions.assertEquals("30#/min", PepperLogHelper.humanRate(5, 10 * 1000, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testBigTimeHIghRate() {
		Assertions.assertEquals("2#/ms", PepperLogHelper.humanRate(Integer.MAX_VALUE, 10, TimeUnit.DAYS).toString());
	}

	@Test
	public void testLowTimeLowRate() {
		Assertions.assertEquals("1#/ms", PepperLogHelper.humanRate(10, 10, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testLowTimeHighRate() {
		Assertions.assertEquals("214#/ns",
				PepperLogHelper.humanRate(Integer.MAX_VALUE, 10, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testRightUnderRatePerSecond() {
		Assertions.assertEquals("999#/sec", PepperLogHelper.humanRate(999, 1000, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testZeroTime() {
		Assertions.assertEquals("999#/0SECONDS", PepperLogHelper.humanRate(999, 0, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testDoubleRate_1Percent_perSecond() {
		Assertions.assertEquals("864#/days", PepperLogHelper.humanRate(0.01, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testDoubleRate_1Million_perMicro() {
		Assertions.assertEquals("1000#/ns", PepperLogHelper.humanRate(1_000_000, TimeUnit.MICROSECONDS).toString());
	}

	@Test
	public void testPercentageNoDecimals() {
		Assertions.assertEquals("100370%", PepperLogHelper.getNicePercentage(123_456, 123).toString());
	}

	@Test
	public void testPercentage() {
		Assertions.assertEquals("100370%", PepperLogHelper.getNicePercentage(123_456, 123).toString());

		Assertions.assertEquals("0.09%", PepperLogHelper.getNicePercentage(123, 123_456).toString());
	}

	@Test
	public void testPercentage2() {
		Assertions.assertEquals("9.80%", PepperLogHelper.getNicePercentage(98, 1000).toString());
	}

	@Test
	public void testPercentage3() {
		Assertions.assertEquals("9.81%", PepperLogHelper.getNicePercentage(981, 10_000).toString());
	}

	@Test
	public void testGetNiceTimeMillis() {
		Assertions.assertEquals("912ms", PepperLogHelper.humanDuration(912).toString());
	}

	@Test
	public void testGetNiceTimeSecondsAndMillis() {
		Assertions.assertEquals("9sec 600ms", PepperLogHelper.humanDuration(9600).toString());
	}

	@Test
	public void testGetNiceTimeSecondsAndMillis_NoHundredsInMillis() {
		Assertions.assertEquals("9sec 60ms", PepperLogHelper.humanDuration(9060).toString());
	}

	@Test
	public void testGetNiceTimeMinAndSeconds() {
		Assertions.assertEquals("2min 11sec", PepperLogHelper.humanDuration(131, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testGetNiceTimeRoundMinutes() {
		Assertions.assertEquals("2min", PepperLogHelper.humanDuration(120, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testGetNiceTimeHoursAndMinutes() {
		Assertions.assertEquals("2hours 11min", PepperLogHelper.humanDuration(131, TimeUnit.MINUTES).toString());
	}

	@Test
	public void testGetNiceDays() {
		Assertions.assertEquals("5days", PepperLogHelper.humanDuration(5, TimeUnit.DAYS).toString());
	}

	@Test
	public void testGetNiceDaysAndHours() {
		Assertions.assertEquals("4days 4hours", PepperLogHelper.humanDuration(100, TimeUnit.HOURS).toString());
	}

	@Test
	public void testGetNiceTimeFromNanos() {
		Assertions.assertEquals("1sec",
				PepperLogHelper.humanDuration(TimeUnit.SECONDS.toNanos(1), TimeUnit.NANOSECONDS).toString());
	}

	@Test
	public void testCollectionLimit_under() {
		Assertions.assertEquals("[0, 1]", PepperLogHelper.getToStringWithLimit(Arrays.asList(0, 1), 3).toString());
	}

	@Test
	public void testCollectionLimit_same() {
		Assertions.assertEquals("[0, 1, 2]",
				PepperLogHelper.getToStringWithLimit(Arrays.asList(0, 1, 2), 3).toString());
	}

	@Test
	public void testCollectionLimit_above() {
		Assertions.assertEquals("[0, 1, (3 more elements)]",
				PepperLogHelper.getToStringWithLimit(Arrays.asList(0, 1, 2, 3, 4), 2).toString());
	}

	@Test
	public void testLimitChars() {
		Assertions.assertEquals("12345...(4 more chars)", PepperLogHelper.getFirstChars("123456789", 5).toString());
	}

	@Test
	public void testLimitChars_underlimit() {
		Assertions.assertEquals("123456789", PepperLogHelper.getFirstChars("123456789", 15).toString());
	}

	@Test
	public void testLimitChars_null() {
		Assertions.assertEquals("null", PepperLogHelper.getFirstChars(null, 5).toString());
	}

	@Test
	public void testRemoveNewLines() {
		Assertions.assertEquals("a b", PepperLogHelper.removeNewLines("a\rb").toString());
		Assertions.assertEquals("a b", PepperLogHelper.removeNewLines("a\nb").toString());
		Assertions.assertEquals("a b", PepperLogHelper.removeNewLines("a\r\nb").toString());

		// \n\r leads to 2 whitespaces
		Assertions.assertEquals("a  b", PepperLogHelper.removeNewLines("a\n\rb").toString());

		Assertions.assertEquals(" a b c ", PepperLogHelper.removeNewLines("\na\rb\r\nc\r").toString());
	}

	@Test
	public void testEscapeNewLines() {
		Assertions.assertEquals("a\\rb", PepperLogHelper.escapeNewLines("a\rb").toString());
		Assertions.assertEquals("a\\nb", PepperLogHelper.escapeNewLines("a\nb").toString());
		Assertions.assertEquals("a\\r\\nb", PepperLogHelper.escapeNewLines("a\r\nb").toString());
	}

	@Test
	public void testObjectAndClass() {
		Assertions.assertEquals("{k=v(java.lang.String), k2=2(java.lang.Long)}",
				PepperLogHelper.getObjectAndClass(ImmutableMap.of("k", "v", "k2", 2L)).toString());
	}

	@Test
	public void testObjectAndClass_recursive() {
		Map<Object, Object> map = new LinkedHashMap<>();
		Assertions.assertEquals("{}", PepperLogHelper.getObjectAndClass(map).toString());

		// Add itself as value
		map.put("k", map);

		// Legimitate use-case as handle by AsbtractMap.toString()
		Assertions.assertEquals("{k=(this Map)}", map.toString());
		Assertions.assertEquals("{k=(this Map)}", PepperLogHelper.getObjectAndClass(map).toString());

		// Add another value
		map.put("k2", "v2");

		Assertions.assertEquals("{k=(this Map), k2=v2(java.lang.String)}",
				PepperLogHelper.getObjectAndClass(map).toString());
	}

	@Test
	public void testHumanBytes() {
		Assertions.assertEquals("789B", PepperLogHelper.humanBytes(789L).toString());
		Assertions.assertEquals("607KB", PepperLogHelper.humanBytes(789L * 789).toString());
		Assertions.assertEquals("468MB", PepperLogHelper.humanBytes(789L * 789 * 789).toString());
		Assertions.assertEquals("360GB", PepperLogHelper.humanBytes(789L * 789 * 789 * 789).toString());
		Assertions.assertEquals("278TB", PepperLogHelper.humanBytes(789L * 789 * 789 * 789 * 789).toString());
		Assertions.assertEquals("214PB", PepperLogHelper.humanBytes(789L * 789 * 789 * 789 * 789 * 789).toString());
	}

	@Test
	public void testGetNiceDouble_null() {
		Assertions.assertEquals("null", PepperLogHelper.getNiceDouble(null).toString());
	}

	@Test
	public void testGetNiceDouble_notNull() {
		Assertions.assertEquals("1.2", PepperLogHelper.getNiceDouble(1.2D).toString());
	}

	@Test
	public void testGetNiceDouble_NaN() {
		Assertions.assertEquals("NaN", PepperLogHelper.getNiceDouble(Double.NaN).toString());
	}

	@Test
	public void testGetNiceDouble_Infinity() {
		Assertions.assertEquals("Infinity", PepperLogHelper.getNiceDouble(Double.POSITIVE_INFINITY).toString());
	}

	@Test
	public void testGetNiceDouble_MediumAndVeryPrecise() {
		Assertions.assertEquals("12.35", PepperLogHelper.getNiceDouble(12.345_678_912_3).toString());
	}

	@Test
	public void testGetNiceDouble_MediumAndVeryPrecise_Negative() {
		Assertions.assertEquals("-12.35", PepperLogHelper.getNiceDouble(-12.345_678_912_3).toString());
	}

	@Test
	public void testGetNiceDouble_BigAndPrecise() {
		Assertions.assertEquals("123456789.12", PepperLogHelper.getNiceDouble(123_456_789.123_456_789D).toString());
	}

	@Test
	public void testGetNiceDouble_BigAndNotPrecise() {
		Assertions.assertEquals("1230000000000.0", PepperLogHelper.getNiceDouble(123e10D).toString());
	}

	@Test
	public void testGetNiceDouble_veryNearZero() {
		Assertions.assertEquals("0.00000000012", PepperLogHelper.getNiceDouble(0.000_000_000_123_456_789D).toString());
	}

	@Test
	public void testGetNiceDouble_Zero() {
		Assertions.assertEquals("0.0", PepperLogHelper.getNiceDouble(0D).toString());
	}

	@Test
	public void testGetNiceDouble_NextToZero() {
		Assertions.assertEquals("0.0", PepperLogHelper.getNiceDouble(Double.MIN_NORMAL).toString());
	}

	@Test
	public void testGetNiceDouble_NextToZero_Negative() {
		Assertions.assertEquals("-0.0", PepperLogHelper.getNiceDouble(-1 * Double.MIN_NORMAL).toString());
	}

	@Test
	public void testGetNiceDouble_FrenchLocal() {
		// France by default have a ',' as decimal separator
		Locale.setDefault(Locale.Category.FORMAT, Locale.FRANCE);
		Assertions.assertEquals("123.46", PepperLogHelper.getNiceDouble(123.456D).toString());
	}
}
