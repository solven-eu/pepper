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
package eu.solven.pepper.collection;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

public class TestPepperMapHelper {

	final LocalDate now = LocalDate.now();

	@Test
	public void testMergeOnNullValue() {
		Map<String, Object> map = new HashMap<>();
		map.put("key", null);

		Map<String, Object> newMap = PepperMapHelper.transcodeColumns(ImmutableBiMap.of("key", "newKey"), map);

		Assertions.assertEquals(Collections.singletonMap("newKey", null), newMap);
	}

	@Test
	public void testDecoratePutAllOnNullValue() {
		Map<String, Object> first = new HashMap<>();
		first.put("key", null);

		Map<String, Object> second = new HashMap<>();
		second.put("key2", null);

		Map<String, Object> newMap = PepperMapHelper.decoratePutAll(first, second);

		Map<String, Object> merged = new HashMap<>();
		merged.put("key", null);
		merged.put("key2", null);
		Assertions.assertEquals(merged, newMap);
	}

	@Test
	public void fromLists() {
		Map<String, String> map = PepperMapHelper.fromLists(Arrays.asList("k1", "k2"), Arrays.asList("v1", "v2"));

		Assertions.assertEquals(ImmutableMap.of("k1", "v1", "k2", "v2"), map);
	}

	@Test
	public void testHideKey() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		Assertions.assertEquals(ImmutableMap.of("k2", "v2"), PepperMapHelper.hideKeys(map, "k1"));
	}

	@Test
	public void testHideKey_unknownType() {
		Map<?, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		Assertions.assertEquals(ImmutableMap.of("k2", "v2"), PepperMapHelper.hideKeys(map, "k1"));
	}

	@Test
	public void testHideKey_UnknownKey() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		Assertions.assertThrows(IllegalArgumentException.class, () -> PepperMapHelper.hideKeys(map, "k3"));
	}

	@Test
	public void testHideKeys_singleKey() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		Assertions.assertEquals(ImmutableMap.of("k2", "v2"), PepperMapHelper.hideKeys(map, "k1"));
	}

	@Test
	public void testHideKeys_multipleKeys_hideAll() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		Assertions.assertEquals(ImmutableMap.of(), PepperMapHelper.hideKeys(map, "k1", "k2"));
	}

	@Test
	public void testHideKeys_multipleKeys_hideSome() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3");

		Assertions.assertEquals(ImmutableMap.of("k2", "v2"), PepperMapHelper.hideKeys(map, "k1", "k3"));
	}

	@Test
	public void testTransferValue_notAlreadyPresent() {
		Map<String, ?> source = ImmutableMap.of("k1", "v1", "k2", "v2");
		Map<String, Object> sink = new LinkedHashMap<>();

		PepperMapHelper.transferValue("k1", source, sink);

		Assertions.assertEquals(ImmutableMap.of("k1", "v1"), sink);
	}

	@Test
	public void testTransferValue_alreadyPresent() {
		Map<String, ?> source = ImmutableMap.of("k1", "v1", "k2", "v2");
		Map<String, Object> sink = new LinkedHashMap<>();
		sink.put("k1", "alreadyPresent");

		PepperMapHelper.transferValue("k1", source, sink);

		Assertions.assertEquals(ImmutableMap.of("k1", "v1"), sink);
	}

	@Test
	public void testTransferValue_notExisting() {
		Map<String, ?> source = ImmutableMap.of("k1", "v1", "k2", "v2");
		Map<String, Object> sink = new LinkedHashMap<>();

		PepperMapHelper.transferValue("k?", source, sink);

		Assertions.assertEquals(ImmutableMap.of(), sink);
	}

	@Test
	public void testImbricatedMap() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key");

		Assertions.assertEquals(ImmutableMap.of(123, ImmutableMap.of("key", "value")), map);
	}

	@Test
	public void testImbricatedMap_Deeper_DifferentTypes() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		Assertions.assertEquals(ImmutableMap.of(123, ImmutableMap.of("key", ImmutableMap.of(now, "value"))), map);
	}

}
