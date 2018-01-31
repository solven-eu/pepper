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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.filter.PagedRecordFilter;
import org.apache.parquet.filter2.compat.FilterCompat;
import org.apache.parquet.filter2.compat.FilterCompat.Filter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;

import com.google.common.base.Suppliers;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;

import cormoran.pepper.avro.AvroStreamFactory;
import cormoran.pepper.avro.AvroTranscodingHelper;
import cormoran.pepper.avro.IGenericRecordConsumer;

/**
 * Enable converting a Parquet file to a Stream of Map
 * 
 * @author Benoit Lacelle
 *
 */
public class ParquetStreamFactory extends AvroStreamFactory {
	// We encounter performance issues comparable to the one in Configuration
	// For each read Parquet file, we search the FS for Hadoop configuration, which may take some time
	// Then, we use this mechanism to load the default configuration only once
	private static final Supplier<Configuration> DEFAULT_CONFIGURATION = Suppliers.memoize(() -> new Configuration());

	public static Configuration cloneDefaultConfiguration() {
		// Ensure the default properties have been loaded before cloning
		DEFAULT_CONFIGURATION.get().get("name");

		// Clone the default as it may be modified later
		return new Configuration(DEFAULT_CONFIGURATION.get());
	}

	private final Configuration configuration;

	public ParquetStreamFactory() {
		this(cloneDefaultConfiguration());
	}

	public ParquetStreamFactory(Configuration configuration) {
		this.configuration = configuration;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	@Override
	public Stream<GenericRecord> stream(URI uri) throws IOException {
		Path hadoopPath = toHadoopPath(uri);

		return toStream(hadoopPath);
	}

	/**
	 * 
	 * @param rawInputStream
	 *            a stream of bytes associated to a Parquet file. It is sub-optimal as Parquet require a
	 *            SeekableInputStream (i.e. an InputStream with RandomAccess). We will then copy the file in local FS
	 * @return
	 * @throws IOException
	 */
	public Stream<GenericRecord> stream(InputStream rawInputStream) throws IOException {
		return new ParquetBytesToStream().stream(rawInputStream).onClose(() -> {
			try {
				rawInputStream.close();
			} catch (IOException e) {
				LOGGER.trace("Ouch on closing", e);
			}
		});
	}

	public Stream<GenericRecord> toStream(Path hadoopPath) throws IOException {
		Filter filter = makeFilter();

		ParquetReader<GenericRecord> reader;
		try {
			reader = AvroParquetReader.<GenericRecord>builder(hadoopPath)
					.withFilter(filter)
					.withConf(getConfiguration())
					.build();
		} catch (IOException e) {
			// Default exception may not refer the input path
			throw new IOException("Issue on path: " + hadoopPath, e);
		} catch (RuntimeException e) {
			// Default exception may not refer the input path
			throw new IOException("Issue on path: " + hadoopPath, e);
		}

		return toStream(reader);
	}

	protected Filter makeFilter() {
		// According to javadoc, numbering starts at 1. However, it seems to be 0 not to skip any row
		// This default should apply no filter, but demonstrate how to filter a page
		return FilterCompat.get(PagedRecordFilter.page(0, Long.MAX_VALUE));
	}

	protected Stream<GenericRecord> toStream(ParquetReader<GenericRecord> reader) {
		return Streams.stream(new AbstractIterator<GenericRecord>() {

			@Override
			protected GenericRecord computeNext() {
				final GenericRecord next;
				try {
					next = reader.read();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				if (next == null) {
					return endOfData();
				} else {
					return next;
				}
			}

		});
	}

	public static Stream<Map<String, ?>> readParquetAsStream(URI uriToParquet, Map<String, ?> exampleTypes)
			throws FileNotFoundException, IOException {
		File file = Paths.get(uriToParquet).toFile();
		return new ParquetBytesToStream().stream(new FileInputStream(file))
				.map(AvroTranscodingHelper.toJavaMap(exampleTypes));
	}

	@Override
	protected IGenericRecordConsumer prepareRecordConsumer(Schema schema, URI uri) throws IOException {
		ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(toHadoopPath(uri))
				.withSchema(schema)
				.withConf(getConfiguration())
				.build();

		return new IGenericRecordConsumer() {

			@Override
			public void accept(GenericRecord t) {
				try {
					writer.write(t);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public void close() throws IOException {
				writer.close();
			}
		};
	}

	protected Path toHadoopPath(URI uri) {
		return new Path(uri);
	}
}
