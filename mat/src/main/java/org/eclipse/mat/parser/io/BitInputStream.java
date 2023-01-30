/**
 * The MIT License
 * Copyright (c) 2008-2010 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.parser.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;

public class BitInputStream implements Flushable, Closeable {
	public final static int DEFAULT_BUFFER_SIZE = 16 * 1024;

	private InputStream is;
	private int current;
	private byte[] buffer;
	private int fill;
	private int pos;
	private int avail;

	public BitInputStream(final InputStream is) {
		this.is = is;
		this.buffer = new byte[DEFAULT_BUFFER_SIZE];
	}

	@Override
	public void flush() {
		avail = 0;
		pos = 0;
		fill = 0;
	}

	@Override
	public void close() throws IOException {
		is.close();
		is = null;
		buffer = null;
	}

	private int read() throws IOException {
		if (avail == 0) {
			avail = is.read(buffer);
			if (avail == -1) {
				avail = 0;
				throw new EOFException();
			} else {
				pos = 0;
			}
		}

		avail--;
		return buffer[pos++] & 0xFF;
	}

	private int readFromCurrent(final int len) throws IOException {
		if (len == 0)
			return 0;

		if (fill == 0) {
			current = read();
			fill = 8;
		}

		return current >>> (fill -= len) & (1 << len) - 1;
	}

	public int readBit() throws IOException {
		return readFromCurrent(1);
	}

	public int readInt(int len) throws IOException {
		int i, x = 0;

		if (len <= fill)
			return readFromCurrent(len);

		len -= fill;
		x = readFromCurrent(fill);

		i = len >> 3;
		while (i-- != 0)
			x = x << 8 | read();

		len &= 7;

		return (x << len) | readFromCurrent(len);
	}

}
