/**
 * The MIT License
 * Copyright (c) 2023 Benoit Lacelle - SOLVEN
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class TestMapPathHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestMapPathHelper.class);

	@Test
	public void testFlattenKeys() {
		{
			Map<String, Object> flatten = MapPathHelper.flatten(ImmutableMap.of());
			Assertions.assertThat(flatten).hasSize(0);
		}
		{
			Map<String, Object> flatten = MapPathHelper.flatten(ImmutableMap.of("a", "b"));
			Assertions.assertThat(flatten).hasSize(1).containsEntry("$.a", "b");
		}
		{
			Map<String, Object> flatten = MapPathHelper.flatten(ImmutableMap.of("a", ImmutableMap.of("b", "c")));
			Assertions.assertThat(flatten).hasSize(1).containsEntry("$.a.b", "c");
		}
		{
			Map<String, Object> flatten =
					MapPathHelper.flatten(ImmutableMap.of("a", ImmutableMap.of("b", "c"), "d", "e"));
			Assertions.assertThat(flatten).hasSize(2).containsEntry("$.a.b", "c").containsEntry("$.d", "e");
		}
		{
			Map<String, Object> flatten =
					MapPathHelper.flatten(ImmutableMap.of("a", ImmutableMap.of("b", ImmutableMap.of("c", "d"))));
			Assertions.assertThat(flatten).hasSize(1).containsEntry("$.a.b.c", "d");
		}
		{
			Map<String, Object> flatten =
					MapPathHelper.flatten(ImmutableMap.of("a", ImmutableMap.of("b", "c1"), "a:b", "c2"));
			Assertions.assertThat(flatten).hasSize(2).containsEntry("$.a.b", "c1").containsEntry("$['a:b']", "c2");
		}

		{
			// A value is a Collection
			Map<String, Object> flatten = MapPathHelper.flatten(
					ImmutableMap.of("a", Arrays.asList(ImmutableMap.of("b", "c1"), ImmutableMap.of("d", "d1"))));
			Assertions.assertThat(flatten).hasSize(2).containsEntry("$.a[0].b", "c1").containsEntry("$.a[1].d", "d1");
		}
		{
			// Collection does not hold only Maps
			Map<String, Object> flatten =
					MapPathHelper.flatten(ImmutableMap.of("a", Arrays.asList(ImmutableMap.of("b", "c1"), "d")));
			Assertions.assertThat(flatten).hasSize(2).containsEntry("$.a[0].b", "c1").containsEntry("$.a[1]", "d");
		}

		{
			// A value is null
			Map<String, String> map = new HashMap<>();
			map.put("a", "b");
			map.put("c", null);
			Map<String, Object> flatten = MapPathHelper.flatten(map);
			Assertions.assertThat(flatten).hasSize(2).containsEntry("$.a", "b").containsEntry("$.c", null);
		}
	}

	@Test
	public void testFlattenKeys_forLog() {
		{
			Map<String, Object> flatten = MapPathHelper.flatten(Collections.singletonMap("k", null));
			Assertions.assertThat(flatten).hasSize(1).containsEntry("$.k", null);
		}
	}

	@Test
	public void testFlattenKeys_List() {
		{
			Map<String, Object> keys = MapPathHelper.flatten(ImmutableList.of("a", "b"));
			Assertions.assertThat(keys).hasSize(2).containsEntry("$[0]", "a").containsEntry("$[1]", "b");
		}

		{
			Map<String, Object> keys = MapPathHelper.flatten(Arrays.asList("a", null));
			Assertions.assertThat(keys).hasSize(2).containsEntry("$[0]", "a").containsEntry("$[1]", null);
		}

		{
			Map<String, Object> keys = MapPathHelper.flatten(ImmutableList.of(ImmutableMap.of("a", "b")));
			Assertions.assertThat(keys).hasSize(1).containsEntry("$[0].a", "b");
		}

		{
			Map<String, Object> keys = MapPathHelper.flatten(ImmutableList
					.of(ImmutableMap.of("a", "b1"), ImmutableMap.of("a", "b2"), ImmutableMap.of("c", "d", "e", "f")));
			Assertions.assertThat(keys)
					.hasSize(4)
					.containsEntry("$[0].a", "b1")
					.containsEntry("$[1].a", "b2")
					.containsEntry("$[2].c", "d")
					.containsEntry("$[2].e", "f");
		}
	}

	@Test
	public void testFlattenKeys_MapListMap() {
		Map<String, Object> flatten = MapPathHelper.flatten(ImmutableMap.of("data",
				ImmutableList.of(ImmutableMap.of("a", "b1"),
						ImmutableMap.of("a", "b2"),
						ImmutableMap.of("c", "d", "e", "f"))));
		Assertions.assertThat(flatten)
				.hasSize(4)
				.containsEntry("$.data[0].a", "b1")
				.containsEntry("$.data[1].a", "b2")
				.containsEntry("$.data[2].c", "d")
				.containsEntry("$.data[2].e", "f");
	}

	@Test
	public void testRecurse_brokenEntry() {
		Map<String, Object> flatten = MapPathHelper.flatten(ImmutableMap.of("k", "v"));

		flatten.put("k2", "v2");

		Assertions.assertThatThrownBy(() -> MapPathHelper.recurse(flatten))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testRecursiveFromFlatten() {
		Map<String, ?> inputRecursive = ImmutableMap.of("k1",
				"v1",
				"k2",
				Arrays.asList(ImmutableMap.of("k3", "v3", "k4", ImmutableMap.of("k5", "v5")),
						ImmutableMap.of("k6", "v6")));

		Map<String, Object> flatten = MapPathHelper.flatten(inputRecursive);
		Assertions.assertThat(flatten)
				.hasSize(4)
				.containsEntry("$.k1", "v1")
				.containsEntry("$.k2[0].k3", "v3")
				.containsEntry("$.k2[0].k4.k5", "v5")
				.containsEntry("$.k2[1].k6", "v6");

		Map<String, ?> backToRecursive = MapPathHelper.recurse(flatten);

		Assert.assertEquals(inputRecursive, backToRecursive);
	}

	// https://github.com/solven-eu/mitrust-datasharing/pull/11869
	@Test
	public void testRecursiveFromFlatten_upperCase() {
		Map<String, ?> inputRecursive = ImmutableMap.of("aBcD", "eFgH");

		Map<String, Object> flatten = MapPathHelper.flatten(inputRecursive);
		Assertions.assertThat(flatten).containsEntry("$.aBcD", "eFgH").hasSize(1);

		Map<String, ?> backToRecursive = MapPathHelper.recurse(flatten);

		Assert.assertEquals(inputRecursive, backToRecursive);
	}

	// @Test
	// public void testLooksRecursive_minimal() {
	// Map<String, Object> flat = ImmutableMap.of("k1", "v1");
	// Assertions.assertThat(MapPathHelper.looksRecursive(flat)).isTrue();
	// }
	//
	// @Test
	// public void testLooksRecursive_mapInput() {
	// Map<String, Object> recursive = ImmutableMap.of("k1", ImmutableMap.of("k2", "v1"));
	// Assertions.assertThat(MapPathHelper.looksRecursive(recursive)).isTrue();
	// }
	//
	// @Test
	// public void testLooksRecursive_mapOfList() {
	// Map<String, ?> inputRecursive = ImmutableMap.of("k1", "v1", "k2", Collections.emptyList());
	// Assertions.assertThat(MapPathHelper.looksRecursive(inputRecursive)).isTrue();
	// }
	//
	// @Test
	// public void testLooksRecursive_dottedKey() {
	// Map<String, Object> flat = ImmutableMap.of("k1.k2", "v2");
	// Assertions.assertThat(MapPathHelper.looksRecursive(flat)).isFalse();
	// }
	//
	// @Test
	// public void testLooksRecursive_minimalAndDotted() {
	// Map<String, Object> flat = ImmutableMap.of("k1", "v1", "k2.k3", "v2");
	// Assertions.assertThat(MapPathHelper.looksRecursive(flat)).isFalse();
	// }
	//
	// @Test
	// public void testLooksRecursive_dottedKey_mapValue() {
	// Map<String, Object> mapValues = ImmutableMap.of("k1.k2", ImmutableMap.of());
	// Assertions.assertThat(MapPathHelper.looksRecursive(mapValues)).isTrue();
	// }
	//
	// @Test
	// public void testLooksRecursive_Empty() {
	// Map<String, ?> inputRecursive = ImmutableMap.of("k1", "v1", "k2", Collections.emptyMap());
	// Assertions.assertThat(MapPathHelper.looksRecursive(inputRecursive)).isTrue();
	// }
	//
	// @Test
	// public void testLooksRecursive_Empty_Deeper() {
	// Map<String, ?> inputRecursive =
	// ImmutableMap.of("k1", "v1", "k2", ImmutableMap.of("k3", Collections.emptyMap()));
	// Assertions.assertThat(MapPathHelper.looksRecursive(inputRecursive)).isTrue();
	// }
	//
	// @Test
	// public void testLooksRecursive_emptyKey() {
	// Map<String, Object> flat = ImmutableMap.of("", "v");
	// Assertions.assertThat(MapPathHelper.looksRecursive(flat)).isTrue();
	// }
	//
	// @Test
	// public void testLooksRecursive_nullKey() {
	// Map<String, Object> flat = Collections.singletonMap(null, "v");
	// Assertions.assertThat(MapPathHelper.looksRecursive(flat)).isTrue();
	// }

	// @Test
	// public void test_flattenMultipleTimes_withDots() {
	// Map<String, Object> recursive = ImmutableMap.of("k.1", ImmutableMap.of("k.2", "v1"));
	// Map<String, Object> flat1 = MapPathHelper.flatten(recursive);
	// // Flattening a flat structure should be a no-op, not a fail
	// Map<String, Object> flat2 = MapPathHelper.flatten(flat1);
	// Assertions.assertThat(flat2).isEqualTo(flat1);
	// }

	// @Test
	// public void testSplit() {
	// Assertions.assertThat(MapPathHelper.split("k..1.k..2")).containsExactly("k.1", "k.2");
	// }

	@Test
	public void testRecursiveFromFlatten_withDotMiddle() {
		Map<String, ?> inputRecursive = ImmutableMap.of("k.1", ImmutableMap.of("k.2", "someValue"));

		Map<String, Object> flatten = MapPathHelper.flatten(inputRecursive);
		Assertions.assertThat(flatten).containsEntry("$['k.1']['k.2']", "someValue").hasSize(1);

		Map<String, ?> backToRecursive = MapPathHelper.recurse(flatten);
		Assert.assertEquals(inputRecursive, backToRecursive);
	}

	@Test
	public void testRecursiveFromFlatten_withRejectedDot() {
		{
			Map<String, ?> input = ImmutableMap.of(".1", ImmutableMap.of(".2", "someValue"));
			NavigableMap<String, Object> flatten = MapPathHelper.flatten(input);
			Assertions.assertThat(flatten).containsEntry("$['.1']['.2']", "someValue").hasSize(1);

			Map<String, ?> backToRecursive = MapPathHelper.recurse(flatten);
			Assert.assertEquals(input, backToRecursive);
		}

		{
			Map<String, ?> input = ImmutableMap.of("1.", ImmutableMap.of("2.", "someValue"));
			NavigableMap<String, Object> flatten = MapPathHelper.flatten(input);
			Assertions.assertThat(flatten).containsEntry("$['1.']['2.']", "someValue").hasSize(1);

			Map<String, ?> backToRecursive = MapPathHelper.recurse(flatten);
			Assert.assertEquals(input, backToRecursive);
		}
	}

	// @Test
	// public void testRecursiveOverDots() {
	// Map<String, Object> input = ImmutableMap.of("k.1", ImmutableMap.of("k.2", "v1"));
	// Map<String, ?> recursiveOverDots = MapPathHelper.recursiveOverDots(input);
	//
	// Assertions.assertThat(recursiveOverDots)
	// .isEqualTo(
	// ImmutableMap.of("k", ImmutableMap.of("1", ImmutableMap.of("k", ImmutableMap.of("2", "v1")))));
	// }

	@Test
	public void testOverIntermediateList() {
		Map<String, List<String>> input = ImmutableMap.of("k", ImmutableList.of("a", "b"));
		NavigableMap<String, Object> flatten = MapPathHelper.flatten(input);

		Assertions.assertThat(flatten).hasSize(2).containsEntry("$.k[0]", "a").containsEntry("$.k[1]", "b");

		Map<String, Object> back = MapPathHelper.recurse(flatten);
		Assertions.assertThat(back).isEqualTo(input);
	}

	@Test
	public void testOverIntermediateList_ListofList() {
		Map<String, List<Object>> input = ImmutableMap.of("k", ImmutableList.of("a", ImmutableList.of("b", "c")));
		NavigableMap<String, Object> flatten = MapPathHelper.flatten(input);

		Assertions.assertThat(flatten)
				.hasSize(3)
				.containsEntry("$.k[0]", "a")
				.containsEntry("$.k[1][0]", "b")
				.containsEntry("$.k[1][1]", "c");

		Map<String, Object> back = MapPathHelper.recurse(flatten);
		Assertions.assertThat(back).isEqualTo(input);
	}

	@Test
	public void testOverIntermediateList_TrailingNull() {
		Map<String, List<String>> input = ImmutableMap.of("k", Arrays.asList("a", null));
		NavigableMap<String, Object> flatten = MapPathHelper.flatten(input);

		Assertions.assertThat(flatten).hasSize(2).containsEntry("$.k[0]", "a").containsEntry("$.k[1]", null);

		Map<String, Object> back = MapPathHelper.recurse(flatten);
		// The trailing null is removed
		Assertions.assertThat(back).isEqualTo(input);
	}

	@Test
	public void testOverIntermediateList_LeadingNull() {
		Map<String, List<String>> input = ImmutableMap.of("k", Arrays.asList(null, "a"));
		NavigableMap<String, Object> flatten = MapPathHelper.flatten(input);

		Assertions.assertThat(flatten).hasSize(2).containsEntry("$.k[0]", null).containsEntry("$.k[1]", "a");

		Map<String, Object> back = MapPathHelper.recurse(flatten);
		// The trailing null is removed
		Assertions.assertThat(back).isEqualTo(input);
	}

	@Test
	public void testOverIntermediateList_Complex() {
		Map<String, ?> input = ImmutableMap.of("k",
				ImmutableList.of("a",
						ImmutableMap.of("k2",
								ImmutableList.of("b",
										ImmutableMap.of("k3", ImmutableList.of("c", ImmutableMap.of("k4", "d")))))));
		Map<String, Object> flatten = MapPathHelper.flatten(input);

		Assertions.assertThat(flatten)
				.hasSize(4)
				.containsEntry("$.k[0]", "a")
				.containsEntry("$.k[1].k2[0]", "b")
				.containsEntry("$.k[1].k2[1].k3[0]", "c")
				.containsEntry("$.k[1].k2[1].k3[1].k4", "d");

		Map<String, Object> back = MapPathHelper.recurse(flatten);
		Assertions.assertThat(back).isEqualTo(input);
	}

	@Test
	public void testFlattenWithGapInList() {
		Map<String, ?> flattenWithGapInList = ImmutableMap.<String, String>builder().put("$.k[1]", "v1").build();

		Map<String, Object> recursive = MapPathHelper.recurse(flattenWithGapInList);
		Assertions.assertThat(recursive)
				.isEqualTo(ImmutableMap.<String, Object>builder().put("k", Arrays.asList(null, "v1")).build());
	}

	@Test
	public void testFlatten_weirdKey() {
		Map<String, ?> inputRecursive = ImmutableMap.<String, Object>builder()
				.put("k1-_:()", "v1")
				.put("k2[", "v2")
				.put("k3]", "v3")
				.put("a'b", "v4")
				.build();

		Map<String, Object> flatten = MapPathHelper.flatten(inputRecursive);
		Assertions.assertThat(flatten)
				.hasSize(4)
				.containsEntry("$['k1-_:()']", "v1")
				.containsEntry("$['k2\\[']", "v2")
				.containsEntry("$['k3\\]']", "v3")
				.containsEntry("$['a\\'b']", "v4");

		Map<String, ?> backToRecursive = MapPathHelper.recurse(flatten);

		Assert.assertEquals(inputRecursive, backToRecursive);
	}
}
