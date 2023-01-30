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
package org.eclipse.mat.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * File utilities for things like copying icon files.
 */
public final class FileUtils {
	private static DirDeleter deleterThread;

	static {
		deleterThread = new DirDeleter();
		Runtime.getRuntime().addShutdownHook(deleterThread);
	}

	private FileUtils() {
	}

	/**
	 * Basic stream copy
	 *
	 * @param in
	 *            input stream
	 * @param out
	 *            output stream
	 * @throws IOException
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] b = new byte[256];
		int i = 0;

		while (true) {
			i = in.read(b);
			if (i == -1)
				break;
			out.write(b, 0, i);
		}

	}

	/**
	 * Create a temporary directory which should be deleted on application close.
	 *
	 * @param prefix
	 * @param parent
	 * @return the temporary directory, to be deleted on shutdown
	 * @throws IOException
	 */
	public static File createTempDirectory(String prefix, File parent) throws IOException {
		File tempFile = File.createTempFile(prefix, "", parent);
		if (!tempFile.delete())
			throw new IOException();
		if (!tempFile.mkdir())
			throw new IOException();
		deleterThread.add(tempFile);
		return tempFile;
	}

	public static String toFilename(String name, String extension) {
		return toFilename(name, "", extension);
	}

	/**
	 * Build a file name. Convert non-letters or digits to underscore.
	 *
	 * @param prefix
	 *            the prefix of the file
	 * @param suffix
	 *            the suffix
	 * @param extension
	 *            the file extension
	 * @return the combined file name
	 */
	public static String toFilename(String prefix, String suffix, String extension) {
		StringBuilder buf = new StringBuilder(prefix.length() + suffix.length() + extension.length() + 1);

		for (String s : new String[] { prefix, suffix }) {
			for (int ii = 0; ii < s.length() && ii < 20; ii++) {
				char c = s.charAt(ii);
				if (Character.isLetterOrDigit(c))
					buf.append(c);
				else
					buf.append("_");
			}
		}

		buf.append(".").append(extension);

		return buf.toString();
	}

	// //////////////////////////////////////////////////////////////
	// inner classes
	// //////////////////////////////////////////////////////////////

	private static class DirDeleter extends Thread {
		private List<File> dirList = new ArrayList<File>();

		public synchronized void add(File dir) {
			dirList.add(dir);
		}

		@Override
		public void run() {
			synchronized (this) {
				for (File dir : dirList)
					deleteDirectory(dir);
			}
		}

		private void deleteDirectory(File dir) {
			if (!dir.exists())
				return;

			File[] fileArray = dir.listFiles();

			if (fileArray != null) {
				for (int i = 0; i < fileArray.length; i++) {
					if (fileArray[i].isDirectory())
						deleteDirectory(fileArray[i]);
					else
						fileArray[i].delete();
				}
			}

			dir.delete();
		}
	}

}
