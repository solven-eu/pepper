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
package eu.solven.pepper.collection;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

public class TestPepperMapHelper {

	final LocalDate now = LocalDate.now();

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

		// Ensure the present keys are logged
		Assertions.assertThatThrownBy(() -> PepperMapHelper.getRequiredString(map, "requiredKey"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("someKey");
	}

	@Test
	public void testGetRequiredString_Recursive() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", "v"));

		Assert.assertEquals("v", PepperMapHelper.getRequiredString(map, "k1", "k2"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetRequiredString_Recursive_NotString() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", OffsetDateTime.now()));

		PepperMapHelper.getRequiredString(map, "k1", "k2");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetRequiredString_Recursive_NotIntermediateMap() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1");

		PepperMapHelper.getRequiredString(map, "k1", "k2");
	}

	@Test
	public void testGetRequiredString_Recursive_NotIntermediateMap_Deeper() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", ImmutableMap.of("k3", "v3")));

		Assertions.assertThatThrownBy(() -> PepperMapHelper.getRequiredString(map, "k1", "k2", "notK3"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("k3")
				.hasMessageNotContaining("k1");
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
	public void testGetOptionalString_Deep() {
		Map<?, ?> map = ImmutableMap.of(123, ImmutableMap.of("k1", "v1"));

		Assert.assertEquals("v1", PepperMapHelper.getOptionalString(map, 123, "k1").get());
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

	@Test
	public void testImbricatedMap_Deeper_DifferentTypes() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		Assert.assertEquals(ImmutableMap.of(123, ImmutableMap.of("key", ImmutableMap.of(now, "value"))), map);
	}

	@Test
	public void testGetOptionalAs() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		Assert.assertEquals(Optional.of("value"), PepperMapHelper.getOptionalAs(map, 123, "key", now));
		Assert.assertEquals(Optional.of(ImmutableMap.of(now, "value")), PepperMapHelper.getOptionalAs(map, 123, "key"));
		Assert.assertEquals(Optional.of(ImmutableMap.of("key", ImmutableMap.of(now, "value"))),
				PepperMapHelper.getOptionalAs(map, 123));
	}

	@Test
	public void testGetRequiredAs() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		Assert.assertEquals("value", PepperMapHelper.getRequiredAs(map, 123, "key", now));
		Assert.assertEquals(ImmutableMap.of(now, "value"), PepperMapHelper.getRequiredAs(map, 123, "key"));
		Assert.assertEquals(ImmutableMap.of("key", ImmutableMap.of(now, "value")),
				PepperMapHelper.getRequiredAs(map, 123));
	}

	@Test
	public void testGetOptionalAs_missing() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		Assertions.assertThat(PepperMapHelper.getOptionalAs(map, 123, "key2")).isEmpty();
		Assertions.assertThat(PepperMapHelper.getOptionalAs(map, 124)).isEmpty();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetRequiredAs_missing_depth2() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		PepperMapHelper.getRequiredAs(map, 123, "key2");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetRequiredAs_missing_depth1() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		PepperMapHelper.getRequiredAs(map, 124);
	}

	@Test
	public void testGetRequiredBoolean_true() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap(Boolean.TRUE, 123, "key", now);

		Assertions.assertThat(PepperMapHelper.getOptionalAs(map, 123, "key", now)).isPresent().contains(Boolean.TRUE);
		Assertions.assertThat(PepperMapHelper.<Object>getRequiredAs(map, 123, "key", now)).isEqualTo(Boolean.TRUE);
		Assertions.assertThat(PepperMapHelper.getRequiredBoolean(map, 123, "key", now)).isEqualTo(Boolean.TRUE);
	}

	@Test
	public void testGetRequiredBoolean_false() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap(Boolean.FALSE, 123, "key", now);
		Assertions.assertThat(PepperMapHelper.getOptionalAs(map, 123, "key", now)).isPresent().contains(Boolean.FALSE);
		Assertions.assertThat(PepperMapHelper.<Object>getRequiredAs(map, 123, "key", now)).isEqualTo(Boolean.FALSE);
		Assertions.assertThat(PepperMapHelper.getRequiredBoolean(map, 123, "key", now)).isEqualTo(Boolean.FALSE);
	}

	@Test
	public void testGetRequiredNumber_int() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap(456, 123, "key", now);

		Assertions.assertThat(PepperMapHelper.getOptionalAs(map, 123, "key", now)).isPresent().contains(456);
		Assertions.assertThat(PepperMapHelper.<Object>getRequiredAs(map, 123, "key", now)).isEqualTo(456);
		Assertions.assertThat(PepperMapHelper.getRequiredNumber(map, 123, "key", now)).isEqualTo(456);
	}

	@Test
	public void testGetRequiredNumber_float() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap(123.456F, 123, "key", now);
		Assertions.assertThat(PepperMapHelper.getOptionalAs(map, 123, "key", now)).isPresent().contains(123.456F);
		Assertions.assertThat(PepperMapHelper.<Object>getRequiredAs(map, 123, "key", now)).isEqualTo(123.456F);
		Assertions.assertThat(PepperMapHelper.getRequiredNumber(map, 123, "key", now)).isEqualTo(123.456F);
	}

	@Test
	public void testGetFromList_number() {
		Map<String, Object> root = new LinkedHashMap<>();

		root.put("k", Arrays.asList("someString", PepperMapHelper.imbricatedMap(123.456F, 123, "key", now)));

		// Check keys can be of any-type
		Assertions.assertThat(PepperMapHelper.getOptionalAs(root, "k", 1, 123, "key", now))
				.isPresent()
				.contains(123.456F);
		Assertions.assertThat(PepperMapHelper.<Object>getRequiredAs(root, "k", 1, 123, "key", now)).isEqualTo(123.456F);
		Assertions.assertThat(PepperMapHelper.getRequiredNumber(root, "k", 1, 123, "key", now)).isEqualTo(123.456F);
	}

	@Test
	public void testGetFromList_string() {
		Map<String, Object> root = new LinkedHashMap<>();

		root.put("k", Arrays.asList("someString", PepperMapHelper.imbricatedMap("deepString", 123, "key", now)));

		// Check keys can be of any-type
		Assertions.assertThat(PepperMapHelper.getOptionalAs(root, "k", 1, 123, "key", now))
				.isPresent()
				.contains("deepString");
		Assertions.assertThat(PepperMapHelper.<Object>getRequiredAs(root, "k", 1, 123, "key", now))
				.isEqualTo("deepString");
		Assertions.assertThat(PepperMapHelper.getRequiredString(root, "k", 1, 123, "key", now)).isEqualTo("deepString");
		Assertions.assertThat(PepperMapHelper.getOptionalString(root, "k", 1, 123, "key", now).get())
				.isEqualTo("deepString");
	}

	@Test
	public void testGetFromList_OutOfRange() {
		Map<String, Object> root = new LinkedHashMap<>();

		root.put("k", Arrays.asList("someString", PepperMapHelper.imbricatedMap("deepString", 123, "key", now)));

		// Check keys can be of any-type
		Assertions.assertThat(PepperMapHelper.getOptionalAs(root, "k", 3)).isEmpty();
	}

}
