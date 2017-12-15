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
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

/**
 * A {@link IAvroStreamFactory} producing Avro binary data
 * 
 * @author Benoit Lacelle
 *
 */
public class AvroStreamFactory implements IAvroStreamFactory {

	@Override
	public Stream<GenericRecord> toStream(Path javaPath) throws IOException {
		return new AvroBytesToStream().stream(javaPath.toUri().toURL().openStream());
	}

	@Override
	public long writeToPath(Path javaPath, Stream<? extends GenericRecord> rowsToWrite) throws IOException {
		// https://avro.apache.org/docs/1.8.1/gettingstartedjava.html
		// We will use the first record to prepare a writer on the correct schema
		AtomicReference<DataFileWriter<GenericRecord>> writer = new AtomicReference<>();

		AtomicLong nbRows = new AtomicLong();
		rowsToWrite.forEach(m -> {

			if (nbRows.get() == 0) {

				try {
					Schema schema = m.getSchema();
					DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<GenericRecord>(schema);
					DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(datumWriter);
					writer.set(dataFileWriter);
					dataFileWriter.create(schema, javaPath.toFile());
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

		if (writer.get() != null) {
			writer.get().close();
		}

		return nbRows.get();
	}

}
