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
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link IAvroStreamFactory} producing Avro binary data
 * 
 * @author Benoit Lacelle
 *
 */
public class AvroStreamFactory implements IAvroStreamFactory {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AvroStreamFactory.class);

	@Override
	public Stream<GenericRecord> toStream(URI uri) throws IOException {
		InputStream openStream = uri.toURL().openStream();
		return new AvroBytesToStream().stream(openStream).onClose(() -> {
			try {
				openStream.close();
			} catch (IOException e) {
				LOGGER.trace("Ouch on closing", e);
			}
		});
	}

	@Override
	public long writeToPath(URI uri, Stream<? extends GenericRecord> rowsToWrite) throws IOException {
		// https://avro.apache.org/docs/1.8.1/gettingstartedjava.html
		// We will use the first record to prepare a writer on the correct schema
		AtomicReference<DataFileWriter<GenericRecord>> writer = new AtomicReference<>();
		AtomicReference<OutputStream> rawStream = new AtomicReference<>();

		AtomicLong nbRows = new AtomicLong();

		try {
			rowsToWrite.forEach(m -> {
				if (nbRows.get() == 0) {
					try {
						Schema schema = m.getSchema();
						DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
						OutputStream outputStream = uri.toURL().openConnection().getOutputStream();
						rawStream.set(outputStream);

						DataFileWriter<GenericRecord> dataFileWriter =
								new DataFileWriter<GenericRecord>(datumWriter).create(schema, outputStream);
						writer.set(dataFileWriter);
					} catch (NullPointerException e) {
						throw new IllegalStateException("Are you missing Hadoop binaries?", e);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}

				try {
					writer.get().append(m);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				nbRows.incrementAndGet();
			});
		} finally {
			if (writer.get() != null) {
				writer.get().close();
			}

			if (rawStream.get() != null) {
				rawStream.get().close();
			}

		}

		return nbRows.get();
	}

}
