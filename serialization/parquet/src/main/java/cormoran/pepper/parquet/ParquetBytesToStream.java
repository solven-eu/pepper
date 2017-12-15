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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.avro.generic.GenericRecord;

import cormoran.pepper.avro.IBinaryToAvroStream;
import cormoran.pepper.core.io.PepperFileHelper;

/**
 * Automatic transformation of a Parquet InputStream to a Stream of objects. It requires to write the file first on the
 * file-system
 * 
 * @author Benoit Lacelle
 *
 */
public class ParquetBytesToStream implements IBinaryToAvroStream {

	protected final AtomicReference<Path> persisted = new AtomicReference<>();

	protected void persist(InputStream inputStream) throws IOException {
		if (persisted.get() != null) {
			throw new RuntimeException("Already persisted on " + persisted.get());
		}

		// TODO: We may not want to delete on exit in order to keep a local cache. But one way rather move to file to a
		// proper place
		boolean deleteOnExit = true;
		// Write the InputStream to FileSystem as Parquet expect a SeekableInputStream as metadata are at the end
		// https://github.com/apache/parquet-mr/blob/master/parquet-common/src/main/java/org/apache/parquet/io/SeekableInputStream.java
		Path tmp = PepperFileHelper.createTempPath(getClass().getSimpleName(), ".tmp", deleteOnExit);

		// Copy InputStream to tmp file
		Files.copy(inputStream, tmp, StandardCopyOption.REPLACE_EXISTING);

		persisted.set(tmp);
	}

	@Override
	public Stream<GenericRecord> stream(InputStream inputStream) throws IOException {
		persist(inputStream);

		return new ParquetStreamFactory().toStream(persisted.get());
	}

	public Stream<GenericRecord> stream(Path path) throws IOException {
		return new ParquetStreamFactory().toStream(path);
	}
}
