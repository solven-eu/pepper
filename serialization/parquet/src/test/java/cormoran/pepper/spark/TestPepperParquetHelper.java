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
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecordBuilder;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.avro.AvroSchemaHelper;
import cormoran.pepper.avro.AvroTranscodingHelper;
import cormoran.pepper.hadoop.PepperHadoopHelper;
import cormoran.pepper.io.PepperFileHelper;
import cormoran.pepper.parquet.ParquetStreamFactory;

public class TestPepperParquetHelper {
	@BeforeClass
	public static void checkHadoop() {
		PepperHadoopHelper.isHadoopReady();
		PepperSparkTestHelper.assumeHadoopEnv();
	}

	ParquetStreamFactory factory = new ParquetStreamFactory();

	@Test
	public void testSchemaFromMap() {
		Map<String, ?> asMap = ImmutableMap.<String, Object>builder()
				.put("stringField", "anyString")
				.put("doubleField", 0D)
				.put("floatField", 0F)
				.put("intField", 0)
				.put("LongField", 0L)
				.put("doubleArrayField", new double[2])
				.put("floatArrayField", new float[2])
				.put("doubleList", Collections.singletonList(1D))
				.build();
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(asMap);

		// We use an union to allow the field to hold null
		// Assert.assertEquals("array", schema.getField("doubleList").schema().getType().getName());
		Assert.assertEquals("union", schema.getField("doubleList").schema().getType().getName());
	}

	@Test
	public void testSchemaForDoubleArray() throws IOException {
		double[] doubles = new double[] { 123D };
		Map<String, ?> asMap = ImmutableMap.of("k", doubles);
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(asMap);

		// We use an union to allow the field to hold null
		// Assert.assertEquals("array", schema.getField("doubleList").schema().getType().getName());
		Assert.assertEquals("union", schema.getField("k").schema().getType().getName());

		{
			Path path = PepperFileHelper.createTempPath("apex", "parquet", true);
			factory.serialize(path.toUri(),
					Stream.of(ImmutableMap.of("k", doubles)).map(AvroTranscodingHelper.toGenericRecord(schema)));

			Map<String, ?> asMapAgain =
					factory.stream(path.toUri()).map(AvroTranscodingHelper.toJavaMap(asMap)).findAny().get();
			Assert.assertArrayEquals(doubles, (double[]) asMapAgain.get("k"), 0.0001D);
		}
	}

	@Test
	public void testSchemaForFloatArray() throws IOException {
		float[] doubles = new float[] { 123F };
		Map<String, ?> asMap = ImmutableMap.of("k", doubles);
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(asMap);

		// We use an union to allow the field to hold null
		// Assert.assertEquals("array", schema.getField("doubleList").schema().getType().getName());
		Assert.assertEquals("union", schema.getField("k").schema().getType().getName());

		{
			Path path = PepperFileHelper.createTempPath("apex", "parquet", true);
			factory.serialize(path.toUri(),
					Stream.of(ImmutableMap.of("k", doubles)).map(AvroTranscodingHelper.toGenericRecord(schema)));

			Map<String, ?> asMapAgain = ParquetStreamFactory.readParquetAsStream(path.toUri(), asMap).findAny().get();
			Assert.assertArrayEquals(doubles, (float[]) asMapAgain.get("k"), 0.0001F);
		}
	}

	@Test
	public void testWriteFloatArray() {
		Object floatArray = AvroTranscodingHelper.toAvro(null, new float[] { 1F });

		Assertions.assertThat(floatArray).isInstanceOf(ByteBuffer.class);
	}

	@Test
	public void testSchemaForNullLong() {
		Map<String, ?> asMap = ImmutableMap.of("LongField", 0L);
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(asMap);
		GenericData.Record record = new GenericRecordBuilder(schema).set("LongField", null).build();

		Assert.assertNull(record.get(0));
	}

