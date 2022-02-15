package eu.solven.pepper.primitives;

import org.junit.Assert;
import org.junit.Test;

public class TestConstantBitsIntArray {
	@Test
	public void testEmty() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[0]);

		Assert.assertEquals(0, compressed.size());
	}

	@Test
	public void testHolds0() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 0 });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(0, compressed.getInt(0));
	}

	@Test
	public void testHolds1() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 1 });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(1, compressed.getInt(0));
	}

	@Test
	public void testHolds2() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 2 });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(2, compressed.getInt(0));
	}

	@Test
	public void testHoldsMinus1() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { -1 });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(-1, compressed.getInt(0));
	}

	@Test
	public void testHoldsMaxValue() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { Integer.MAX_VALUE });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(Integer.MAX_VALUE, compressed.getInt(0));
	}

	@Test
	public void testHoldsMinValue() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { Integer.MIN_VALUE });

		Assert.assertEquals(1, compressed.size());
		Assert.assertEquals(Integer.MIN_VALUE, compressed.getInt(0));
	}

	@Test
	public void testHolds0_2() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 0, 2 });

		Assert.assertEquals(2, compressed.size());
		Assert.assertEquals(0, compressed.getInt(0));
		Assert.assertEquals(2, compressed.getInt(1));
	}

	@Test
	public void testHolds0_1_2() {
		ConstantBitsIntArray compressed = ConstantBitsIntArray.fromIntArray(new int[] { 0, 1, 2 });

		Assert.assertEquals(3, compressed.size());
		Assert.assertEquals(0, compressed.getInt(0));
		Assert.assertEquals(1, compressed.getInt(1));
		Assert.assertEquals(2, compressed.getInt(2));
	}
}
