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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TestPepperCartesianProductHelper {
	public static final String A = "keyA";
	public static final Set<?> setA = Sets.newHashSet(A);

	public static final String B = "keyB";
	public static final Set<?> setB = Sets.newHashSet(B);

	public static final Set<?> setAB = Sets.newHashSet(A, B);

	public static final String C = "keyC";
	public static final String D = "keyD";
	public static final String M = "valueM";
	public static final Set<?> setM = Sets.newHashSet(M);

	public static final String N = "valueN";
	public static final Set<?> setN = Sets.newHashSet(N);

	public static final Set<?> setMN = Sets.newHashSet(M, N);

	@Test
	public void test_cotr() {
		Assert.assertNotNull(new CartesianProductHelper());
	}

	@Test
	public void testTrivialCases() {
		// Empty
		{
			Collection<? extends Map<?, ?>> input = Arrays.asList();
			Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
			Assert.assertTrue(result.isEmpty());
		}

		// OneMap
		{
			// one entry
			{
				Collection<? extends Map<?, ?>> input = Arrays.asList(ImmutableMap.of(A, M));
				Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
				Assert.assertEquals(1, result.size());
				Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setM));
			}
			// two entry
			{
				Collection<? extends Map<?, ?>> input = Arrays.asList(ImmutableMap.of(A, M, B, N));
				Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
				Assert.assertEquals(1, result.size());
				Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setM, B, setN));
			}
		}
	}

	@Test
	public void testNullValue() {
		// OneMap
		{
			// one entry
			{
				Collection<? extends Map<?, ?>> input = Arrays.asList(Collections.singletonMap(A, null));
				Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
				Assert.assertEquals(0, result.size());
			}
			// two entry
			{
				Map<String, Object> someNullValue = new HashMap<>();
				someNullValue.put(A, null);
				someNullValue.put(B, N);

				Collection<? extends Map<?, ?>> input = Arrays.asList(someNullValue);
				Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
				Assert.assertEquals(0, result.size());
			}
		}
	}

	@Test
	public void testEmptyMap() {
		// one empty map
		{
			Collection<? extends Map<?, ?>> input = Arrays.asList(Collections.emptyMap());
			Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
			Assert.assertEquals(1, result.size());
			Assert.assertEquals(Collections.singleton(Collections.emptyMap()), result);
		}

		// two empty maps
		{
			Collection<? extends Map<?, ?>> input = Arrays.asList(Collections.emptyMap(), Collections.emptyMap());
			Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
			Assert.assertEquals(1, result.size());
			Assert.assertEquals(Collections.singleton(Collections.emptyMap()), result);
		}

		// one empty map and one non-empty
		{
			Collection<? extends Map<?, ?>> input = Arrays.asList(Collections.emptyMap(), ImmutableMap.of(A, M));
			Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);

			// We keep only the empty pattern, as it is the biggest of both
			Assert.assertEquals(1, result.size());
			Assert.assertEquals(ImmutableSet.of(Collections.emptyMap()), result);
		}
	}

	@Test
	public void testSimpleCases() {
		// two entry
		{
			Collection<? extends Map<?, ?>> input = Arrays.asList(ImmutableMap.of(A, M), ImmutableMap.of(A, N));
			Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
			Assert.assertEquals(1, result.size());
			Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setMN));
		}
		// three entry
		{
			Collection<? extends Map<?, ?>> input =
					Arrays.asList(ImmutableMap.of(A, M), ImmutableMap.of(A, N), ImmutableMap.of(B, N));
			Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);

			Assert.assertEquals(2, result.size());
			Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setMN));
			Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(B, setN));
		}
		// four entry
		{
			Collection<? extends Map<?, ?>> input = Arrays
					.asList(ImmutableMap.of(A, M), ImmutableMap.of(A, N), ImmutableMap.of(B, M), ImmutableMap.of(B, N));
			Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);

			Assert.assertEquals(2, result.size());
			Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setMN));
			Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(B, setMN));
		}
	}

	@Test
	public void testSmallestCartesianProduct() {
		Collection<? extends Map<?, ?>> input = Arrays.asList(ImmutableMap.of(A, M, B, M),
				ImmutableMap.of(A, N, B, M),
				ImmutableMap.of(A, M, B, N),
				ImmutableMap.of(A, N, B, N));
		Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
		Assert.assertEquals(1, result.size());

		Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setMN, B, setMN));
	}

	@Test
	public void testCartesianProductWithCollections() {
		// We have Collections and some useless entries
		Collection<? extends Map<?, ?>> input = Arrays.asList(ImmutableMap.of(A, Arrays.asList(M, N), B, M),
				ImmutableMap.of(A, N, B, Arrays.asList(M, N)),
				ImmutableMap.of(A, M, B, N));
		Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);
		Assert.assertEquals(1, result.size());

		Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setMN, B, setMN));
	}

	@Test
	public void testMultipleKeySets() {
		// Some condition express only A, and others both A and B
		Collection<? extends Map<?, ?>> input = Arrays.asList(ImmutableMap.of(A, Arrays.asList(M, N), B, M),
				ImmutableMap.of(A, N, B, Arrays.asList(M, N)),
				ImmutableMap.of(A, M, B, N),
				ImmutableMap.of(A, N));
		Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);

		Assert.assertEquals(2, result.size());
		Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setM, B, setMN));
		Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setN));
	}

	@Test
	public void testNearlyCartesianProductButDuplicate() {

		// There is twice AMBM
		Collection<? extends Map<?, ?>> input = Arrays.asList(ImmutableMap.of(A, M, B, M),
				ImmutableMap.of(A, M, B, M),
				ImmutableMap.of(A, M, B, N),
				ImmutableMap.of(A, N, B, N));
		Set<? extends Map<?, ? extends Set<?>>> result = CartesianProductHelper.groupByKeyAndInValues(input);

		Assert.assertEquals(2, result.size());
		Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setM, B, setMN));
		Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, setN, B, setN));
	}

	@Test
	public void testMergeCartesianProduct() {
		Collection<? extends Set<? extends Map<String, ?>>> input = Arrays.asList(
				// (AM,BM)|(AM,BN) -> (AM,B(MN))
				ImmutableSet.of(ImmutableMap.of(A, M, B, M), ImmutableMap.of(A, M, B, N)),
				// (AM,BM)|(AN,BM) -> (A(MN),BM)
				ImmutableSet.of(ImmutableMap.of(A, M, B, M), ImmutableMap.of(A, N, B, M)));
		Set<? extends Map<String, ?>> result = CartesianProductHelper.mergeCartesianProducts(input);

		Assert.assertEquals(1, result.size());
		Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, M, B, M));
	}

	@Test
	public void testMergeCartesianProductIfContains() {
		Set<? extends Map<String, ?>> input =
				// (AM)|(AM,BN) -> (AM)
				ImmutableSet.of(ImmutableMap.of(A, M), ImmutableMap.of(A, M, B, N));
		Set<? extends Map<String, ?>> result = CartesianProductHelper.groupByKeyAndInValues(input);

		// We keep only the A->M constrain
		Assert.assertEquals(1, result.size());
		Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(A, ImmutableSet.of(M)));
	}

	@Test
	public void testCoverEscalier() {
		Set<? extends Map<String, ?>> input =
				// (A12345|B123|C1) -> (AB*123)|(A45)|C1
				ImmutableSet.of(ImmutableMap.of(A, 1, B, ImmutableSet.of(1, 2, 3, 4, 5)),
						ImmutableMap.of(A, 2, B, ImmutableSet.of(1, 2, 3)),
						ImmutableMap.of(A, 3, B, ImmutableSet.of(1, 2, 3)),
						ImmutableMap.of(A, 4, B, ImmutableSet.of(1)));
		Set<? extends Map<String, ?>> result = CartesianProductHelper.groupByKeyAndInValues(input);

		// We keep only the A->M constrain
		Assert.assertEquals(3, result.size());
		Assertions.assertThat((Set) result)
				.contains(ImmutableMap.of(A, ImmutableSet.of(1), B, ImmutableSet.of(1, 2, 3, 4, 5)));
		Assertions.assertThat((Set) result)
				.contains((Map) ImmutableMap.of(A, ImmutableSet.of(2, 3), B, ImmutableSet.of(1, 2, 3)));
		Assertions.assertThat((Set) result)
				.contains((Map) ImmutableMap.of(A, ImmutableSet.of(4), B, ImmutableSet.of(1)));
	}

	// We encountered a "java.lang.IllegalStateException: We have depleted keys without covering all maps" on this case
	@Test
	public void testSomeIssue() {
		// {[COUNTRY, CCY]=[{COUNTRY=USA, CCY=EUR}, {COUNTRY=USA, CCY=USD}], [CCY]=[{CCY=EUR}]}
		Set<? extends Map<String, ?>> input =
				ImmutableSet.of(ImmutableMap.of(A, 1, B, 2), ImmutableMap.of(A, 1, B, 3), ImmutableMap.of(B, 2));
		Set<? extends Map<String, ?>> result = CartesianProductHelper.groupByKeyAndInValues(input);

		// We keep only the A->M constrain
		Assert.assertEquals(2, result.size());
		Assertions.assertThat((Set) result).contains((Map) ImmutableMap.of(B, ImmutableSet.of(2)));
		Assertions.assertThat((Set) result)
				.contains((Map) ImmutableMap.of(A, ImmutableSet.of(1), B, ImmutableSet.of(3)));
	}

	@Test
	public void testSizeEmpty() {
		Assert.assertEquals(0, CartesianProductHelper.cartesianProductSize(Arrays.asList()));

		Assert.assertEquals(0, CartesianProductHelper.cartesianProductSize(Arrays.asList()));
	}

	@Test
	public void testSizeHuge() {
		long problemSize = 2000;
		Set<?> asSet = LongStream.range(0, problemSize).mapToObj(Long::valueOf).collect(Collectors.toSet());

		long expectedSize = problemSize * problemSize * problemSize;

		// Check we handle above Integer.MAX_VALUE
		Assert.assertTrue(expectedSize > Integer.MAX_VALUE);

		long hugeSize = CartesianProductHelper.cartesianProductSize(Arrays.asList(asSet, asSet, asSet));

		Assert.assertEquals(expectedSize, hugeSize);
	}
}