	@Test
	public void testSchemaForLocalDate() throws IOException {
		Map<String, ?> asMap = ImmutableMap.of("DateField", LocalDate.now());

		Schema schema = AvroSchemaHelper.proposeSimpleSchema(asMap);

		LocalDate date = LocalDate.now();
		{
			GenericData.Record record = new GenericRecordBuilder(schema).set("DateField", date).build();
			Assert.assertEquals(date, record.get(0));
		}

		{
			PepperHadoopHelper.isHadoopReady();
			PepperSparkTestHelper.assumeHadoopEnv();

			Path path = PepperFileHelper.createTempPath("apex", "parquet", true);

			factory.serialize(path.toUri(),
					Stream.of(ImmutableMap.of("DateField", date)).map(AvroTranscodingHelper.toGenericRecord(schema)));

			Map<String, ?> asMapAgain = ParquetStreamFactory.readParquetAsStream(path.toUri(), asMap).findAny().get();
			Assert.assertEquals(date, asMapAgain.get("DateField"));
		}
	}

	public static final class NotSerializable {

	}

	@Test(expected = IllegalArgumentException.class)
	public void testSchemaForNotSerializable() throws IOException {
		Map<String, ?> asMap = ImmutableMap.of("NotSerializableField", new NotSerializable());

		AvroSchemaHelper.proposeSimpleSchema(asMap);
	}

	@Test
	public void testReadComplexFloatArray() throws IOException {
		Schema elementSchema = Schema.createRecord("arrayElement",
				"doc",
				"namespace",
				false,
				Arrays.asList(new Field("name",
						Schema.createUnion(Schema.create(Schema.Type.NULL), Schema.create(Schema.Type.FLOAT)),
						"doc",
						(Object) null)));
		Schema schema = Schema.createRecord("wholeRecord",
				"doc",
				"namespace",
				false,
				Arrays.asList(new Field("arrayField", elementSchema, "doc", (Object) null)));

		Record element1 = new GenericRecordBuilder(elementSchema).set("name", 123F).build();
		Record element2 = new GenericRecordBuilder(elementSchema).set("name", 234F).build();
		Record topRecord =
				new GenericRecordBuilder(schema).set("arrayField", Arrays.asList(element1, element2)).build();

		// Read as float[]
		{
			Map<String, ?> asMap =
					AvroTranscodingHelper.toJavaMap(topRecord, ImmutableMap.of("arrayField", new float[0]));

			Assert.assertTrue(asMap.get("arrayField") instanceof float[]);
		}

		// Read as double[]
		{
			Map<String, ?> asMap =
					AvroTranscodingHelper.toJavaMap(topRecord, ImmutableMap.of("arrayField", new double[0]));

			Assert.assertTrue(asMap.get("arrayField") instanceof double[]);
		}

		// Read as List<Float>
		{
			Map<String, ?> asMap =
					AvroTranscodingHelper.toJavaMap(topRecord, ImmutableMap.of("arrayField", Arrays.asList(1F)));

			Assert.assertTrue(asMap.get("arrayField") instanceof List);
		}

		// Read as List<Double>
		{
			Map<String, ?> asMap =
					AvroTranscodingHelper.toJavaMap(topRecord, ImmutableMap.of("arrayField", Arrays.asList(1D)));

			Assert.assertTrue(asMap.get("arrayField") instanceof List);
		}
	}

	@Test
	public void testToJavaMap_SchemaFloat_ValueFloat() throws IOException {
		Schema elementSchema = Schema.createRecord("arrayElement",
				"doc",
				"namespace",
				false,
				Arrays.asList(new Field("name",
						Schema.createUnion(Schema.create(Schema.Type.NULL), Schema.create(Schema.Type.FLOAT)),
						"doc",
						(Object) null)));

		Record element1 = new GenericRecordBuilder(elementSchema).set("name", 123F).build();

		Map<String, ?> asMap = AvroTranscodingHelper.toJavaMap(element1);

		Assert.assertTrue(asMap.get("name") instanceof Float);
	}

	@Test
	public void testToJavaMap_SchemaFloat_ValueNull() throws IOException {
		Schema elementSchema = Schema.createRecord("arrayElement",
				"doc",
				"namespace",
				false,
				Arrays.asList(new Field("name",
						Schema.createUnion(Schema.create(Schema.Type.NULL), Schema.create(Schema.Type.FLOAT)),
						"doc",
						(Object) null)));

		Record element1 = new GenericRecordBuilder(elementSchema).set("name", null).build();

		Map<String, ?> asMap = AvroTranscodingHelper.toJavaMap(element1);

		Assert.assertTrue(asMap.get("name") instanceof Float);
	}
}
