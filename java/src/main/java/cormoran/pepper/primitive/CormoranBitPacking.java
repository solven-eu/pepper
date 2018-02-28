package cormoran.pepper.primitive;

/**
 * Helper methods related to bit-packing, typically to pack 2 integers into a single long
 * 
 * @author Benoit Lacelle
 *
 */
public class CormoranBitPacking {
	protected CormoranBitPacking() {
		// hidden
	}

	/**
	 * Standard way of packing 2 integers in a long
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static long packInts(int left, int right) {
		// https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
		return (((long) left) << 32) | (right & 0xffffffffL);
	}

	/**
	 * Standard way of unpacking the first integer from long
	 * 
	 * @param pack
	 * @return
	 */
	public static int unpackLeft(long pack) {
		// https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
		return (int) (pack >> 32);
	}

	/**
	 * Standard way of unpacking the second integer from long
	 * 
	 * @param pack
	 * @return
	 */
	public static int unpackRight(long pack) {
		// https://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long
		return (int) pack;
	}

	/**
	 * Ordered way of packing 2 integers in a long, i.e. we produce two positive integers if the packed long is positive
	 * and below Long.MAX_VALUE / 2
	 * 
	 * @param left
	 * @param right
	 * @return
	 */
	public static long packOrderedInts(int left, int right) {
		long packed1 = (long) left << 32;
		long packed2 = Integer.rotateLeft(right, 1) & 0xFFFFFFFFL;
		return Long.rotateRight(packed1 | packed2, 1);
	}

	/**
	 * Ordered way of unpacking the first integer from long, i.e. we produce two positive integers if the packed long is
	 * positive and below Long.MAX_VALUE / 2
	 * 
	 * @param pack
	 * @return
	 */
	public static int unpackOrderedLeft(long pack) {
		// Move the higher as lower bit: if packed > 0, we are then sure to have 0 as first bit
		return (int) (Long.rotateLeft(pack, 1) >>> 32);
	}

	/**
	 * Ordered way of unpacking the second integer from long, i.e. we produce two positive integers if the packed long
	 * is positive and below Long.MAX_VALUE / 2
	 * 
	 * @param pack
	 * @return
	 */
	public static int unpackOrderedRight(long pack) {
		// Move the higher as lower bit: if packed > 0, we are then sure to have 0 as first bit
		// THen, this 0 bit is put back as last bit: the integer is guaranteed to be positive
		return Integer.rotateRight((int) (Long.rotateLeft(pack, 1) & 0xFFFFFFFFL), 1);
	}
}
