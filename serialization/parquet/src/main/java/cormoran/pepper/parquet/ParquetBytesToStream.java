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
package cormoran.pepper.parquet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cormoran.pepper.avro.IBinaryToAvroStream;
import cormoran.pepper.io.PepperFileHelper;

/**
 * Automatic transformation of a Parquet InputStream to a Stream of objects. It requires to write the file first on the
 * file-system
 * 
 * @author Benoit Lacelle
 *
 */
public class ParquetBytesToStream implements IBinaryToAvroStream {
	protected static final Logger LOGGER = LoggerFactory.getLogger(ParquetBytesToStream.class);

	protected final boolean deleteOnExit;

	/**
	 * By default, the InputStream is flushed to a temporary file which will be deleted with the JVM
	 * 
	 * @see File.deleteOnExit
	 */
	public ParquetBytesToStream() {
		this(true);
	}

	protected ParquetBytesToStream(boolean deleteOnExit) {
		this.deleteOnExit = deleteOnExit;
	}

	protected URI persist(InputStream inputStream) throws IOException {
		// Write the InputStream to FileSystem as Parquet expect a SeekableInputStream as metadata are at the end
		// https://github.com/apache/parquet-mr/blob/master/parquet-common/src/main/java/org/apache/parquet/io/SeekableInputStream.java
		Path tmp = pathWhereToDumpBytes();

		LOGGER.debug("We persist a parquet InputStream into {}", tmp);

		// Copy InputStream to tmp file
		Files.copy(inputStream, tmp, StandardCopyOption.REPLACE_EXISTING);

		return tmp.toUri();
	}

	protected Path pathWhereToDumpBytes() throws IOException {
		// One may not want to delete on exit in order to keep a local cache. But one way rather move to file to a
		// persisted cache on FS
		return PepperFileHelper.createTempPath(getClass().getSimpleName(), ".parquet", true);
	}

	@Override
	public Stream<GenericRecord> stream(InputStream inputStream) throws IOException {
		// We write the bytes in FS
		URI uri = persist(inputStream);

		// Parquet require random-access to bytes
		return new ParquetStreamFactory().stream(uri);
	}
}
