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
package cormoran.pepper.collection;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

public class TestPepperMapHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestPepperMapHelper.class);

	@Test
	public void testMergeOnNullValue() {
		Map<String, Object> map = new HashMap<>();
		map.put("key", null);

		Map<String, Object> newMap = PepperMapHelper.transcodeColumns(ImmutableBiMap.of("key", "newKey"), map);

		Assert.assertEquals(Collections.singletonMap("newKey", null), newMap);
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
		Assert.assertEquals(merged, newMap);
	}

	@Test
	public void fromLists() {
		Map<String, String> map = PepperMapHelper.fromLists(Arrays.asList("k1", "k2"), Arrays.asList("v1", "v2"));

		Assert.assertEquals(ImmutableMap.of("k1", "v1", "k2", "v2"), map);
	}

	@Test
	public void testMissingKeyInMap() {
		Map<String, ?> map = ImmutableMap.of("someKey", "v");

		try {
			PepperMapHelper.getRequiredString(map, "requiredKey");
			Assert.fail("Should reject on missing");
		} catch (RuntimeException e) {
			LOGGER.trace("Arg", e);
			Assert.assertTrue(e instanceof IllegalArgumentException);
			// Ensure the present keys are logged
			Assertions.assertThat(e.getMessage()).contains("someKey");
		}
	}

	@Test
	public void testGetRequiredStringRecursive() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", "v"));

		Assert.assertEquals("v", PepperMapHelper.getRequiredString(map, "k1", "k2"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetRequiredStringRecursive_NotString() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", OffsetDateTime.now()));

		PepperMapHelper.getRequiredString(map, "k1", "k2");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetRequiredStringRecursive_NotIntermediateMap() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1");

		PepperMapHelper.getRequiredString(map, "k1", "k2");
	}

	@Test
	public void testGetRequiredMap() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", "v"));

		Map<Object, Object> requiredMap = PepperMapHelper.getRequiredMap(map, "k1");
		Assert.assertEquals(ImmutableMap.of("k2", "v"), requiredMap);
	}

	@Test
	public void testGetRequiredMap_Generic() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", "v"));

		Map<String, ?> requiredMap = PepperMapHelper.getRequiredMap(map, "k1");
		Assert.assertEquals(ImmutableMap.of("k2", "v"), requiredMap);
	}

	@Test
	public void testPresentKeyInMap() {
		Map<String, ?> map = ImmutableMap.of("requiredKey", "v");

		Assert.assertEquals("v", PepperMapHelper.getRequiredString(map, "requiredKey"));
	}

	@Test
	public void testPresentNumberKeyInMap_Int() {
		Map<String, ?> map = ImmutableMap.of("requiredKey", 123);

		Assert.assertEquals(123, PepperMapHelper.getRequiredNumber(map, "requiredKey"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPresentNumberKeyInMap_String() {
		PepperMapHelper.getRequiredNumber(ImmutableMap.of("requiredKey", "123"), "requiredKey");
	}

	// Not a NullPointerException
	@Test(expected = IllegalArgumentException.class)
	public void testPresentNumberKeyInMap_null() {
		PepperMapHelper.getRequiredNumber(Collections.singletonMap("requiredKey", null), "requiredKey");
	}

	@Test
	public void testHideKey() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		Assert.assertEquals(ImmutableMap.of("k2", "v2"), PepperMapHelper.hideKeys(map, "k1"));
	}

	@Test
	public void testHideKey_unknownType() {
		Map<?, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		Assert.assertEquals(ImmutableMap.of("k2", "v2"), PepperMapHelper.hideKeys(map, "k1"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHideKey_UnknownKey() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		PepperMapHelper.hideKeys(map, "k3");
	}

	@Test
	public void testHideKeys_singleKey() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		Assert.assertEquals(ImmutableMap.of("k2", "v2"), PepperMapHelper.hideKeys(map, "k1"));
	}

	@Test
	public void testHideKeys_multipleKeys_hideAll() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2");

		Assert.assertEquals(ImmutableMap.of(), PepperMapHelper.hideKeys(map, "k1", "k2"));
	}

	@Test
	public void testHideKeys_multipleKeys_hideSome() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3");

		Assert.assertEquals(ImmutableMap.of("k2", "v2"), PepperMapHelper.hideKeys(map, "k1", "k3"));
	}

	@Test
	public void testGetOptionalString_Ok() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1");

		Assert.assertEquals("v1", PepperMapHelper.getOptionalString(map, "k1").get());
	}

	@Test
	public void testGetOptionalString_Missing() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1");

		Assert.assertFalse(PepperMapHelper.getOptionalString(map, "k2").isPresent());
	}

	@Test
	public void testGetOptionalString_Null() {
		Map<String, ?> map = Collections.singletonMap("k1", null);

		Assert.assertTrue(map.containsKey("k1"));
		Assert.assertFalse(PepperMapHelper.getOptionalString(map, "k1").isPresent());
	}

	@Test
	public void testGetOptionalString_Empty() {
		Map<String, ?> map = Collections.singletonMap("k1", "");

		Assert.assertTrue(map.containsKey("k1"));
		Assert.assertFalse(PepperMapHelper.getOptionalString(map, "k1").isPresent());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetOptionalString_WrongType() {
		Map<String, ?> map = Collections.singletonMap("k1", 123L);

		PepperMapHelper.getOptionalString(map, "k1").isPresent();
	}

	@Test
	public void testTransferValue_notAlreadyPresent() {
		Map<String, ?> source = ImmutableMap.of("k1", "v1", "k2", "v2");
		Map<String, Object> sink = new LinkedHashMap<>();

		PepperMapHelper.transferValue("k1", source, sink);

		Assert.assertEquals(ImmutableMap.of("k1", "v1"), sink);
	}

	@Test
	public void testTransferValue_alreadyPresent() {
		Map<String, ?> source = ImmutableMap.of("k1", "v1", "k2", "v2");
		Map<String, Object> sink = new LinkedHashMap<>();
		sink.put("k1", "alreadyPresent");

		PepperMapHelper.transferValue("k1", source, sink);

		Assert.assertEquals(ImmutableMap.of("k1", "v1"), sink);
	}

	@Test
	public void testTransferValue_notExisting() {
		Map<String, ?> source = ImmutableMap.of("k1", "v1", "k2", "v2");
		Map<String, Object> sink = new LinkedHashMap<>();

		PepperMapHelper.transferValue("k?", source, sink);

		Assert.assertEquals(ImmutableMap.of(), sink);
	}

	@Test
	public void testImbricatedMap() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key");

		Assert.assertEquals(ImmutableMap.of(123, ImmutableMap.of("key", "value")), map);
	}

}
