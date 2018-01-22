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
package cormoran.pepper.avro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.avro.generic.GenericRecord;

import com.google.common.annotations.Beta;

/**
 * This interface enables write and reading of GenericRecord from/to bytes. We work with {@link URI} and not
 * {@link InputStream} as some implementation may not produce/require an actual stream of b ytes like parquet.
 * 
 * @author Benoit Lacelle
 *
 */
@Beta
public interface IAvroStreamFactory {

	/**
	 * @deprecated We prefer not to rely on java.nio.Path as it requires a {@link FileSystem} compatible with the Path
	 *             scheme. it would typically not work for swebhdfs scheme
	 */
	@Deprecated
	default Stream<GenericRecord> toStream(Path javaPath) throws IOException {
		return toStream(javaPath.toUri());
	}

	@Deprecated
	default Stream<GenericRecord> toStream(URI uri) throws IOException {
		return stream(uri);
	}

	Stream<GenericRecord> stream(URI uri) throws IOException;

	/**
	 * @deprecated We prefer not to rely on java.nio.Path as it requires a {@link FileSystem} compatible with the Path
	 *             scheme. it would typically not work for swebhdfs scheme
	 */
	@Deprecated
	default long writeToPath(Path javaPath, Stream<? extends GenericRecord> rowsToWrite) throws IOException {
		return writeToPath(javaPath.toUri(), rowsToWrite);
	}

	@Deprecated
	default long writeToPath(URI uri, Stream<? extends GenericRecord> rowsToWrite) throws IOException {
		return serialize(uri, rowsToWrite);
	}

	long serialize(URI uri, Stream<? extends GenericRecord> rowsToWrite) throws IOException;

	long serialize(OutputStream outputStream, Stream<? extends GenericRecord> rowsToWrite) throws IOException;
}
