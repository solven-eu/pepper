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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import eu.solven.pepper.logging.PepperLogHelper;

public class TestPepperLogHelper {

	@Test
	public void testLazyToString() {
		// Not the String
		Assert.assertNotEquals("Youpi", PepperLogHelper.lazyToString(() -> "Youpi"));

		// But same .toString
		Assert.assertEquals("Youpi", PepperLogHelper.lazyToString(() -> "Youpi").toString());
	}

	@Test
	public void testLazyToString_jackson() throws JsonProcessingException {
		Object lazyToString = PepperLogHelper.lazyToString(() -> "Youpi");

		ObjectMapper objectMapper = new ObjectMapper();
		// Check .lazyToString is compatible with Jackson (e.g. for logstash-encoder)
		Assert.assertEquals("\"Youpi\"", objectMapper.writeValueAsString(lazyToString));
		// Check it is similar than over the initial String
		Assert.assertEquals("\"Youpi\"", objectMapper.writeValueAsString("Youpi"));
	}

	@Test
	public void getPercentage() {
		Assert.assertEquals("10%", PepperLogHelper.getNicePercentage(100, 1000).toString());
	}

	@Test
	public void getPercentageDivideBy0() {
		Assert.assertEquals("-%", PepperLogHelper.getNicePercentage(100, 0).toString());
	}

	@Test
	public void getSmallPercentage() {
		Assert.assertEquals("0.3%", PepperLogHelper.getNicePercentage(3, 1000).toString());
	}

	@Test
	public void getVerySmallPercentage() {
		Assert.assertEquals("0.03%", PepperLogHelper.getNicePercentage(3, 10_000).toString());
	}

	@Test
	public void getMediumPercentage() {
		Assert.assertEquals("2.66%", PepperLogHelper.getNicePercentage(6, 225).toString());
	}

	@Test
	public void getProgressAboveMax() {
		Assert.assertEquals("1000%", PepperLogHelper.getNicePercentage(1000, 100).toString());
	}

	@Test
	public void testHuanRate_allTimeUnit() {
		Stream.of(TimeUnit.values()).forEach(tu -> {
			Assertions.assertThat(PepperLogHelper.humanRate(10, 10, tu).toString()).isNotEmpty();
		});
	}

	@Test
	public void testBigTimeLowRate() {
		Assert.assertEquals("1#/days", PepperLogHelper.humanRate(10, 10, TimeUnit.DAYS).toString());
	}

