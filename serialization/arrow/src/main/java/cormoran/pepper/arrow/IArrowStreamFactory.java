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
package cormoran.pepper.arrow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.arrow.vector.types.pojo.Schema;

import com.google.common.annotations.Beta;

import cormoran.pepper.io.PepperURLHelper;

/**
 * This interface enables write and reading of GenericRecord from/to bytes. We work with {@link URI} and not
 * {@link InputStream} as some implementation may not produce/require an actual stream of b ytes like parquet.
 * 
 * @author Benoit Lacelle
 *
 */
@Beta
public interface IArrowStreamFactory {

	Stream<Map<String, ?>> stream(URI uri) throws IOException;

	default long serialize(URI uri, Schema schema, Stream<? extends Map<String, ?>> rowsToWrite) throws IOException {
		if (uri.getScheme().equals("file")) {
			// Arrow add magic-headers when writing to files
			try (FileOutputStream fos = new FileOutputStream(Paths.get(uri).toFile())) {
				return serialize(fos.getChannel(), true, schema, rowsToWrite);
			}
		} else {
			return serialize(Channels.newChannel(PepperURLHelper.outputStream(uri)), false, schema, rowsToWrite);
		}
	}

	long serialize(WritableByteChannel byteChannel,
			boolean outputIsFile,
			Schema schema,
			Stream<? extends Map<String, ?>> rowsToWrite) throws IOException;
}
