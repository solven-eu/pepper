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
package cormoran.pepper.shared.logging;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.logging.PepperLogHelper;

public class TestPepperLogHelper {
	@Test
	public void lazyToString() {
		// Not the String
		Assert.assertNotEquals("Youpi", PepperLogHelper.lazyToString(() -> "Youpi"));

		// But same .toString
		Assert.assertEquals("Youpi", PepperLogHelper.lazyToString(() -> "Youpi").toString());
	}

	@Test
	public void testLazyToString() {
		// The lazyToString should not be a String
		Assert.assertNotEquals("Youpi", PepperLogHelper.lazyToString(() -> "Youpi"));

		Assert.assertEquals("Youpi", PepperLogHelper.lazyToString(() -> "Youpi").toString());
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
		Assert.assertEquals("0.03%", PepperLogHelper.getNicePercentage(3, 10000).toString());
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
	public void testBigTimeLowRate() {
		Assert.assertEquals("1#/days", PepperLogHelper.getNiceRate(10, 10, TimeUnit.DAYS).toString());
	}

	@Test
	public void testBigTimeVeryLowRate() {
		Assert.assertEquals("10#/sec", PepperLogHelper.getNiceRate(1, 100, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testBigTimeVeryLowRate1() {
		Assert.assertEquals("30#/min", PepperLogHelper.getNiceRate(5, 10 * 1000, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testBigTimeHIghRate() {
		Assert.assertEquals("2#/ms", PepperLogHelper.getNiceRate(Integer.MAX_VALUE, 10, TimeUnit.DAYS).toString());
	}

	@Test
	public void testLowTimeLowRate() {
		Assert.assertEquals("1#/ms", PepperLogHelper.getNiceRate(10, 10, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testLowTimeHighRate() {
		Assert.assertEquals("214#/ns",
				PepperLogHelper.getNiceRate(Integer.MAX_VALUE, 10, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testRightUnderRatePerSecond() {
		Assert.assertEquals("999#/sec", PepperLogHelper.getNiceRate(999, 1000, TimeUnit.MILLISECONDS).toString());
	}

	@Test
	public void testZeroTime() {
		Assert.assertEquals("999#/0SECONDS", PepperLogHelper.getNiceRate(999, 0, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testPercentageNoDecimals() {
		Assert.assertEquals("100370%", PepperLogHelper.getNicePercentage(123456, 123).toString());
	}

	@Test
	public void testPercentage() {
		Assert.assertEquals("100370%", PepperLogHelper.getNicePercentage(123456, 123).toString());

		Assert.assertEquals("0.09%", PepperLogHelper.getNicePercentage(123, 123456).toString());
	}

	@Test
	public void testPercentage2() {
		Assert.assertEquals("9.80%", PepperLogHelper.getNicePercentage(98, 1000).toString());
	}

	@Test
	public void testPercentage3() {
		Assert.assertEquals("9.81%", PepperLogHelper.getNicePercentage(981, 10000).toString());
	}

	@Test
	public void testGetNiceTimeMillis() {
		Assert.assertEquals("912ms", PepperLogHelper.getNiceTime(912).toString());
	}

	@Test
	public void testGetNiceTimeSecondsAndMillis() {
		Assert.assertEquals("9sec 600ms", PepperLogHelper.getNiceTime(9600).toString());
	}

	@Test
	public void testGetNiceTimeSecondsAndMillis_NoHundredsInMillis() {
		Assert.assertEquals("9sec 60ms", PepperLogHelper.getNiceTime(9060).toString());
	}

	@Test
	public void testGetNiceTimeMinAndSeconds() {
		Assert.assertEquals("2min 11sec", PepperLogHelper.getNiceTime(131, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testGetNiceTimeRoundMinutes() {
		Assert.assertEquals("2min", PepperLogHelper.getNiceTime(120, TimeUnit.SECONDS).toString());
	}

	@Test
	public void testGetNiceTimeHoursAndMinutes() {
		Assert.assertEquals("2hours 11min", PepperLogHelper.getNiceTime(131, TimeUnit.MINUTES).toString());
	}

	@Test
	public void testGetNiceDays() {
		Assert.assertEquals("5days", PepperLogHelper.getNiceTime(5, TimeUnit.DAYS).toString());
	}

	@Test
	public void testGetNiceDaysAndHours() {
		Assert.assertEquals("4days 4hours", PepperLogHelper.getNiceTime(100, TimeUnit.HOURS).toString());
	}

	@Test
	public void testGetNiceTimeFromNanos() {
		Assert.assertEquals("1sec",
				PepperLogHelper.getNiceTime(TimeUnit.SECONDS.toNanos(1), TimeUnit.NANOSECONDS).toString());
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
		Assert.assertEquals("'12345...(4 more chars)'", PepperLogHelper.getFirstChars("123456789", 5).toString());
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
	public void testhumanBytes() {
		Assert.assertEquals("789B", PepperLogHelper.humanBytes(789L).toString());
		Assert.assertEquals("607KB", PepperLogHelper.humanBytes(789L * 789).toString());
		Assert.assertEquals("468MB", PepperLogHelper.humanBytes(789L * 789 * 789).toString());
		Assert.assertEquals("360GB", PepperLogHelper.humanBytes(789L * 789 * 789 * 789).toString());
		Assert.assertEquals("278TB", PepperLogHelper.humanBytes(789L * 789 * 789 * 789 * 789).toString());
		Assert.assertEquals("214PB", PepperLogHelper.humanBytes(789L * 789 * 789 * 789 * 789 * 789).toString());
	}
}
