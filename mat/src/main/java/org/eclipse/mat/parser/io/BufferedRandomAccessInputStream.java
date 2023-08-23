/*******************************************************************************
 * Copyright (c) 2008, 2023 SAP AG, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - additional debug information
 *    Netflix (Jason Koch) - refactors for increased performance and concurrency
 *******************************************************************************/
package org.eclipse.mat.parser.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;

import org.eclipse.mat.collect.HashMapLongObject;

public class BufferedRandomAccessInputStream extends InputStream {
	RandomAccessFile raf;

	private class Page {
		long real_pos_start;
		byte[] buffer;
		int buf_end;

		public Page() {
			buffer = new byte[bufsize];
		}
	}

	int bufsize;
	long fileLength;
	long real_pos;
	long reported_pos;

	HashMapLongObject<SoftReference<Page>> pages = new HashMapLongObject<SoftReference<Page>>();

	Page current;

	public BufferedRandomAccessInputStream(RandomAccessFile in) throws IOException {
		this(in, 1024);
	}

	public BufferedRandomAccessInputStream(RandomAccessFile in, int bufsize) throws IOException {
		this.bufsize = bufsize;
		this.raf = in;
		this.fileLength = in.length();
	}

	@Override
	public final int read() throws IOException {
		if (reported_pos == fileLength)
			return -1;

		if (current == null || (reported_pos - current.real_pos_start) >= current.buf_end)
			current = getPage(reported_pos);

		return current.buffer[((int) (reported_pos++ - current.real_pos_start))] & 0xff;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		if (reported_pos == fileLength)
			return -1;

		int copied = 0;

		while (copied < len) {
			if (reported_pos == fileLength)
				return copied;

			if (current == null || (reported_pos - current.real_pos_start) >= current.buf_end)
				current = getPage(reported_pos);

			int buf_pos = (int) (reported_pos - current.real_pos_start);
			int length = Math.min(len - copied, current.buf_end - buf_pos);
			System.arraycopy(current.buffer, buf_pos, b, off + copied, length);

			reported_pos += length;
			copied += length;
		}

		return copied;
	}

	private Page getPage(long pos) throws IOException {
		long key = pos / bufsize;

		SoftReference<Page> r = pages.get(key);
		Page p;
		if (r == null) {
			p = null;
		} else {
			p = r.get();
		}

		if (p != null)
			return p;

		long page_start = key * bufsize;

		if (page_start != real_pos) {
			raf.seek(page_start);
			real_pos = page_start;
		}

		p = new Page();

		int n = raf.read(p.buffer);
		if (n >= 0) {
			p.real_pos_start = real_pos;
			p.buf_end = n;
			real_pos += n;
		}

		pages.put(key, new SoftReference<Page>(p));

		return p;
	}

	@Override
	public boolean markSupported() {
		return false;
	}

	@Override
	public void close() throws IOException {
		raf.close();
	}

	/**
	 * @throws IOException
	 */
	public void seek(long pos) throws IOException {
		reported_pos = pos;
		current = null;
	}

	public long getFilePointer() {
		return reported_pos;
	}
}
