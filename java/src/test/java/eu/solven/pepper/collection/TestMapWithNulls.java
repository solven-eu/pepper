/**
 * The MIT License
 * Copyright (c) 2026 Benoit Lacelle - SOLVEN
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

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestMapWithNulls {
	@Test
	public void test1() {
		Map<String, Object> map = MapWithNulls.of("a", null);
		Assertions.assertThat(map)
				.hasToString("{a=null}")
				.containsKey("a")
				.containsEntry("a", null)
				.hasEntrySatisfying("a", v -> Assertions.assertThat(v).isNull());
	}

	@Test
	public void test2() {
		// check a `null->null` entry
		Map<String, Object> map = MapWithNulls.of("a", null, null, null);
		Assertions.assertThat(map)
				.hasToString("{a=null, null=null}")
				.containsKey("a")
				.containsKey(null)
				.containsEntry("a", null)
				.containsEntry(null, null)
				.hasEntrySatisfying("a", v -> Assertions.assertThat(v).isNull())
				.hasEntrySatisfying(null, v -> Assertions.assertThat(v).isNull());
	}

	@Test
	public void test3() {
		// Check we smoothly handle overloading a key
		Map<String, Object> map = MapWithNulls.of("a", null, null, null, null, "b1");
		Assertions.assertThat(map)
				.hasToString("{a=null, null=b1}")
				.containsKey("a")
				.containsKey(null)
				.containsEntry("a", null)
				.containsEntry(null, "b1")
				.hasEntrySatisfying("a", v -> Assertions.assertThat(v).isNull())
				.hasEntrySatisfying(null, v -> Assertions.assertThat(v).isEqualTo("b1"));
	}

	@Test
	public void test4() {
		// a float Value
		// a non-null entry
		// a long key
		Map<Object, Object> map = MapWithNulls.of("a", null, null, "b1", "c", 12.34, 123L, "d");
		Assertions.assertThat(map)
				.hasToString("{a=null, null=b1, c=12.34, 123=d}")
				.containsKey("a")
				.containsKey(null)
				.containsKey("c")
				.containsKey(123L)
				.containsEntry("a", null)
				.containsEntry(null, "b1")
				.containsEntry("c", 12.34)
				.containsEntry(123L, "d")
				.hasEntrySatisfying("a", v -> Assertions.assertThat(v).isNull())
				.hasEntrySatisfying(null, v -> Assertions.assertThat(v).isEqualTo("b1"))
				.hasEntrySatisfying("c", v -> Assertions.assertThat(v).isEqualTo(12.34))
				.hasEntrySatisfying(123L, v -> Assertions.assertThat(v).isEqualTo("d"));
	}

	@Test
	public void testBuilder() {
		Map<String, Object> map = MapWithNulls.<String, Object>builder()
				.put("a", null)
				.put(null, null)
				.put("c", 12.34)
				.put("d", 123L)
				.build();
		Assertions.assertThat(map)
				.hasToString("{a=null, null=null, c=12.34, d=123}")
				.containsKey("a")
				.containsKey(null)
				.containsKey("c")
				.containsKey("d")
				.containsEntry("a", null)
				.containsEntry(null, null)
				.containsEntry("c", 12.34)
				.containsEntry("d", 123L)
				.hasEntrySatisfying("a", v -> Assertions.assertThat(v).isNull())
				.hasEntrySatisfying(null, v -> Assertions.assertThat(v).isEqualTo(null))
				.hasEntrySatisfying("c", v -> Assertions.assertThat(v).isEqualTo(12.34))
				.hasEntrySatisfying("d", v -> Assertions.assertThat(v).isEqualTo(123L));
	}
}
