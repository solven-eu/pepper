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
import java.io.UncheckedIOException;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificDatumReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps converting an InputStream to a Stream of {@link GenericRecord}
 * 
 * @author Benoit Lacelle
 *
 */
public class AvroBytesToStream implements IBinaryToAvroStream {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AvroBytesToStream.class);

	@Override
	public Stream<GenericRecord> stream(InputStream inputStream) throws IOException {
		SpecificDatumReader<GenericRecord> specificDatumReader = new SpecificDatumReader<GenericRecord>();
		DataFileStream<GenericRecord> dataFileStream = new DataFileStream<>(inputStream, specificDatumReader);

		return StreamSupport.stream(dataFileStream.spliterator(), false).onClose(() -> {
			try {
				dataFileStream.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
}
