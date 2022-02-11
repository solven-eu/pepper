package eu.solven.pepper.primitives;

import java.util.stream.IntStream;

import com.google.common.primitives.Ints;

/**
 * This will detect bits not changing once in input int[], in order to store the constant mask and bit-pack the other
 * bits
 * 
 * @author Benoit Lacelle
 *
 */
@SuppressWarnings("checkstyle:MagicNumber")
public class ConstantBitsIntArray implements IReadableIntArray {
	// 1 means given bit takes different values through the array: rely on varyingBits
	// 0 means given bit takes same value through the array: rely on constantBitMask
	protected final int varyingBitMask;

	// An int where relevant values are at bit in which varyingBitMask is 0
	protected final int constantBitMask;

	protected final long length;
	protected final long[] varyingBits;

	protected transient int[] varyingIndexes;

	protected ConstantBitsIntArray(int... input) {
		length = input.length;
		int intLength = input.length;

		// We will put 1 in bits having a varying value through input array
		int mutatingVaryingBitMask = 0;

		if (input.length == 0) {
			varyingBitMask = 0;
			constantBitMask = 0;
			varyingBits = new long[0];
		} else {
			int firstInput = input[0];

			for (int i = 1; i < length; i++) {
				// https://stackoverflow.com/questions/1991380/what-does-the-operator-do-in-java
				int differentBits = firstInput ^ input[i];

				// Register bits having at least one difference
				mutatingVaryingBitMask |= differentBits;
			}

			// TODO Should we set to 0 varying bits, so that we later have just to write bits set ot one while reading
			// back from varyingBits
			constantBitMask = firstInput;
			varyingBitMask = mutatingVaryingBitMask;

			int nbVarying = Integer.bitCount(varyingBitMask);

			{
				varyingIndexes = new int[nbVarying];

				int registeredIndex = 0;
				for (int j = 0; j < 32; j++) {
					if (bitIndexIsVarying(j)) {
						varyingIndexes[registeredIndex++] = j;
					}
				}
			}

			// If nbVarying == 1, length==1 -> nbLongs==1 && length==64 -> nbLongs==1
			int nbLongs = indexVaryingToIndexLong(intLength, nbVarying) + 1;
			varyingBits = new long[nbLongs];

			for (int i = 0; i < intLength; i++) {
				int currentInput = input[i];
				int longIndex = indexVaryingToIndexLong(i, nbVarying);
				int longBitIndex = (i * nbVarying) % 64;

				for (int j : varyingIndexes) {
					if (bitIndexIsVarying(j)) {
						if (bitIsSet(currentInput, j)) {
							varyingBits[longIndex] = computeBit(varyingBits[longIndex], longBitIndex);
						}

						longBitIndex++;
						if (longBitIndex % 64 == 0) {
							// We have cycled to next long bucket
							longBitIndex = 0;
							longIndex++;
						}
					}
				}
			}
		}

	}

	@Override
	public int getInt(int index) {
		int output = constantBitMask;

		int nbVarying = Integer.bitCount(varyingBitMask);

		int longIndex = indexVaryingToIndexLong(index, nbVarying);
		int longBitIndex = (index * nbVarying) % 64;

		int nbConsidered = 0;
		for (int i : varyingIndexes) {
			// https://stackoverflow.com/questions/9354860/how-to-get-the-value-of-a-bit-at-a-certain-position-from-a-byte/9354899
			if (bitIndexIsVarying(i)) {
				if (bitIsSet(varyingBits[longIndex], longBitIndex)) {
					output = computeBit(output, i);
				} else {
					output = computeBitToZero(output, i);
				}

				longBitIndex++;
				if (longBitIndex % 64 == 0) {
					// We have cycled to next long bucket
					longBitIndex = 0;
					longIndex++;
				}

				nbConsidered++;
				if (nbVarying == nbConsidered) {
					break;
				}
			}
		}

		return output;
	}

	private int computeBit(int someInteger, int longBitIndex) {
		return someInteger | (1 << longBitIndex);
	}

	private int computeBitToZero(int someInteger, int longBitIndex) {
		return someInteger & ~(1 << longBitIndex);
	}

	private long computeBit(long someLong, int longBitIndex) {
		return someLong | (1 << longBitIndex);
	}

	private int indexVaryingToIndexLong(int index, int nbVarying) {
		return Ints.checkedCast((index * nbVarying) / 64);
	}

	private boolean bitIndexIsVarying(int bitIndex) {
		return bitIsSet(varyingBitMask, bitIndex);
	}

	private boolean bitIsSet(long someLong, int bitIndex) {
		return 1 == ((someLong >> bitIndex) & 1);
	}

	private boolean bitIsSet(int someInteger, int bitIndex) {
		return 1 == ((someInteger >> bitIndex) & 1);
	}

	public static ConstantBitsIntArray fromIntArray(int... input) {
		return new ConstantBitsIntArray(input);
	}

	public static ConstantBitsIntArray fromIntStream(IntStream range) {
		// TODO Do not materialize the intermediate int[]
		return fromIntArray(range.toArray());
	}

	/**
	 * 
	 * @return the size of this array, saturated to Integer.MAX_VALUE
	 */
	public int size() {
		return Ints.saturatedCast(length);
	}

	public long longSize() {
		return length;
	}

	public int[] toIntArray() {
		int[] intArray = new int[size()];

		for (int i = 0; i < intArray.length; i++) {
			intArray[i] = getInt(i);
		}
		return intArray;
	}

	public boolean isEmpty() {
		return size() == 0;
	}

}
