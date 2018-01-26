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
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.avro.AvroSchemaHelper;
import cormoran.pepper.avro.AvroTranscodingHelper;
import cormoran.pepper.io.PepperFileHelper;

public class TestWriteLargeParquet {
	@Test
	public void testWriteLarge() throws IOException {
		ParquetStreamFactory factory = new ParquetStreamFactory();

		Path tmpPath = PepperFileHelper.createTempPath("TestWriteParquet", ".parquet", true);

		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.of("key", 1));

		int nbRows1 = 1000 * 1000;
		Stream<GenericRecord> stream = IntStream.range(0, nbRows1).mapToObj(i -> ImmutableMap.of("key", i)).map(
				AvroTranscodingHelper.toGenericRecord(schema));

		// Parallel and unordered: suggest data can be processed concurrently: we want to check we do not try to write
		// concurrently in the outputstream
		stream = stream.parallel().unordered();
		long nbRows = factory.serialize(tmpPath.toUri(), stream);

		Assert.assertEquals(nbRows1, nbRows);

		// Read back to ensure the data is readable (it may fail if concurrent write have corrupted the flow)
		Assert.assertEquals(nbRows1, factory.stream(tmpPath.toUri()).count());
	}
}
