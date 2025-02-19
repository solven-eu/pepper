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
package eu.solven.pepper.jmx;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import eu.solven.pepper.io.PepperFileHelper;

public class TestPepperJMXHelper {

	/**
	 * Check we split correctly the template as String
	 */
	@Test
	public void testSearchWithTemplate() {
		Assertions.assertEquals(Maps.newHashMap(), PepperJMXHelper.convertToMap(PepperJMXHelper.JMX_DEFAULT_STRING));
		Assertions.assertEquals(Lists.newArrayList(),
				PepperJMXHelper.convertToList(PepperJMXHelper.JMX_DEFAULT_STRING));
		Assertions.assertEquals(Sets.newHashSet(), PepperJMXHelper.convertToSet(PepperJMXHelper.JMX_DEFAULT_STRING));

		Assertions.assertEquals(Collections.singletonMap("key", "value"), PepperJMXHelper.convertToMap("key=value"));
		Assertions.assertEquals(Lists.newArrayList("key", "value"), PepperJMXHelper.convertToList("key,value"));
		Assertions.assertEquals(Sets.newHashSet("key", "value"), PepperJMXHelper.convertToSet("key,value"));

		Assertions.assertEquals(Maps.newHashMap(), PepperJMXHelper.convertToMap(""));
		Assertions.assertEquals(Lists.newArrayList(), PepperJMXHelper.convertToList(""));
		Assertions.assertEquals(Sets.newHashSet(), PepperJMXHelper.convertToSet(""));
	}

	@Test
	public void testSearchWithNotTrimmed() {
		Assertions.assertEquals(ImmutableMap.of("key", "value", "key2", "value"),
				PepperJMXHelper.convertToMap(" key = value , key2 = value "));
		Assertions.assertEquals(Lists.newArrayList("key", "value"), PepperJMXHelper.convertToList(" key , value "));
		Assertions.assertEquals(Sets.newHashSet("key", "value"), PepperJMXHelper.convertToSet(" key , value"));
	}

	@Test
	public void testConvertToJMX() {
		Assertions.assertTrue(PepperJMXHelper.convertToJMXMap(ImmutableMap.of()) instanceof TreeMap<?, ?>);
		Assertions.assertTrue(
				PepperJMXHelper.convertToJMXMapString(ImmutableMap.of(new Date(), 3L)) instanceof LinkedHashMap<?, ?>);
		Assertions.assertTrue(
				PepperJMXHelper.convertToJMXSet(ImmutableSet.of(new Date(), new Date())) instanceof TreeSet<?>);
		Assertions.assertTrue(
				PepperJMXHelper.convertToJMXValueOrderedMap(ImmutableMap.of(new Date(), 3L)) instanceof HashMap<?, ?>);
	}

	@Test
	public void testConvertJMXLimit() {
		// We keep negative as explicitly set to it
		Assertions.assertEquals(-1, PepperJMXHelper.convertToLimit(-1));

		// Convert the default JMX int to the default limit
		Assertions.assertEquals(PepperJMXHelper.DEFAULT_LIMIT,
				PepperJMXHelper.convertToLimit(PepperJMXHelper.JMX_DEFAULT_INT));

		// Keep positive values as they are
		Assertions.assertEquals(1, PepperJMXHelper.convertToLimit(1));
	}

	@Test
	public void testConvertToMapOrdered() {
		Map<String, Long> reverse = ImmutableMap.of("A", 2L, "B", 1L);

		Map<String, Long> decreasing = PepperJMXHelper.convertToJMXValueOrderedMap(reverse);

		// Check we re-ordered by value
		Assertions.assertEquals(Arrays.asList("B", "A"), Lists.newArrayList(decreasing.keySet()));
	}

	@Test
	public void testConvertToMapOrderedReversed() {
		Map<String, Long> reverse = ImmutableMap.of("A", 1L, "B", 2L);

		Map<String, Long> decreasing = PepperJMXHelper.convertToJMXValueOrderedMap(reverse, true);

		// Check we re-ordered by value
		Assertions.assertEquals(Arrays.asList("B", "A"), Lists.newArrayList(decreasing.keySet()));
	}

	@Test
	public void testConvertToMapOrderedReversedListKey() {
		Map<List<String>, Long> reverse = ImmutableMap.of(ImmutableList.of("A"), 1L, ImmutableList.of("B"), 2L);

		Map<List<String>, Long> decreasing = PepperJMXHelper.convertToJMXValueOrderedMap(reverse, true);

		// Check we re-ordered by value
		Assertions.assertEquals(Arrays.asList(Arrays.asList("B"), Arrays.asList("A")),
				Lists.newArrayList(decreasing.keySet()));

		// Check the key has been made JMX compatible
		Assertions.assertTrue(decreasing.keySet().iterator().next() instanceof ArrayList);
	}

	@Test
	public void testConvertToMapList() {
		Map<String, List<String>> asMapOfList = PepperJMXHelper.convertToMapList("a=b|c;d=e|f");

		Assertions.assertEquals(ImmutableMap.of("a", ImmutableList.of("b", "c"), "d", ImmutableList.of("e", "f")),
				asMapOfList);
	}

	@Test
	public void testConvertToJMXListMapString() {
		LocalDateTime nowAsObject = LocalDateTime.now();
		List<? extends Map<String, String>> asMapOfList =
				PepperJMXHelper.convertToJMXListMapString(ImmutableList.of(ImmutableMap.of("key", nowAsObject)));

		Assertions.assertEquals(ImmutableList.of(ImmutableMap.of("key", nowAsObject.toString())), asMapOfList);

		// JMX compatible classes
		Assertions.assertTrue(asMapOfList instanceof ArrayList);
		Assertions.assertTrue(asMapOfList.get(0) instanceof TreeMap);
	}

	@Test
	public void testConvertURL_DefaultJMX() throws IOException {
		Assertions.assertNull(PepperJMXHelper.convertToURL("String"));
	}

	@Test
	public void testConvertURL() throws IOException {
		Path testPath = PepperFileHelper.createTempPath("apex", "tmp", true);

		// We need the file to exist to enable derection of URL being a local file
		testPath.toFile().createNewFile();

		URL asURL = PepperJMXHelper.convertToURL(testPath.toString());

		// Check the URL maps actually to the file
		Assertions.assertNotNull(asURL.openStream());

		Assertions.assertEquals(testPath.toUri().toURL(), asURL);
	}

	@Test
	public void testConvertURL_withspace() throws IOException {
		Path testPath = PepperFileHelper.createTempPath("ap ex", "tmp", true);

		// We need the file to exist to enable derection of URL being a local file
		testPath.toFile().createNewFile();

		URL asURL = PepperJMXHelper.convertToURL(testPath.toString());

		// Check the URL maps actually to the file
		Assertions.assertNotNull(asURL.openStream());

		// https://stackoverflow.com/questions/60160/how-to-escape-text-for-regular-expression-in-java
		URL expectedUrl = testPath.toUri().toURL();
		Assertions.assertEquals(expectedUrl, asURL);
	}

}
