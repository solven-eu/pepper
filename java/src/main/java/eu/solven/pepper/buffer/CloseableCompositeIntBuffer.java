/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
import java.nio.MappedByteBuffer;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;

/**
 * {@link AutoCloseable} onto {@link IIntBufferWrapper}
 *
 * @author Benoit Lacelle
 *
 */
@Beta
public class CloseableCompositeIntBuffer implements AutoCloseable {
	protected static final Logger LOGGER = LoggerFactory.getLogger(CloseableCompositeIntBuffer.class);

	protected final MappedByteBuffer[] buffers;

	protected final IIntBufferWrapper heapBuffer;

	@SuppressWarnings("PMD.ArrayIsStoredDirectly")
	public CloseableCompositeIntBuffer(MappedByteBuffer... buffers) {
		this.buffers = buffers;

		this.heapBuffer = new IntBufferCompositeWrapper(
				Stream.of(buffers).map(MappedByteBuffer::asIntBuffer).toArray(IntBuffer[]::new));
	}

	@SuppressWarnings("PMD.NullAssignment")
	public CloseableCompositeIntBuffer(IntBuffer wrap) {
		this.buffers = null;
		this.heapBuffer = new IntBufferWrapper(wrap);
	}

	@Override
	public void close() {
		if (this.buffers != null) {
			for (MappedByteBuffer buffer : this.buffers) {
				CloseableIntBuffer.closeBuffer(buffer);
			}
		}
	}

	public IIntBufferWrapper asIntBuffer() {
		return heapBuffer;
	}

}