	@Test
	public void testBigTimeVeryLowRate() {
		Assert.assertEquals("10#/sec", PepperLogHelper.humanRate(1, 100, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testBigTimeVeryLowRate1() {
		Assert.assertEquals("30#/min", PepperLogHelper.humanRate(5, 10 * 1000, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testBigTimeHIghRate() {
		Assert.assertEquals("2#/ms", PepperLogHelper.humanRate(Integer.MAX_VALUE, 10, TimeUnit.DAYS).toString());
	}

	@Test
	public void testLowTimeLowRate() {
		Assert.assertEquals("1#/ms", PepperLogHelper.humanRate(10, 10, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testLowTimeHighRate() {
		Assert.assertEquals("214#/ns",
				PepperLogHelper.humanRate(Integer.MAX_VALUE, 10, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testRightUnderRatePerSecond() {
		Assert.assertEquals("999#/sec", PepperLogHelper.humanRate(999, 1000, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testZeroTime() {
		Assert.assertEquals("999#/0SECONDS", PepperLogHelper.humanRate(999, 0, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testDoubleRate_1Percent_perSecond() {
		Assert.assertEquals("864#/days", PepperLogHelper.humanRate(0.01, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testDoubleRate_1Million_perMicro() {
		Assert.assertEquals("1000#/ns", PepperLogHelper.humanRate(1_000_000, TimeUnit.MICROSECONDS).toString());
	}

	@Test
	public void testPercentageNoDecimals() {
		Assert.assertEquals("100370%", PepperLogHelper.getNicePercentage(123_456, 123).toString());
	}

	@Test
	public void testPercentage() {
		Assert.assertEquals("100370%", PepperLogHelper.getNicePercentage(123_456, 123).toString());

		Assert.assertEquals("0.09%", PepperLogHelper.getNicePercentage(123, 123_456).toString());
	}

	@Test
	public void testPercentage2() {
		Assert.assertEquals("9.80%", PepperLogHelper.getNicePercentage(98, 1000).toString());
	}

	@Test
	public void testPercentage3() {
		Assert.assertEquals("9.81%", PepperLogHelper.getNicePercentage(981, 10_000).toString());
	}

	@Test
	public void testGetNiceTimeMillis() {
		Assert.assertEquals("912ms", PepperLogHelper.humanDuration(912).toString());
	}

	@Test
	public void testGetNiceTimeSecondsAndMillis() {
		Assert.assertEquals("9sec 600ms", PepperLogHelper.humanDuration(9600).toString());
	}

	@Test
	public void testGetNiceTimeSecondsAndMillis_NoHundredsInMillis() {
		Assert.assertEquals("9sec 60ms", PepperLogHelper.humanDuration(9060).toString());
	}

	@Test
	public void testGetNiceTimeMinAndSeconds() {
		Assert.assertEquals("2min 11sec", PepperLogHelper.humanDuration(131, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testGetNiceTimeRoundMinutes() {
		Assert.assertEquals("2min", PepperLogHelper.humanDuration(120, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testGetNiceTimeHoursAndMinutes() {
		Assert.assertEquals("2hours 11min", PepperLogHelper.humanDuration(131, TimeUnit.MINUTES).toString());
	}

	@Test
	public void testGetNiceDays() {
		Assert.assertEquals("5days", PepperLogHelper.humanDuration(5, TimeUnit.DAYS).toString());
	}

	@Test
	public void testGetNiceDaysAndHours() {
		Assert.assertEquals("4days 4hours", PepperLogHelper.humanDuration(100, TimeUnit.HOURS).toString());
	}

	@Test
	public void testGetNiceTimeFromNanos() {
		Assert.assertEquals("1sec",
				PepperLogHelper.humanDuration(TimeUnit.SECONDS.toNanos(1), TimeUnit.NANOSECONDS).toString());
	}

	@Test
	public void testCollectionLimit_under() {
		Assert.assertEquals("[0, 1]", PepperLogHelper.getToStringWithLimit(Arrays.asList(0, 1), 3).toString());
	}

	@Test
	public void testCollectionLimit_same() {
		Assert.assertEquals("[0, 1, 2]", PepperLogHelper.getToStringWithLimit(Arrays.asList(0, 1, 2), 3).toString());
	}

	@Test
	public void testCollectionLimit_above() {
		Assert.assertEquals("[0, 1, (3 more elements)]",
				PepperLogHelper.getToStringWithLimit(Arrays.asList(0, 1, 2, 3, 4), 2).toString());
	}

	@Test
	public void testLimitChars() {
		Assert.assertEquals("12345...(4 more chars)", PepperLogHelper.getFirstChars("123456789", 5).toString());
	}

	@Test
	public void testLimitChars_underlimit() {
		Assert.assertEquals("123456789", PepperLogHelper.getFirstChars("123456789", 15).toString());
	}

	@Test
	public void testLimitChars_null() {
		Assert.assertEquals("null", PepperLogHelper.getFirstChars(null, 5).toString());
	}

	@Test
	public void testRemoveNewLines() {
		Assert.assertEquals("a b", PepperLogHelper.removeNewLines("a\rb").toString());
		Assert.assertEquals("a b", PepperLogHelper.removeNewLines("a\nb").toString());
		Assert.assertEquals("a b", PepperLogHelper.removeNewLines("a\r\nb").toString());

		// \n\r leads to 2 whitespaces
		Assert.assertEquals("a  b", PepperLogHelper.removeNewLines("a\n\rb").toString());

		Assert.assertEquals(" a b c ", PepperLogHelper.removeNewLines("\na\rb\r\nc\r").toString());
	}

	@Test
	public void testEscapeNewLines() {
		Assert.assertEquals("a\\rb", PepperLogHelper.escapeNewLines("a\rb").toString());
		Assert.assertEquals("a\\nb", PepperLogHelper.escapeNewLines("a\nb").toString());
		Assert.assertEquals("a\\r\\nb", PepperLogHelper.escapeNewLines("a\r\nb").toString());
	}

	@Test
	public void testObjectAndClass() {
		Assert.assertEquals("{k=v(java.lang.String), k2=2(java.lang.Long)}",
				PepperLogHelper.getObjectAndClass(ImmutableMap.of("k", "v", "k2", 2L)).toString());
	}

	@Test
	public void testObjectAndClass_recursive() {
		Map<Object, Object> map = new LinkedHashMap<>();
		Assert.assertEquals("{}", PepperLogHelper.getObjectAndClass(map).toString());

		// Add itself as value
		map.put("k", map);

		// Legimitate use-case as handle by AsbtractMap.toString()
		Assert.assertEquals("{k=(this Map)}", map.toString());
		Assert.assertEquals("{k=(this Map)}", PepperLogHelper.getObjectAndClass(map).toString());

		// Add another value
		map.put("k2", "v2");

		Assert.assertEquals("{k=(this Map), k2=v2(java.lang.String)}",
				PepperLogHelper.getObjectAndClass(map).toString());
	}

	@Test
	public void testHumanBytes() {
		Assert.assertEquals("789B", PepperLogHelper.humanBytes(789L).toString());
		Assert.assertEquals("607KB", PepperLogHelper.humanBytes(789L * 789).toString());
		Assert.assertEquals("468MB", PepperLogHelper.humanBytes(789L * 789 * 789).toString());
		Assert.assertEquals("360GB", PepperLogHelper.humanBytes(789L * 789 * 789 * 789).toString());
		Assert.assertEquals("278TB", PepperLogHelper.humanBytes(789L * 789 * 789 * 789 * 789).toString());
		Assert.assertEquals("214PB", PepperLogHelper.humanBytes(789L * 789 * 789 * 789 * 789 * 789).toString());
	}

	@Test
	public void testGetNiceDouble_null() {
		Assert.assertEquals("null", PepperLogHelper.getNiceDouble(null).toString());
	}

	@Test
	public void testGetNiceDouble_notNull() {
		Assert.assertEquals("1.2", PepperLogHelper.getNiceDouble(1.2D).toString());
	}

	@Test
	public void testGetNiceDouble_NaN() {
		Assert.assertEquals("NaN", PepperLogHelper.getNiceDouble(Double.NaN).toString());
	}

	@Test
	public void testGetNiceDouble_Infinity() {
		Assert.assertEquals("Infinity", PepperLogHelper.getNiceDouble(Double.POSITIVE_INFINITY).toString());
	}

	@Test
	public void testGetNiceDouble_MediumAndVeryPrecise() {
		Assert.assertEquals("12.35", PepperLogHelper.getNiceDouble(12.345_678_912_3).toString());
	}

	@Test
	public void testGetNiceDouble_MediumAndVeryPrecise_Negative() {
		Assert.assertEquals("-12.35", PepperLogHelper.getNiceDouble(-12.345_678_912_3).toString());
	}

	@Test
	public void testGetNiceDouble_BigAndPrecise() {
		Assert.assertEquals("123456789.12", PepperLogHelper.getNiceDouble(123_456_789.123_456_789D).toString());
	}

	@Test
	public void testGetNiceDouble_BigAndNotPrecise() {
		Assert.assertEquals("1230000000000.0", PepperLogHelper.getNiceDouble(123e10D).toString());
	}

	@Test
	public void testGetNiceDouble_veryNearZero() {
		Assert.assertEquals("0.00000000012", PepperLogHelper.getNiceDouble(0.000_000_000_123_456_789D).toString());
	}

	@Test
	public void testGetNiceDouble_Zero() {
		Assert.assertEquals("0.0", PepperLogHelper.getNiceDouble(0D).toString());
	}

	@Test
	public void testGetNiceDouble_NextToZero() {
		Assert.assertEquals("0.0", PepperLogHelper.getNiceDouble(Double.MIN_NORMAL).toString());
	}

	@Test
	public void testGetNiceDouble_NextToZero_Negative() {
		Assert.assertEquals("-0.0", PepperLogHelper.getNiceDouble(-1 * Double.MIN_NORMAL).toString());
	}

	@Test
	public void testGetNiceDouble_FrenchLocal() {
		// France by default have a ',' as decimal separator
		Locale.setDefault(Locale.Category.FORMAT, Locale.FRANCE);
		Assert.assertEquals("123.46", PepperLogHelper.getNiceDouble(123.456D).toString());
	}
}
