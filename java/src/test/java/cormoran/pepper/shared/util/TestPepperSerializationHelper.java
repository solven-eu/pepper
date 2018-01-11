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
package cormoran.pepper.shared.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import cormoran.pepper.io.PepperFileHelper;
import cormoran.pepper.io.PepperSerializationHelper;

public class TestPepperSerializationHelper {
	@Test
	public void testConvertEmptyToMap() {
		String asString = "";

		Assert.assertEquals(Collections.emptyMap(), PepperSerializationHelper.convertToMap(asString));
	}

	@Test
	public void testConvertToMap() {
		String asString = "a=b,c=d";

		Assert.assertEquals(ImmutableMap.of("a", "b", "c", "d"), PepperSerializationHelper.convertToMap(asString));
	}

	@Test
	public void testConvertToMapInvalidSeparator() {
		String asString = "a=b;c=d";

		Assert.assertEquals(ImmutableMap.of("a", "b", "c", "d"), PepperSerializationHelper.convertToMap(asString));
	}

	@Test
	public void testConvertList() {
		Assert.assertEquals(Arrays.asList("EUR", "USD"), PepperSerializationHelper.convertToList("EUR,USD"));
		Assert.assertEquals(Arrays.asList("EUR", "USD"), PepperSerializationHelper.convertToList("EUR|USD"));

		Assert.assertEquals(Sets.newHashSet("EUR", "USD"), PepperSerializationHelper.convertToSet("EUR,USD"));
		Assert.assertEquals(Sets.newHashSet("EUR", "USD"), PepperSerializationHelper.convertToSet("EUR|USD"));
	}

	@Test
	public void testConvertMapIterableToMap() {
		Assert.assertEquals("a=b|c",
				PepperSerializationHelper.convertToString(ImmutableMap.of("a", Arrays.asList("b", "c"))));
	}

	@Test
	public void testConvertMapOfObject() {
		ImmutableMap<String, LocalDate> objectMap = ImmutableMap.of("key", LocalDate.now());
		Assert.assertEquals(objectMap,
				PepperSerializationHelper.convertToMap(PepperSerializationHelper.convertToString(objectMap)));
	}

	@Test
	public void testAppendLineInCSV() throws IOException {
		Path tmpFile = PepperFileHelper.createTempPath("apex.test", ".csv", true);

		PepperSerializationHelper.appendLineInCSVFile(tmpFile, Arrays.asList("col1", "col2"));

		// handle null value
		PepperSerializationHelper.appendLineInCSVFile(tmpFile, Arrays.asList("value1", null));
	}

	@Test
	public void testAppendLineInFileOutputStream() throws IOException {
		Path tmpFile = PepperFileHelper.createTempPath("apex.test", ".csv", true);

		FileOutputStream fos = new FileOutputStream(tmpFile.toFile());

		PepperSerializationHelper.appendLineInCSVFile(fos, Arrays.asList("col1", "col2"));

		// handle null value
		PepperSerializationHelper.appendLineInCSVFile(fos, Arrays.asList("value1", null));
	}

	@Test
	public void testEscapeDoubleQuotesMax() throws IOException {
		StringWriter sw = new StringWriter();

		PepperSerializationHelper.rawAppendLineInCSVFile(sw, Arrays.asList("In\"Middle", null, "\"Wrapped\""), true, 5);

		Assert.assertEquals("\"In\"\"M\";;\"Wrapp\"", sw.toString());
	}

	@Test
	public void testEscapeDoubleQuotesNoMax() throws IOException {
		StringWriter sw = new StringWriter();

		PepperSerializationHelper
				.rawAppendLineInCSVFile(sw, Arrays.asList("In\"Middle", null, "\"Wrapped\""), true, Integer.MAX_VALUE);

		Assert.assertEquals("\"In\"\"Middle\";;\"Wrapped\"", sw.toString());
	}

	@Test
	public void testMD5_utf8_md5() throws NoSuchAlgorithmException {
		String md5 = PepperSerializationHelper.toMD5("Youpi", Charsets.UTF_8, MessageDigest.getInstance("MD5"));
		Assert.assertEquals("c2dd8be8874b1200530e72bcb5d416da", md5);
	}

	// This test depends on the platform (e.g. default charset)
	@Test
	public void testMD5_default() throws NoSuchAlgorithmException {
		String output = PepperSerializationHelper.toMD5("Youpi");
		Assert.assertTrue(output.length() > 0);
	}

	@Test
	public void testSafeToObject_LocalDate() {
		Object object = LocalDate.now();
		Assert.assertEquals(object, PepperSerializationHelper.safeToObject(LocalDate.class, object.toString()).get());
	}

	@Test
	public void testSafeToObject_Float() {
		Object object = 3F;
		Assert.assertEquals(object, PepperSerializationHelper.safeToObject(Float.class, object.toString()).get());
	}

}
