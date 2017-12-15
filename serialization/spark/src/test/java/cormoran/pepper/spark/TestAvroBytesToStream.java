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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.dataformat.avro.AvroMapper;
import com.fasterxml.jackson.dataformat.avro.AvroSchema;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.primitives.Doubles;

import cormoran.pepper.avro.AvroBytesToStream;
import cormoran.pepper.avro.AvroStreamHelper;

public class TestAvroBytesToStream {
	// https://github.com/FasterXML/jackson-dataformats-binary/blob/master/avro/src/test/java/com/fasterxml/jackson/dataformat/avro/MapTest.java

	private final static String MAP_OR_NULL_SCHEMA_JSON =
			SchemaBuilder.map().values(Schema.create(Schema.Type.STRING)).toString();

	private final AvroMapper MAPPER = getMapper();

	protected AvroMapper _sharedMapper;

	protected AvroMapper getMapper() {
		if (_sharedMapper == null) {
			_sharedMapper = newMapper();
		}
		return _sharedMapper;
	}

	protected AvroMapper newMapper() {
		return new AvroMapper();
	}

	@Ignore("Not much interested in submitting Maps. We prefer submitted IndexedRecord")
	@Test
	public void testMapOrNull() throws Exception {
		AvroSchema schema = MAPPER.schemaFrom(MAP_OR_NULL_SCHEMA_JSON);

		schema.getAvroSchema().getType();

		DatumWriter<Map<?, ?>> userDatumWriter = new SpecificDatumWriter<Map<?, ?>>(schema.getAvroSchema());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Use DataFileWriter to write the schema in the bytes
		try (DataFileWriter<Map<?, ?>> fileWriter = new DataFileWriter<>(userDatumWriter)) {
			fileWriter.create(schema.getAvroSchema(), baos);
			fileWriter.append(ImmutableMap.of("x", "y"));
		}

		List<? extends Map<String, ?>> avroStream =
				new AvroBytesToStream().stream(new ByteArrayInputStream(baos.toByteArray()))
						.map(AvroStreamHelper.toJavaMap())
						.collect(Collectors.toList());

		Assert.assertEquals(1, avroStream.size());
		Assert.assertEquals(ImmutableMap.of("x", "y"), avroStream.get(0));
	}

	@Test
	public void testAvroToStream() throws IOException {
		GenericRowWithSchema row = new GenericRowWithSchema(new Object[] { "someValue" },
				new StructType(new StructField[] { new StructField("ccy", DataTypes.StringType, true, null) }));

		Schema outputSchema = Schema.createRecord("testSchema",
				"doc",
				"namespace",
				false,
				Arrays.asList(new Field("Currency", Schema.create(Type.STRING), null, Schema.NULL_VALUE)));

		BiMap<String, String> mapping = ImmutableBiMap.of("ccy", "Currency");
		InputStream stream = PepperSparkHelper.toAvro(outputSchema, Iterators.singletonIterator(row), mapping);

		List<?> resultAsList =
				new AvroBytesToStream().stream(stream).map(AvroStreamHelper.toJavaMap()).collect(Collectors.toList());

		Assert.assertEquals(1, resultAsList.size());
		Assert.assertEquals(ImmutableMap.of("Currency", "someValue"), resultAsList.get(0));
	}

	@Test
	public void testAvroToStream_doublearray() throws IOException {
		// Avro works with Collection by default
		GenericRowWithSchema row = new GenericRowWithSchema(new Object[] { Doubles.asList(1D, 2D, 3D) },
				new StructType(new StructField[] { new StructField("doubleArray", DataTypes.StringType, true, null) }));

		Schema outputSchema = Schema.createRecord("testSchema",
				"doc",
				"namespace",
				false,
				Arrays.asList(new Field("DoubleArray",
						Schema.createArray(Schema.create(Schema.Type.DOUBLE)),
						null,
						Schema.NULL_VALUE)));

		BiMap<String, String> mapping = ImmutableBiMap.of("doubleArray", "DoubleArray");
		InputStream stream = PepperSparkHelper.toAvro(outputSchema, Iterators.singletonIterator(row), mapping);

		List<?> resultAsList = new AvroBytesToStream().stream(stream)
				.map(AvroStreamHelper.toJavaMap(ImmutableMap.of("DoubleArray", new double[0])))
				.collect(Collectors.toList());

		Assert.assertEquals(1, resultAsList.size());
		Map<?, ?> singleOutput = (Map<?, ?>) resultAsList.get(0);
		Assert.assertEquals(ImmutableSet.of("DoubleArray"), singleOutput.keySet());
		Assert.assertArrayEquals(new double[] { 1D, 2D, 3D },
				(double[]) singleOutput.values().iterator().next(),
				0.0001D);
	}
}
