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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
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
	public Stream<GenericRecord> stream(URI uri) throws IOException {
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
	public long serialize(URI uri, Stream<? extends GenericRecord> rowsToWrite) throws IOException {
		return transcode(schema -> {
			try {
				return prepareRecordConsumer(schema, uri);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, rowsToWrite);
	}

	@Override
	public long serialize(OutputStream outputStream, Stream<? extends GenericRecord> rowsToWrite) throws IOException {
		return transcode(schema -> {
			try {
				return prepareRecordConsumer(schema, outputStream);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, rowsToWrite);
	}

	protected long transcode(Function<Schema, IGenericRecordConsumer> consumerSupplier,
			Stream<? extends GenericRecord> rowsToWrite) {
		// https://avro.apache.org/docs/1.8.1/gettingstartedjava.html
		// We will use the first record to prepare a writer on the correct schema
		AtomicLong nbRows = new AtomicLong();

		// Avro writing is mono-threaded: we switch to a Iterator to prevent any unordered-parallel stream to write
		// concurrently
		Iterator<? extends GenericRecord> it = rowsToWrite.iterator();
		if (it.hasNext()) {
			GenericRecord first = it.next();

			// We use the first GenericRecord to fetch the schema and initialize the output-stream
			Schema schema = first.getSchema();

			try (IGenericRecordConsumer dataFileWriter = consumerSupplier.apply(schema)) {
				// Write the first row, used to prepare the schema
				dataFileWriter.accept(first);
				nbRows.incrementAndGet();

				while (it.hasNext()) {
					dataFileWriter.accept(it.next());
					nbRows.incrementAndGet();
				}
			} catch (NullPointerException e) {
				throw new IllegalStateException("Are you missing Hadoop binaries?", e);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		return nbRows.get();
	}

	protected IGenericRecordConsumer prepareRecordConsumer(Schema schema, URI uri) throws IOException {
		OutputStream outputStream = outputStream(uri);
		return prepareRecordConsumer(schema, outputStream);
	}

	protected IGenericRecordConsumer prepareRecordConsumer(Schema schema, OutputStream outputStream)
			throws IOException {
		DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);

		DataFileWriter<GenericRecord> rawWriter = new DataFileWriter<GenericRecord>(datumWriter);
		DataFileWriter<GenericRecord> dataFileWriter = rawWriter.create(schema, outputStream);

		return new IGenericRecordConsumer() {

			@Override
			public void accept(GenericRecord datum) {
				try {
					dataFileWriter.append(datum);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public void close() throws IOException {
				// Flush the last bytes from the writer
				// But do not close the OutputStream as somebody may want to push more bytes
				dataFileWriter.close();
				rawWriter.close();
			}
		};

	}

	protected OutputStream outputStream(URI uri) throws IOException, MalformedURLException {
		if (uri.getScheme().equals("file")) {
			// For an unknown reason, the default connection to a file is not writable: we prepare the file manually
			return new FileOutputStream(Paths.get(uri).toFile());
		} else {
			return uri.toURL().openConnection().getOutputStream();
		}
	}

}
