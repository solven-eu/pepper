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
package cormoran.pepper.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;
import com.google.common.io.Files;

/**
 * Various utilities for GZip
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperGzipHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperGzipHelper.class);

	private static final int GZIP_MAGIC_SHIFT = 8;

	protected PepperGzipHelper() {
		// hidden
	}

	/**
	 * 
	 * @param bytes
	 * @return true if this byte array seems to hold a gzip compressed message
	 */
	public static boolean isGZIPStream(byte[] bytes) {
		return bytes != null && bytes.length >= 2
				&& bytes[0] == (byte) GZIPInputStream.GZIP_MAGIC
				&& bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >>> GZIP_MAGIC_SHIFT);
	}

	/**
	 * This is able to handle byte arrays, would it hold the compressed String, or just plain data
	 * 
	 * @param bytes
	 * @return the deflated String
	 * @throws IOException
	 */
	public static String toStringOptCompressed(byte[] bytes) throws IOException {
		if (isGZIPStream(bytes)) {
			return toStringCompressed(bytes);
		} else {
			return new String(bytes, StandardCharsets.UTF_8);
		}
	}

	public static String toStringCompressed(byte[] bytes) throws IOException {
		return inflate(bytes);
	}

	/**
	 * @deprecated Prefer .deflate
	 */
	@Deprecated
	public static byte[] compress(String someString) throws IOException {
		return deflate(someString);
	}

	public static byte[] deflate(String someString) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (OutputStreamWriter osw = new OutputStreamWriter(new GZIPOutputStream(baos), StandardCharsets.UTF_8)) {
			osw.write(someString);
		}

		return baos.toByteArray();
	}

	public static String inflate(byte[] inflated) throws IOException {
		return inflate(inflated, StandardCharsets.UTF_8);
	}

	public static String inflate(byte[] inflated, Charset charset) throws IOException {
		try (GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(inflated));
				InputStreamReader osw = new InputStreamReader(gzipInputStream, charset)) {
			return CharStreams.toString(osw);
		}
	}

	/**
	 * 
	 * @param folder
	 *            a file-system folder were to read files and folders
	 * @param zipFilePath
	 *            a file-system path where to create a new archive
	 * @throws IOException
	 */
	public static void packToZip(final File folder, final File zipFilePath) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(zipFilePath); ZipOutputStream zos = new ZipOutputStream(fos)) {
			// https://stackoverflow.com/questions/15968883/how-to-zip-a-folder-itself-using-java
			Iterator<File> iterator = Files.fileTreeTraverser().preOrderTraversal(folder).iterator();

			while (iterator.hasNext()) {
				File next = iterator.next();

				if (next.isDirectory()) {
					// https://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls
					zos.putNextEntry(new ZipEntry(folder.toURI().relativize(next.toURI()).getPath() + "/"));
					zos.closeEntry();
				} else if (next.isFile()) {
					LOGGER.debug("Adding {} in {}", next, zipFilePath);
					zos.putNextEntry(new ZipEntry(folder.toURI().relativize(next.toURI()).getPath()));
					Files.copy(next, zos);
					zos.closeEntry();
				}
			}
		} catch (IOException e) {
			// Delete this tmp file
			if (zipFilePath.isFile() && !zipFilePath.delete()) {
				LOGGER.debug("For some reason, we failed deleting {}", zipFilePath);
			}

			throw new IOException("Issue while writing in " + zipFilePath, e);
		}
	}

	/**
	 * Gzip enable compressing a single file
	 * 
	 * @param inputPath
	 * @param zipFilePath
	 * @throws IOException
	 */
	public static void packToGzip(final File inputPath, final File zipFilePath) throws IOException {
		try (FileOutputStream fos = new FileOutputStream(zipFilePath);
				GZIPOutputStream zos = new GZIPOutputStream(fos)) {
			Files.copy(inputPath, zos);
		}
	}
}
