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
package cormoran.pepper.buffer;

import java.lang.reflect.InvocationTargetException;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;

@Beta
public class CloseableIntBuffer implements AutoCloseable {
	protected static final Logger LOGGER = LoggerFactory.getLogger(CloseableIntBuffer.class);

	protected final MappedByteBuffer buffer;
	protected final IntBuffer heapBuffer;

	public CloseableIntBuffer(MappedByteBuffer buffer) {
		this.buffer = buffer;
		this.heapBuffer = null;
	}

	public CloseableIntBuffer(IntBuffer heapBuffer) {
		this.heapBuffer = heapBuffer;
		this.buffer = null;
	}

	// https://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java
	// Beware if buffer is re http://bugs.java.com/view_bug.do?bug_id=4724038
	@Override
	public void close() {
		// We clean the hook to the mapped file, else even a shutdown-hook would not remove the mapped-file
		if (this.buffer != null) {
			try {
				// sun.misc.Cleaner cleaner = ((DirectBuffer) buffer).cleaner();
				// cleaner.clean();
				Class<?> directBufferClass = Class.forName("sun.nio.ch.DirectBuffer");
				Object cleaner = directBufferClass.getMethod("cleaner").invoke(buffer);

				Class<?> cleanerClass = Class.forName("sun.misc.Cleaner");
				cleanerClass.getMethod("clean").invoke(cleaner);
			} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException | SecurityException e) {
				LOGGER.trace("Ouch", e);
				// JDK9?
				return;
			}

		}
	}

	public IntBuffer asIntBuffer() {
		if (heapBuffer != null) {
			return heapBuffer;
		} else {
			return buffer.asIntBuffer();
		}
	}

}
