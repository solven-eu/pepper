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
package cormoran.pepper.primitives;

import java.util.Arrays;

import org.roaringbitmap.RoaringBitmap;

import com.google.common.primitives.Ints;

import it.unimi.dsi.fastutil.ints.AbstractIntList;

/**
 * A compressed IntList implementation. It provides random read-access. It is compressed by bit-packing
 * 
 * TODO: Add append-behavior
 * 
 * @author Benoit Lacelle
 *
 */
public class RunningCompressedIntArray extends AbstractIntList implements Cloneable, java.io.Serializable {
	private static final long serialVersionUID = 2801769711578854510L;

	protected final int[] nbConstant;
	protected final int[] constantBits;
	protected final int[] constantMasks;
	protected final RoaringBitmap bits;

	public RunningCompressedIntArray(int[] nbConstant, int[] constantBits, int[] constantMasks, RoaringBitmap bits) {
		this.nbConstant = nbConstant;
		this.constantBits = constantBits;
		this.constantMasks = constantMasks;
		this.bits = bits;
	}

	@Override
	public int getInt(int index) {
		int block = 0;
		int previousSize = 0;

		long bitShift = 0;

		int indexInBlock = index;
		while (block < Integer.SIZE) {
			int blockSize = nbConstant[block];
			if (index >= previousSize + blockSize) {
				previousSize += blockSize;
				indexInBlock -= blockSize;

				bitShift += (blockSize * 1L) * block;

				block++;
			} else {
				break;
			}
		}

		if (nbConstant[block] == 0) {
			throw new ArrayIndexOutOfBoundsException("index=" + index + " while size=" + size());
		}

		bitShift += indexInBlock * block;

		// Initialize the value given input mask
		int value = constantMasks[block];

		// Which bits have to be set out of the mask
		int differentBits = ~constantBits[block];

		for (int bitIndex = 0; bitIndex < Integer.SIZE; bitIndex++) {
			if ((differentBits & Integer.rotateLeft(1, bitIndex)) != 0) {

				if (bits.contains(Ints.checkedCast(bitShift))) {
					value |= Integer.rotateLeft(1, bitIndex);
				}

				bitShift++;
			}
		}

		return value;
	}

	@Override
	public int size() {
		return Arrays.stream(nbConstant).sum();
	}

}
