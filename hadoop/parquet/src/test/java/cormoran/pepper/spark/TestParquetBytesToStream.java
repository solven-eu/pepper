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
package cormoran.pepper.spark;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.avro.AvroSchemaHelper;
import cormoran.pepper.avro.AvroTranscodingHelper;
import cormoran.pepper.avro.IAvroStreamFactory;
import cormoran.pepper.io.PepperFileHelper;
import cormoran.pepper.parquet.ParquetStreamFactory;

public class TestParquetBytesToStream {
	@Test
	public void testConvertListMapBackAndForth() throws IOException {
		TestReadWrite.ensureAndAssumeHadoopEnvForTests();

		String stringField = "stringField";
		String doubleField = "doubleField";
		String doubleArrayField = "doubleArrayField";

		Map<String, Serializable> exampleMap =
				ImmutableMap.of(stringField, "anyString", doubleField, 0D, doubleArrayField, new double[2]);
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(exampleMap);

		List<Map<String, Object>> list = Arrays.asList(ImmutableMap
				.of(stringField, "stringValue", doubleField, 123D, doubleArrayField, new double[] { 234D, 345D }));

		Path pathOnDisk = PepperFileHelper.createTempPath(getClass().getSimpleName(), ".tmp", true);
		IAvroStreamFactory factory = new ParquetStreamFactory();

		// Write to disk
		factory.writeToPath(pathOnDisk, list.stream().map(AvroTranscodingHelper.toGenericRecord(schema)));

		// Read to Java maps
		Stream<? extends Map<String, ?>> asMapStream =
				factory.stream(pathOnDisk.toUri()).map(AvroTranscodingHelper.toJavaMap(exampleMap));

		List<Map<String, ?>> asMapList = asMapStream.collect(Collectors.toList());

		Assert.assertEquals(asMapList.size(), list.size());

		for (int i = 0; i < list.size(); i++) {
			Map<String, Object> originalItem = list.get(i);
			Map<String, ?> rereadItem = asMapList.get(i);
			Assert.assertEquals(rereadItem.keySet(), originalItem.keySet());

			for (String key : originalItem.keySet()) {
				Object originalValue = originalItem.get(key);
				Object rereadValue = rereadItem.get(key);

				if (rereadValue instanceof double[]) {
					Assert.assertArrayEquals(key, (double[]) originalValue, (double[]) rereadValue, 0.001D);
				} else {
					Assert.assertEquals(key, originalValue, rereadValue);
				}
			}
		}
	}
}
