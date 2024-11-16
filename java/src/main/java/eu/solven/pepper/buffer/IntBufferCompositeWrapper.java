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
package eu.solven.pepper.buffer;

import java.nio.IntBuffer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Wraps at most 2 {@link IntBuffer}
 *
 * @author Benoit Lacelle
 *
 */
public class IntBufferCompositeWrapper implements IIntBufferWrapper {

	private final IntBuffer asIntBuffer;
	private final IntBuffer second;

	@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
	public IntBufferCompositeWrapper(IntBuffer... buffers) {
		if (buffers.length == 0 || buffers.length > 2) {
			throw new UnsupportedOperationException("TODO");
		} else if (buffers.length == 1) {
			asIntBuffer = buffers[0];
			second = IntBuffer.allocate(0);
		} else if (buffers.length == 2) {
			asIntBuffer = buffers[0];
			second = buffers[1];
		} else {
			throw new UnsupportedOperationException("TODO");
		}
	}

	@Override
	public void put(int index, int value) {
		int capacity = asIntBuffer.capacity();
		if (index > capacity) {
			second.put(index - capacity, value);
		} else {
			asIntBuffer.put(index, value);
		}
	}

	@Override
	public int capacity() {
		return asIntBuffer.capacity() + second.capacity();
	}

	@Override
	public IntBuffer getFirstRawBuffer() {
		return asIntBuffer;
	}

	@Override
	public int get(int index) {
		int capacity = asIntBuffer.capacity();
		if (index > capacity) {
			return asIntBuffer.get(index - capacity);
		} else {
			return asIntBuffer.get(index);
		}
	}

}
