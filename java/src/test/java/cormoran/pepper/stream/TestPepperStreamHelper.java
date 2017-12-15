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
package cormoran.pepper.stream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.primitives.Booleans;

public class TestPepperStreamHelper {
	@Test
	public void testPartitionConsume_nolimit() {
		List<List<Integer>> spot = new ArrayList<>();

		long nbDone = PepperStreamHelper.consumeByPartition(() -> new LinkedBlockingQueue<>(),
				IntStream.range(0, 12345).mapToObj(Integer::valueOf),
				c -> spot.add(Lists.newArrayList(c)));

		Assert.assertEquals(12345, nbDone);

		Set<Integer> consumedSizes = spot.stream().map(Collection::size).collect(Collectors.toSet());

		Assert.assertEquals(ImmutableSet.of(12345), consumedSizes);
	}

	@Test
	public void testPartitionConsume_limit100() {
		List<List<Integer>> spot = new ArrayList<>();

		long nbDone = PepperStreamHelper.consumeByPartition(IntStream.range(0, 12345).mapToObj(Integer::valueOf),
				c -> spot.add(Lists.newArrayList(c)),
				100);

		Assert.assertEquals(12345, nbDone);

		Set<Integer> consumedSizes = spot.stream().map(Collection::size).collect(Collectors.toSet());

		// We have blocks with size 100, and a left-over
		Assert.assertEquals(ImmutableSet.of(100, 45), consumedSizes);
	}

	@Test
	public void testPartitionConsume_limit100_parallel() {
		Queue<List<Integer>> spot = new ConcurrentLinkedQueue<>();

		long nbDone =
				PepperStreamHelper.consumeByPartition(IntStream.range(0, 12345).parallel().mapToObj(Integer::valueOf),
						c -> spot.add(Lists.newArrayList(c)),
						100);

		Assert.assertEquals(12345, nbDone);

		Set<Integer> consumedSizes = spot.stream().map(Collection::size).collect(Collectors.toSet());

		// We have blocks with size 100, and a left-over
		Assert.assertEquals(ImmutableSet.of(100, 45), consumedSizes);
	}

	@Test
	public void testPartitionConsume_limit100_parallel_large() {
		int problemSize = 100 * 1000 * 1000;

		boolean[] checked;
		{
			boolean[] tmpChecked = null;
			do {
				try {
					tmpChecked = new boolean[problemSize];
				} catch (OutOfMemoryError e) {
					// In some environment, the available memory could be low
					problemSize /= 2;
				}
			} while (tmpChecked == null);
			checked = tmpChecked;
		}

		// Check that we have false by default
		Assert.assertFalse(checked[0]);

		long nbDone = PepperStreamHelper.consumeByPartition(
				IntStream.range(0, problemSize).parallel().mapToObj(Integer::valueOf),
				c -> c.forEach(i -> checked[i] = true),
				100);

		Assert.assertEquals(problemSize, nbDone);

		Assert.assertEquals(-1, Booleans.indexOf(checked, false));
	}
}
