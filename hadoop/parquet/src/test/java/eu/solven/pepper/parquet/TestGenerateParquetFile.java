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
package eu.solven.pepper.parquet;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.util.Shell;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.io.PepperFileHelper;
import eu.solven.pepper.avro.AvroTranscodingHelper;

public class TestGenerateParquetFile {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestGenerateParquetFile.class);

	@Before
	public void testHadoopIsAvailable() {
		Assume.assumeTrue(Shell.hasWinutilsPath());
	}

	@Test
	public void testWriteManyDifferentTypes() throws IOException {
		ParquetStreamFactory factory = new ParquetStreamFactory();

		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.<String, Object>builder()
				.put("keyInteger", 123)
				.put("keyLong", 123456L)
				.put("keyFloat", 1.2F)
				.put("keyDouble", 987.654D)
				.put("keyString", "someString")
				.put("keyLocalDate", LocalDate.now())
				.build());

		int nbRows1 = 7;
		Stream<GenericRecord> stream = IntStream.range(0, nbRows1)
				.mapToObj(i -> ImmutableMap.<String, Object>builder()
						.put("keyInteger", i)
						.put("keyLong", 0L + i)
						.put("keyFloat", i / 7F)
						.put("keyDouble", i / 13D)
						.put("keyString", "someString" + i)
						.put("keyLocalDate", LocalDate.now().plusDays(i))
						.build())
				.map(AvroTranscodingHelper.toGenericRecord(schema));

		Path tmpPath = PepperFileHelper.createTempPath("TestWriteParquet", ".parquet", true);

		long nbRows = factory.serialize(tmpPath.toUri(), stream);
		LOGGER.debug("7 rows have been written in {}", tmpPath);

		Assert.assertEquals(nbRows1, nbRows);

		// Read back to ensure the data is readable (it may fail if concurrent write have corrupted the flow)
		Assert.assertEquals(nbRows1, factory.stream(tmpPath.toUri()).count());
	}
}
