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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.mat.parser.internal.Messages;

public class PositionInputStream extends FilterInputStream implements DataInput {
	private final byte[] readBuffer = new byte[32];
	private long position = 0L;

	public PositionInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		int res = super.read();
		if (res != -1)
			position++;
		return res;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int res = super.read(b, off, len);
		if (res != -1)
			position += res;
		return res;
	}

	@Override
	public long skip(long n) throws IOException {
		long res = super.skip(n);
		position += res;
		return res;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void mark(int readLimit) {
		throw new UnsupportedOperationException(Messages.PositionInputStream_mark);
	}

	@Override
	public void reset() {
		throw new UnsupportedOperationException(Messages.PositionInputStream_reset);
	}

	@Override
	public final int skipBytes(int n) throws IOException {
		int total = 0;
		int cur = 0;

		while ((total < n) && ((cur = (int) skip(n - total)) > 0)) {
			total += cur;
		}

		return total;
	}

	public final int skipBytes(long n) throws IOException {
		long total = 0;
		long cur = 0;

		while ((total < n) && ((cur = skip(n - total)) > 0)) {
			total += cur;
		}

		return (int) total;
	}

	@Override
	public final void readFully(byte b[]) throws IOException {
		readFully(b, 0, b.length);
	}

	@Override
	public final void readFully(byte b[], int off, int len) throws IOException {
		int n = 0;
		while (n < len) {
			int count = read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
	}

	public long position() {
		return position;
	}

	public void seek(long pos) throws IOException {
		if (in instanceof BufferedRandomAccessInputStream) {
			position = pos;
			((BufferedRandomAccessInputStream) in).seek(pos);
		} else if (in instanceof SimpleBufferedRandomAccessInputStream) {
			position = pos;
			((SimpleBufferedRandomAccessInputStream) in).seek(pos);
		} else {
			throw new UnsupportedOperationException(Messages.PositionInputStream_seek);
		}
	}

	// //////////////////////////////////////////////////////////////
	// DataInput implementations
	// //////////////////////////////////////////////////////////////

	@Override
	public final int readUnsignedByte() throws IOException {
		int ch = in.read();
		if (ch < 0)
			throw new EOFException();
		position++;
		return ch;
	}

	@Override
	public final int readInt() throws IOException {
		readFully(readBuffer, 0, 4);
		return readInt(readBuffer, 0);
	}

	@Override
	public final long readLong() throws IOException {
		readFully(readBuffer, 0, 8);
		return readLong(readBuffer, 0);
	}

	@Override
	public boolean readBoolean() throws IOException {
		int ch = in.read();
		if (ch < 0)
			throw new EOFException();
		position++;
		return (ch != 0);
	}

	@Override
	public byte readByte() throws IOException {
		int ch = in.read();
		if (ch < 0)
			throw new EOFException();
		position++;
		return (byte) (ch);
	}

	@Override
	public char readChar() throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		position += 2;
		return (char) ((ch1 << 8) + (ch2 << 0));
	}

	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public short readShort() throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		position += 2;
		return (short) ((ch1 << 8) + (ch2 << 0));
	}

	@Override
	public String readUTF() throws IOException {
		return DataInputStream.readUTF(this);
	}

	@Override
	public int readUnsignedShort() throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		position += 2;
		return (ch1 << 8) + (ch2 << 0);
	}

	// //////////////////////////////////////////////////////////////
	// additions
	// //////////////////////////////////////////////////////////////

	public int readIntArray(int[] a) throws IOException {
		int len = a.length * 4;
		byte[] b;
		if (len > readBuffer.length) {
			b = new byte[len];
		} else {
			b = readBuffer;
		}

		if (read(b, 0, len) != len)
			throw new IOException();

		for (int ii = 0; ii < a.length; ii++)
			a[ii] = readInt(b, ii * 4);

		return a.length;
	}

	private static final int readInt(byte[] b, int offset) throws IOException {
		int ch1 = b[offset] & 0xff;
		int ch2 = b[offset + 1] & 0xff;
		int ch3 = b[offset + 2] & 0xff;
		int ch4 = b[offset + 3] & 0xff;
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new EOFException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	public int readLongArray(long[] a) throws IOException {
		int len = a.length * 8;
		byte[] b;
		if (len > readBuffer.length) {
			b = new byte[len];
		} else {
			b = readBuffer;
		}

		if (read(b, 0, len) != len)
			throw new IOException();

		for (int ii = 0; ii < a.length; ii++)
			a[ii] = readLong(b, ii * 8);

		return a.length;
	}

	private static final long readLong(byte[] b, int offset) {
		return (((long) b[offset] << 56) //
				+ ((long) (b[offset + 1] & 255) << 48) //
				+ ((long) (b[offset + 2] & 255) << 40) //
				+ ((long) (b[offset + 3] & 255) << 32) //
				+ ((long) (b[offset + 4] & 255) << 24) //
				+ ((b[offset + 5] & 255) << 16) //
				+ ((b[offset + 6] & 255) << 8) //
				+ ((b[offset + 7] & 255) << 0));
	}

}
