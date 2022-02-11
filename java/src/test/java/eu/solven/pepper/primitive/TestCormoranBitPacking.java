package eu.solven.pepper.primitive;

import java.util.function.LongToIntFunction;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;

import eu.solven.pepper.primitive.CormoranBitPacking;

public class TestCormoranBitPacking {
	SetMultimap<Integer, Integer> pairsToTry = ImmutableSetMultimap.<Integer, Integer>builder()
			.put(0, 0)
			.put(0, 1)
			.put(1, 0)
			.put(1, 1)
			.put(0, Integer.MAX_VALUE)
			.put(Integer.MAX_VALUE, 0)
			.put(Integer.MAX_VALUE, Integer.MAX_VALUE)
			.put(0, Integer.MIN_VALUE)
			.put(Integer.MIN_VALUE, 0)
			.put(Integer.MIN_VALUE, Integer.MIN_VALUE)
			.build();

	@Test
	public void testStandardPacking() {
		pairsToTry.forEach(this::checkStandard);
	}

	@Test
	public void testOrderedPacking() {
		pairsToTry.forEach(this::checkOrdered);
	}

	// Check we have 2 positive integers even if the long if very large
	@Test
	public void testOrderedPacking_IsOrdered() {
		// Long.MAX_VALUE -> It is positive so right should be positive
		// '>> 1' is equivalent to '/2' -> it is low enough even left is positive
		int left = CormoranBitPacking.unpackOrderedLeft(Long.MAX_VALUE >> 1);
		int right = CormoranBitPacking.unpackOrderedRight(Long.MAX_VALUE >> 1);

		Assert.assertTrue(left > 0);
		Assert.assertTrue(right > 0);
	}

	@Test
	public void testOrderedPacking_FirstIntOverflow() {
		int left = CormoranBitPacking.unpackOrderedLeft(Integer.MAX_VALUE + 1L);
		int right = CormoranBitPacking.unpackOrderedRight(Integer.MAX_VALUE + 1L);

		Assert.assertEquals(1, left);
		Assert.assertEquals(0, right);
	}

	private void checkStandard(int left, int right) {
		check(left,
				right,
				CormoranBitPacking::packInts,
				CormoranBitPacking::unpackLeft,
				CormoranBitPacking::unpackRight);
	}

	private void checkOrdered(int left, int right) {
		check(left,
				right,
				CormoranBitPacking::packOrderedInts,
				CormoranBitPacking::unpackOrderedLeft,
				CormoranBitPacking::unpackOrderedRight);
	}

	interface Packing {
		long fromIntegers(int left, int right);
	}

	private void check(int left,
			int right,
			Packing packToLong,
			LongToIntFunction unpackFirst,
			LongToIntFunction unpackSecond) {
		long pack = packToLong.fromIntegers(left, right);
		long leftFromPack = unpackFirst.applyAsInt(pack);
		long rightFromPack = unpackSecond.applyAsInt(pack);

		Assert.assertEquals(left, leftFromPack);
		Assert.assertEquals(right, rightFromPack);
	}
}
