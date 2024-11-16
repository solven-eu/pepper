package eu.solven.pepper.mappath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;

public class TestMapPathPut {

	@Test
	public void testPutEntry_writeRootNotWritable() {

		Assertions.assertThatThrownBy(() -> MapPathPut.putEntry(Collections.emptyMap(), "someValue", "someKey"))
				.isInstanceOf(RuntimeException.class)
				.hasRootCauseInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void testPutEntry_writeRoot() {
		Map<Object, Object> mutableRoot = new HashMap<>();
		MapPathPut.putEntry(mutableRoot, "someValue", "someKey");
	}

	@Test
	public void testPutEntry_overwriteRoot() {
		Map<Object, Object> mutableRoot = new HashMap<>();
		var singleKey = "someKey";
		mutableRoot.put(singleKey, "initial");
		MapPathPut.putEntry(mutableRoot, "update", singleKey);
		Assertions.assertThat(mutableRoot.get(singleKey)).isEqualTo("update");
	}

	@Test
	public void testPutEntry_intermediateKeysAreMissing() {
		Map<Object, Object> mutableRoot = new HashMap<>();
		MapPathPut.putEntry(mutableRoot, "update", "firstKey", "secondKey");
		Assertions.assertThat(mutableRoot.get("firstKey")).isEqualTo(ImmutableMap.of("secondKey", "update"));
	}

	@Test
	public void testPutEntry_updateDeepKey_chainNotUpdatable() {
		Map<Object, Object> immutableRoot = ImmutableMap.of("rootKey", ImmutableMap.of("subKey", "initial"));

		Assertions.assertThatThrownBy(() -> MapPathPut.putEntry(immutableRoot, "update", "rootKey", "subKey"))
				.isInstanceOf(RuntimeException.class)
				.hasRootCauseInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	public void testPutEntry_updateDeepKey() {
		Map<Object, Object> mutableRoot =
				new LinkedHashMap<>(ImmutableMap.of("rootKey", ImmutableMap.of("subKey", "initial")));
		MapPathPut.putEntry(mutableRoot, "update", "rootKey", "subKey");
		Assertions.assertThat(ImmutableMap.of("subKey", "update")).isEqualTo(mutableRoot.get("rootKey"));
	}

	@Test
	public void testPutEntry_valueIsMap() {
		Map<Object, Object> mutableRoot = new LinkedHashMap<>(ImmutableMap.of("rootKey",
				ImmutableMap.builder().put("subKey", "initial").put("otherKey", "otherValue").build()));
		MapPathPut.putEntry(mutableRoot, ImmutableMap.of("subKey", "update"), "rootKey");
		Assertions.assertThat(mutableRoot.get("rootKey"))
				.isEqualTo(ImmutableMap.builder().put("subKey", "update").put("otherKey", "otherValue").build());
	}

	@Test
	public void testPutEntry_MapListMap() {
		Map<String, Object> root = new LinkedHashMap<>();
		List<Object> list = new ArrayList<>();
		Map<String, Object> inner = new LinkedHashMap<>();

		list.add(inner);
		root.put("k", list);

		MapPathPut.putEntry(root, "someValue", "k", 0, "k2");

		// Ensure we did not change the list size
		Assertions.assertThat(list).hasSize(1);

		Assertions.assertThat(inner).containsEntry("k2", "someValue");
	}

	@Test
	public void testPutEntry_ListMapList() {
		List<Object> root = new ArrayList<>();
		Map<String, Object> map = new LinkedHashMap<>();
		List<Object> inner = new ArrayList<>();

		inner.add("initialValue");
		map.put("k", inner);
		root.add(map);

		MapPathPut.putEntry(root, "someValue", 0, "k", 0);

		// Ensure we did not change the list size
		Assertions.assertThat(root).hasSize(1);
		Assertions.assertThat(inner).hasSize(1);

		Assertions.assertThat(inner).element(0).isEqualTo("someValue");
	}

	// We insert a Map.Entry in a Map in a List
	@Test
	public void testPutEntry_MapList_unmappedBefore_putMapEntryInList() {
		Map<String, Object> map = new LinkedHashMap<>();
		var someKey = "someKey";
		var someValue = "someValue";

		MapPathPut.putEntry(map, someValue, "elements", 0, someKey);

		Assertions.assertThat(map).containsEntry("elements", List.of(Map.of(someKey, someValue))).hasSize(1);
	}

	// We insert a Map.Entry in a Map in a List
	@Test
	public void testPutEntry_MapList_unmappedBefore_putDeepMapEntryInList() {
		Map<String, Object> map = new LinkedHashMap<>();
		var someKey = "someKey";
		var otherKey = "otherKey";
		var someValue = "someValue";

		MapPathPut.putEntry(map, someValue, "elements", 0, someKey, otherKey);

		Assertions.assertThat(map)
				.containsEntry("elements", List.of(Map.of(someKey, Map.of(otherKey, someValue))))
				.hasSize(1);
	}

	// We insert a Map in a List
	@Test
	public void testPutEntry_MapList_unmappedBefore_putMapInList() {
		Map<String, Object> map = new LinkedHashMap<>();
		var someKey = "someKey";
		var someValue = "someValue";

		MapPathPut.putEntry(map, Map.of(someKey, someValue), "elements", 0);

		Assertions.assertThat(map).containsEntry("elements", List.of(Map.of(someKey, someValue))).hasSize(1);
	}

	// We insert a String in a List
	@Test
	public void testPutEntry_MapList_unmappedBefore_putStringInList() {
		Map<String, Object> map = new LinkedHashMap<>();

		MapPathPut.putEntry(map, "someString", "elements", 0);

		Assertions.assertThat(map).containsEntry("elements", List.of("someString")).hasSize(1);
	}

	@Test
	public void testPutEntry_List_negative() {
		Map<String, Object> map = new LinkedHashMap<>();
		var someKey = "someKey";
		var someValue = "someValue";

		Assertions.assertThatThrownBy(() -> MapPathPut.putEntry(map, someValue, "elements", -1, someKey))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testPutEntry_ListMapList_OutOfRange() {
		List<Object> root = new ArrayList<>();
		Map<String, Object> map = new LinkedHashMap<>();
		List<Object> inner = new ArrayList<>();

		map.put("k", inner);
		root.add(map);

		MapPathPut.putEntry(root, "someValue", 0, "k", 999);
		Assertions.assertThat(root.get(0))
				.asInstanceOf(InstanceOfAssertFactories.MAP)
				.hasSize(1)
				.containsKey("k")
				.extracting("k")
				.asInstanceOf(InstanceOfAssertFactories.LIST)
				.hasSize(1000)
				.element(999)
				.isEqualTo("someValue");
	}

	@Test
	public void testMergedPutAll_newKeys() {
		Map<Object, Object> mutableRoot =
				new LinkedHashMap<>(ImmutableMap.of("rootKey", ImmutableMap.of("subKey", "initial")));

		Map<Object, Object> toInsert =
				ImmutableMap.builder().put("a", "b").put("rootKey", ImmutableMap.of("otherSubKey", "other")).build();
		MapPathPut.mergedPutAll(mutableRoot, toInsert);

		Assertions.assertThat(mutableRoot.get("a")).isEqualTo("b");
		Assertions.assertThat(mutableRoot.get("rootKey"))
				.isEqualTo(ImmutableMap.builder().put("subKey", "initial").put("otherSubKey", "other").build());
	}

	@Test
	public void testMergedPutAll_updateKeys() {
		Map<Object, Object> mutableRoot = new LinkedHashMap<>(ImmutableMap.builder()
				.put("a", "b")
				.put("rootKey",
						ImmutableMap.builder().put("subKey", "initial").put("otherSubKey", "otherValue").build())
				.build());

		Map<Object, Object> toInsert =
				ImmutableMap.builder().put("a", "c").put("rootKey", ImmutableMap.of("subKey", "update")).build();
		MapPathPut.mergedPutAll(mutableRoot, toInsert);

		Assertions.assertThat(mutableRoot.get("a")).isEqualTo("c");
		Assertions.assertThat(mutableRoot.get("rootKey"))
				.isEqualTo(ImmutableMap.builder().put("subKey", "update").put("otherSubKey", "otherValue").build());
	}

}
