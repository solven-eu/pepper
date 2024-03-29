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
package org.eclipse.mat.parser.internal.snapshot;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.mat.collect.HashMapIntLong;
import org.eclipse.mat.parser.index.IIndexReader;
import org.eclipse.mat.parser.internal.Messages;
import org.eclipse.mat.parser.model.XSnapshotInfo;

public class RetainedSizeCache implements IIndexReader {
	private String filename;
	private HashMapIntLong id2size;
	private boolean isDirty = false;

	/**
	 * File is expected to exist, and is read in the new format.
	 *
	 * @param f
	 */
	public RetainedSizeCache(File f) {
		this.filename = f.getAbsolutePath();
		doRead(f, false);
	}

	/**
	 * Reads file i2sv2.index in new format, or file i2s.index in the old format, or creates an empty map.
	 *
	 * @param snapshotInfo
	 */
	public RetainedSizeCache(XSnapshotInfo snapshotInfo) {
		this.filename = snapshotInfo.getPrefix() + "i2sv2.index";
		readId2Size(snapshotInfo.getPrefix());
	}

	public long get(int key) {
		try {
			return id2size.get(key);
		} catch (NoSuchElementException e) {
			// $JL-EXC$
			return 0;
		}
	}

	public void put(int key, long value) {
		id2size.put(key, value);
		isDirty = true;
	}

	@Override
	public void close() {
		if (!isDirty)
			return;

		File file = new File(filename);
		try (DataOutputStream out = new DataOutputStream(new FileOutputStream(file))) {
			for (int key : id2size.getAllKeys()) {
				out.writeInt(key);
				out.writeLong(id2size.get(key));
			}

			isDirty = false;
		} catch (IOException e) {
			Logger.getLogger(RetainedSizeCache.class.getName())
					.log(Level.WARNING, Messages.RetainedSizeCache_Warning_IgnoreError, e);
		}
	}

	private void doRead(File file, boolean readOldFormat) {
		DataInputStream in = null;
		boolean delete = false;

		try {
			id2size = new HashMapIntLong((int) file.length() / 8);

			in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

			while (in.available() > 0) {
				int key = in.readInt();
				long value = in.readLong();
				if (value < 0 && readOldFormat)
					value = -(value - (Long.MIN_VALUE + 1));
				id2size.put(key, value);
			}
		} catch (IOException e) {
			Logger.getLogger(RetainedSizeCache.class.getName())
					.log(Level.WARNING, Messages.RetainedSizeCache_ErrorReadingRetainedSizes, e);

			// might have read corrupt data
			id2size.clear();
			delete = true;
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ignore) {
				// $JL-EXC$
			}
			try {
				if (delete) {
					file.delete();
				}
			} catch (RuntimeException ignore) {
				// $JL-EXC$
			}
		}
	}

	private void readId2Size(String prefix) {
		File file = new File(filename);
		if (file.exists()) {
			doRead(file, false);
		} else {
			File legacyFile = new File(prefix + "i2s.index");
			if (legacyFile.exists()) {
				doRead(legacyFile, true);
			} else {
				id2size = new HashMapIntLong();
			}
		}
	}

	@Override
	public int size() {
		return id2size.size();
	}

	@Override
	public void unload() throws IOException {
		close();
	}

	@Override
	public void delete() {
		close();

		File file = new File(filename);
		file.delete();
	}

}
