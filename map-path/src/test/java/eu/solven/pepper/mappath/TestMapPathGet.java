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
package eu.solven.pepper.mappath;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

import eu.solven.pepper.collection.PepperMapHelper;

public class TestMapPathGet {

	final LocalDate now = LocalDate.now();

	@Test
	public void testMissingKeyInMap() {
		Map<String, ?> map = ImmutableMap.of("someKey", "v");

		// Ensure the present keys are logged
		org.assertj.core.api.Assertions.assertThatThrownBy(() -> MapPathGet.getRequiredString(map, "requiredKey"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("someKey");
	}

	@Test
	public void testGetRequiredString_Recursive() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", "v"));

		Assertions.assertEquals("v", MapPathGet.getRequiredString(map, "k1", "k2"));
	}

	@Test
	public void testGetRequiredString_Recursive_NotString() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", OffsetDateTime.now()));

		Assertions.assertThrows(IllegalArgumentException.class, () -> MapPathGet.getRequiredString(map, "k1", "k2"));
	}

	@Test
	public void testGetRequiredString_Recursive_NotIntermediateMap() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1");

		Assertions.assertThrows(IllegalArgumentException.class, () -> MapPathGet.getRequiredString(map, "k1", "k2"));
	}

	@Test
	public void testGetRequiredString_Recursive_NotIntermediateMap_Deeper() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", ImmutableMap.of("k3", "v3")));

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> MapPathGet.getRequiredString(map, "k1", "k2", "notK3"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("k3")
				.hasMessageNotContaining("k1");
	}

	@Test
	public void testGetRequiredMap() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", "v"));

		Map<Object, Object> requiredMap = MapPathGet.getRequiredMap(map, "k1");
		Assertions.assertEquals(ImmutableMap.of("k2", "v"), requiredMap);
	}

	@Test
	public void testGetRequiredMap_Generic() {
		Map<String, ?> map = ImmutableMap.of("k1", ImmutableMap.of("k2", "v"));

		Map<String, ?> requiredMap = MapPathGet.getRequiredMap(map, "k1");
		Assertions.assertEquals(ImmutableMap.of("k2", "v"), requiredMap);
	}

	@Test
	public void testPresentKeyInMap() {
		Map<String, ?> map = ImmutableMap.of("requiredKey", "v");

		Assertions.assertEquals("v", MapPathGet.getRequiredString(map, "requiredKey"));
	}

	@Test
	public void testPresentNumberKeyInMap_Int() {
		Map<String, ?> map = ImmutableMap.of("requiredKey", 123);

		Assertions.assertEquals(123, MapPathGet.getRequiredNumber(map, "requiredKey"));
	}

	@Test
	public void testPresentNumberKeyInMap_String() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> MapPathGet.getRequiredNumber(ImmutableMap.of("requiredKey", "123"), "requiredKey"));
	}

	// Not a NullPointerException
	@Test
	public void testPresentNumberKeyInMap_null() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> MapPathGet.getRequiredNumber(Collections.singletonMap("requiredKey", null), "requiredKey"));
	}

	@Test
	public void testGetOptionalString_Ok() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1");

		Assertions.assertEquals("v1", MapPathGet.getOptionalString(map, "k1").get());
	}

	@Test
	public void testGetOptionalString_Deep() {
		Map<?, ?> map = ImmutableMap.of(123, ImmutableMap.of("k1", "v1"));

		Assertions.assertEquals("v1", MapPathGet.getOptionalString(map, 123, "k1").get());
	}

	@Test
	public void testGetOptionalString_Missing() {
		Map<String, ?> map = ImmutableMap.of("k1", "v1");

		Assertions.assertFalse(MapPathGet.getOptionalString(map, "k2").isPresent());
	}

	@Test
	public void testGetOptionalString_Null() {
		Map<String, ?> map = Collections.singletonMap("k1", null);

		Assertions.assertTrue(map.containsKey("k1"));
		Assertions.assertFalse(MapPathGet.getOptionalString(map, "k1").isPresent());
	}

	@Test
	public void testGetOptionalString_Empty() {
		Map<String, ?> map = Collections.singletonMap("k1", "");

		Assertions.assertTrue(map.containsKey("k1"));
		Assertions.assertFalse(MapPathGet.getOptionalString(map, "k1").isPresent());
	}

	@Test
	public void testGetOptionalString_WrongType() {
		Map<String, ?> map = Collections.singletonMap("k1", 123L);

		Assertions.assertThrows(IllegalArgumentException.class,
				() -> MapPathGet.getOptionalString(map, "k1").isPresent());
	}

	@Test
	public void testGetOptionalAs() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		Assertions.assertEquals(Optional.of("value"), MapPathGet.getOptionalAs(map, 123, "key", now));
		Assertions.assertEquals(Optional.of(ImmutableMap.of(now, "value")), MapPathGet.getOptionalAs(map, 123, "key"));
		Assertions.assertEquals(Optional.of(ImmutableMap.of("key", ImmutableMap.of(now, "value"))),
				MapPathGet.getOptionalAs(map, 123));
	}

	@Test
	public void testGetRequiredAs() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		Assertions.assertEquals("value", MapPathGet.getRequiredAs(map, 123, "key", now));
		Assertions.assertEquals(ImmutableMap.of(now, "value"), MapPathGet.getRequiredAs(map, 123, "key"));
		Assertions.assertEquals(ImmutableMap.of("key", ImmutableMap.of(now, "value")),
				MapPathGet.getRequiredAs(map, 123));
	}

	@Test
	public void testGetOptionalAs_missing() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalAs(map, 123, "key2")).isEmpty();
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalAs(map, 124)).isEmpty();
	}

	@Test
	public void testGetRequiredAs_missing_depth2() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		Assertions.assertThrows(IllegalArgumentException.class, () -> MapPathGet.getRequiredAs(map, 123, "key2"));
	}

	@Test
	public void testGetRequiredAs_missing_depth1() {
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap("value", 123, "key", now);

		Assertions.assertThrows(IllegalArgumentException.class, () -> MapPathGet.getRequiredAs(map, 124));
	}

	@Test
	public void testGetRequiredBoolean_true() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap(Boolean.TRUE, 123, "key", now);

		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalAs(map, 123, "key", now))
				.isPresent()
				.contains(Boolean.TRUE);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.<Object>getRequiredAs(map, 123, "key", now))
				.isEqualTo(Boolean.TRUE);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getRequiredBoolean(map, 123, "key", now))
				.isEqualTo(Boolean.TRUE);
	}

	@Test
	public void testGetRequiredBoolean_false() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap(Boolean.FALSE, 123, "key", now);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalAs(map, 123, "key", now))
				.isPresent()
				.contains(Boolean.FALSE);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.<Object>getRequiredAs(map, 123, "key", now))
				.isEqualTo(Boolean.FALSE);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getRequiredBoolean(map, 123, "key", now))
				.isEqualTo(Boolean.FALSE);
	}

	@Test
	public void testGetRequiredNumber_int() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap(456, 123, "key", now);

		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalAs(map, 123, "key", now))
				.isPresent()
				.contains(456);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.<Object>getRequiredAs(map, 123, "key", now))
				.isEqualTo(456);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getRequiredNumber(map, 123, "key", now)).isEqualTo(456);
	}

	@Test
	public void testGetRequiredNumber_float() {
		// Check keys can be of any-type
		Map<Integer, ?> map = PepperMapHelper.imbricatedMap(123.456F, 123, "key", now);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalAs(map, 123, "key", now))
				.isPresent()
				.contains(123.456F);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.<Object>getRequiredAs(map, 123, "key", now))
				.isEqualTo(123.456F);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getRequiredNumber(map, 123, "key", now))
				.isEqualTo(123.456F);
	}

	@Test
	public void testGetFromList_number() {
		Map<String, Object> root = new LinkedHashMap<>();

		root.put("k", Arrays.asList("someString", PepperMapHelper.imbricatedMap(123.456F, 123, "key", now)));

		// Check keys can be of any-type
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalAs(root, "k", 1, 123, "key", now))
				.isPresent()
				.contains(123.456F);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.<Object>getRequiredAs(root, "k", 1, 123, "key", now))
				.isEqualTo(123.456F);
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getRequiredNumber(root, "k", 1, 123, "key", now))
				.isEqualTo(123.456F);
	}

	@Test
	public void testGetFromList_string() {
		Map<String, Object> root = new LinkedHashMap<>();

		root.put("k", Arrays.asList("someString", PepperMapHelper.imbricatedMap("deepString", 123, "key", now)));

		// Check keys can be of any-type
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalAs(root, "k", 1, 123, "key", now))
				.isPresent()
				.contains("deepString");
		org.assertj.core.api.Assertions.assertThat(MapPathGet.<Object>getRequiredAs(root, "k", 1, 123, "key", now))
				.isEqualTo("deepString");
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getRequiredString(root, "k", 1, 123, "key", now))
				.isEqualTo("deepString");
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalString(root, "k", 1, 123, "key", now).get())
				.isEqualTo("deepString");
	}

	@Test
	public void testGetFromList_OutOfRange() {
		Map<String, Object> root = new LinkedHashMap<>();

		root.put("k", Arrays.asList("someString", PepperMapHelper.imbricatedMap("deepString", 123, "key", now)));

		// Check keys can be of any-type
		org.assertj.core.api.Assertions.assertThat(MapPathGet.getOptionalAs(root, "k", 3)).isEmpty();
	}
}
