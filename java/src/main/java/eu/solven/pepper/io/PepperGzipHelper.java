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
package eu.solven.pepper.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.CharStreams;

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
	@SuppressWarnings("PMD.ExceptionAsFlowControl")
	public static void packToZip(final Path folder, final Path zipFilePath) throws IOException {
		URI folderUri = folder.toUri();

		try (OutputStream fos = Files.newOutputStream(zipFilePath); ZipOutputStream zos = new ZipOutputStream(fos)) {
			// https://stackoverflow.com/questions/15968883/how-to-zip-a-folder-itself-using-java
			Files.walk(folder).forEach(next -> {
				URI nextUri = next.toUri();

				try {
					if (Files.isDirectory(next)) {
						// https://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls
						zos.putNextEntry(new ZipEntry(folderUri.relativize(nextUri).getPath() + "/"));
						zos.closeEntry();
					} else if (Files.exists(next)) {
						LOGGER.debug("Adding {} in {}", next, zipFilePath);
						zos.putNextEntry(new ZipEntry(folderUri.relativize(nextUri).getPath()));
						Files.copy(next, zos);
						zos.closeEntry();
					}
				} catch (IOException e) {
					throw new UncheckedIOException("Issue while processing " + next + " into " + zipFilePath, e);
				}
			});
		} catch (RuntimeException e) {
			// Delete this tmp file
			if (Files.exists(zipFilePath)) {
				Files.delete(zipFilePath);
			}

			throw new IllegalStateException("Issue while writing in " + zipFilePath, e);
		}
	}

	@Deprecated
	public static void packToZip(final File folder, final File zipFilePath) throws IOException {
		packToZip(folder.toPath(), zipFilePath.toPath());
	}

	/**
	 * Gzip enable compressing a single file
	 *
	 * @param inputPath
	 * @param zipFilePath
	 * @throws IOException
	 */
	public static void packToGzip(final Path inputPath, final Path zipFilePath) throws IOException {
		try (OutputStream fos = Files.newOutputStream(zipFilePath); GZIPOutputStream zos = new GZIPOutputStream(fos)) {
			Files.copy(inputPath, zos);
		}
	}

	@Deprecated
	public static void packToGzip(final File inputPath, final File zipFilePath) throws IOException {
		packToGzip(inputPath.toPath(), zipFilePath.toPath());
	}

	// https://stackoverflow.com/questions/10633595/java-zip-how-to-unzip-folder
	@SuppressWarnings({ "PMD.AssignmentInOperand", "PMD.AvoidReassigningLoopVariables" })
	public static void unzip(InputStream is, Path targetDir) throws IOException {
		targetDir = targetDir.toAbsolutePath();
		try (ZipInputStream zipIn = new ZipInputStream(is)) {
			for (ZipEntry ze; (ze = zipIn.getNextEntry()) != null;) {
				Path resolvedPath = targetDir.resolve(ze.getName()).normalize();
				if (!resolvedPath.startsWith(targetDir)) {
					// see: https://snyk.io/research/zip-slip-vulnerability
					throw new RuntimeException("Entry with an illegal path: " + ze.getName());
				}
				if (ze.isDirectory()) {
					Files.createDirectories(resolvedPath);
				} else {
					Files.createDirectories(resolvedPath.getParent());
					Files.copy(zipIn, resolvedPath);
				}
			}
		}
	}
}
