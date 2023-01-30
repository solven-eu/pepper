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
package org.eclipse.mat.parser.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.collect.ArrayInt;
import org.eclipse.mat.collect.HashMapIntObject;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IThreadStack;
import org.eclipse.mat.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* package */class ThreadStackHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(ThreadStackHelper.class);

	/* package */static HashMapIntObject<IThreadStack> loadThreadsData(ISnapshot snapshot) throws SnapshotException {
		String fileName = snapshot.getSnapshotInfo().getPrefix() + "threads";
		File f = new File(fileName);
		if (!f.exists())
			return null;

		HashMapIntObject<IThreadStack> threadId2stack = new HashMapIntObject<IThreadStack>();

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
			String line = in.readLine();

			while (line != null) {
				line = line.trim();
				if (line.startsWith("Thread")) {
					long threadAddress = readThreadAddres(line);
					List<String> lines = new ArrayList<String>();
					HashMapIntObject<ArrayInt> line2locals = new HashMapIntObject<ArrayInt>();

					line = in.readLine();
					while (line != null && !line.equals("")) {
						lines.add(line.trim());
						line = in.readLine();
					}

					line = in.readLine();
					if (line != null && line.trim().startsWith("locals")) {
						line = in.readLine();
						while (line != null && !line.equals("")) {
							int lineNr = readLineNumber(line);
							if (lineNr >= 0) {
								int objectId = readLocalId(line, snapshot);
								ArrayInt arr = line2locals.get(lineNr);
								if (arr == null) {
									arr = new ArrayInt();
									line2locals.put(lineNr, arr);
								}
								arr.add(objectId);
							}
							line = in.readLine();
						}
					}

					if (threadAddress != -1) {
						try {
							int threadId = snapshot.mapAddressToId(threadAddress);
							IThreadStack stack = new ThreadStackImpl(threadId, buildFrames(lines, line2locals));
							threadId2stack.put(threadId, stack);
						} catch (SnapshotException se) {
							// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=520908
							LOGGER.warn(MessageUtil.format("Invalid thread {0}: {1}",
									"0x" + Long.toHexString(threadAddress),
									se.getLocalizedMessage()));
						}
					}
				}

				if (line != null)
					line = in.readLine();
				else
					break;
			}
		} catch (IOException e) {
			throw new SnapshotException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception ignore) {
					// $JL-EXC$
				}
			}
		}

		return threadId2stack;

	}

	private static long readThreadAddres(String line) {
		int start = line.indexOf("0x");
		if (start < 0)
			return -1;
		return (new BigInteger(line.substring(start + 2), 16)).longValue();
	}

	private static int readLocalId(String line, ISnapshot snapshot) throws SnapshotException {
		int start = line.indexOf("0x");
		int end = line.indexOf(',', start);
		long address = (new BigInteger(line.substring(start + 2, end), 16)).longValue();
		return snapshot.mapAddressToId(address);
	}

	private static int readLineNumber(String line) {
		int start = line.indexOf("line=");
		return Integer.valueOf(line.substring(start + 5));
	}

	private static StackFrameImpl[] buildFrames(List<String> lines, HashMapIntObject<ArrayInt> line2locals) {
		int sz = lines.size();

		StackFrameImpl[] frames = new StackFrameImpl[sz];
		for (int i = 0; i < sz; i++) {
			int[] localsIds = null;
			ArrayInt locals = line2locals.get(i);
			if (locals != null && locals.size() > 0) {
				localsIds = locals.toArray();
			}
			frames[i] = new StackFrameImpl(lines.get(i), localsIds);
		}

		return frames;

	}

}
