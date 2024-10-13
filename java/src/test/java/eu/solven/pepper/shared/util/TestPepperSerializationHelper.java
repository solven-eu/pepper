/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.shared.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.solven.pepper.io.PepperSerializationHelper;

public class TestPepperSerializationHelper {
	@Test
	public void testConvertEmptyToMap() {
		String asString = "";

		Assertions.assertEquals(Collections.emptyMap(), PepperSerializationHelper.convertToMap(asString));
	}

	@Test
	public void testConvertToMap() {
		String asString = "a=b,c=d";

		Assertions.assertEquals(ImmutableMap.of("a", "b", "c", "d"), PepperSerializationHelper.convertToMap(asString));
	}

	@Test
	public void testConvertToMapInvalidSeparator() {
		String asString = "a=b;c=d";

		Assertions.assertEquals(ImmutableMap.of("a", "b", "c", "d"), PepperSerializationHelper.convertToMap(asString));
	}

	@Test
	public void testConvertList() {
		Assertions.assertEquals(Arrays.asList("EUR", "USD"), PepperSerializationHelper.convertToList("EUR,USD"));
		Assertions.assertEquals(Arrays.asList("EUR", "USD"), PepperSerializationHelper.convertToList("EUR|USD"));

		Assertions.assertEquals(Sets.newHashSet("EUR", "USD"), PepperSerializationHelper.convertToSet("EUR,USD"));
		Assertions.assertEquals(Sets.newHashSet("EUR", "USD"), PepperSerializationHelper.convertToSet("EUR|USD"));
	}

	@Test
	public void testConvertMapIterableToMap() {
		Assertions.assertEquals("a=b|c",
				PepperSerializationHelper.convertToString(ImmutableMap.of("a", Arrays.asList("b", "c"))));
	}

	@Test
	public void testConvertMapOfObject() {
		ImmutableMap<String, LocalDate> objectMap = ImmutableMap.of("key", LocalDate.now());
		Assertions.assertEquals(objectMap,
				PepperSerializationHelper.convertToMap(PepperSerializationHelper.convertToString(objectMap)));
	}

	@Test
	public void testMD5_utf8_md5() throws NoSuchAlgorithmException {
		String md5 = PepperSerializationHelper.toMD5("Youpi", Charsets.UTF_8, MessageDigest.getInstance("MD5"));
		Assertions.assertEquals("c2dd8be8874b1200530e72bcb5d416da", md5);
	}

	// This test depends on the platform (e.g. default charset)
	@Test
	public void testMD5_default() throws NoSuchAlgorithmException {
		String output = PepperSerializationHelper.toMD5("Youpi");
		Assertions.assertTrue(output.length() > 0);
	}

	@Test
	public void testSha512() throws NoSuchAlgorithmException {
		String sha512 = PepperSerializationHelper.toSha512("Youpi", "someSalt");
		Assertions.assertEquals(
				"a2972849746c35f2dc82dc1cedb9e78318508b93b6b007f37f779838f62ca196612c8c6fb8f18b83d9cf144ce4b373d84781b69a9301097d9b53ab7516ed553d",
				sha512);
	}

	@Test
	public void testSha512_Random() throws NoSuchAlgorithmException {
		String output = PepperSerializationHelper.generateSha512();
		Assertions.assertTrue(output.length() > 0);
	}

	@Test
	public void testSafeToObject_LocalDate() {
		Object object = LocalDate.now();
		Assertions.assertEquals(object,
				PepperSerializationHelper.safeToObject(LocalDate.class, object.toString()).get());
	}

	@Test
	public void testSafeToObject_Float() {
		Object object = 3F;
		Assertions.assertEquals(object, PepperSerializationHelper.safeToObject(Float.class, object.toString()).get());
	}

}
