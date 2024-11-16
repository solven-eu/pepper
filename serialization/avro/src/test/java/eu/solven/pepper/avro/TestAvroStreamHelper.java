/**
 * The MIT License
 * Copyright (c) 2014-2024 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.avro;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

public class TestAvroStreamHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TestAvroStreamHelper.class);
	final LocalDate now = LocalDate.now();

	@Test
	public void testToMap() {
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3"));
		IndexedRecord record = new GenericData.Record(schema);

		record.put(0, "v0");
		record.put(1, "v1");
		record.put(2, "v2");

		Map<String, ?> map = AvroTranscodingHelper.toJavaMap(record);

		Assertions.assertEquals(ImmutableMap.of("k1", "v0", "k2", "v1", "k3", "v2"), map);

		// Ensure we maintained the original ordering
		Assertions.assertEquals(Arrays.asList("k1", "k2", "k3"), new ArrayList<>(map.keySet()));
	}

	@Test
	public void testToMap_MissingColumnInMap() {
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3"));

		GenericRecord transcoded =
				AvroTranscodingHelper.toGenericRecord(schema).apply(ImmutableMap.of("k1", "v1", "k2", "v2"));

		IndexedRecord record = new GenericData.Record(schema);
		record.put(0, "v1");
		record.put(1, "v2");
		Assertions.assertEquals(record, transcoded);
	}

	@Test
	public void testToMap_AdditionalColumnInMap() {
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.of("k1", "v1", "k2", "v2"));

		GenericRecord transcoded = AvroTranscodingHelper.toGenericRecord(schema)
				.apply(ImmutableMap.of("k1", "v1", "k2", "v2", "k3", "v3"));

		IndexedRecord record = new GenericData.Record(schema);
		record.put(0, "v1");
		record.put(1, "v2");
		Assertions.assertEquals(record, transcoded);
	}

	@Test
	public void testToGenericRecord_SecondMapHasMissingKey() {
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.of("k1", "v1", "k2", "v2"));

		Function<Map<String, ?>, GenericRecord> mapper = AvroTranscodingHelper.toGenericRecord(schema);

		GenericRecord firstRecord = mapper.apply(ImmutableMap.of("k1", "v1", "k2", "v2"));
		GenericRecord secondRecord = mapper.apply(ImmutableMap.of("k2", "v2'"));

		Assertions.assertEquals("v1", firstRecord.get("k1"));
		Assertions.assertEquals("v2", firstRecord.get("k2"));

		Assertions.assertEquals(null, secondRecord.get("k1"));
		Assertions.assertEquals("v2'", secondRecord.get("k2"));
	}

	@Test
	public void testToGenericRecord_FloatArray() {
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.of("k1", new float[] { 2F }));

		Function<Map<String, ?>, GenericRecord> mapper = AvroTranscodingHelper.toGenericRecord(schema);

		GenericRecord firstRecord = mapper.apply(ImmutableMap.of("k1", new float[] { 2F }));

		Assertions.assertTrue(firstRecord.get("k1") instanceof ByteBuffer);

		// No type information: keep the raw byte[]
		Map<String, ?> backToMapNoType = AvroTranscodingHelper.toJavaMap(firstRecord);
		Assertions.assertTrue(backToMapNoType.get("k1") instanceof ByteBuffer);

		// Exact byte[] info (float[])
		Map<String, ?> backToMapWithMap =
				AvroTranscodingHelper.toJavaMap(firstRecord, Collections.singletonMap("k1", new float[0]));
		Assertions.assertArrayEquals(new float[] { 2F }, (float[]) backToMapWithMap.get("k1"), 0.01F);

		// Inexact byte[] info (double[]): we deserialize to float[], but should we transcode to double[]?
		Map<String, ?> backToMapWithDoubleMap =
				AvroTranscodingHelper.toJavaMap(firstRecord, Collections.singletonMap("k1", new double[0]));
		Assertions.assertArrayEquals(new float[] { 2F }, (float[]) backToMapWithDoubleMap.get("k1"), 0.01F);
	}

	@Test
	public void testToGenericRecord_NullValue() {
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.of("k1", 123L));

		Function<Map<String, ?>, GenericRecord> mapper = AvroTranscodingHelper.toGenericRecord(schema);

		GenericRecord firstRecord = mapper.apply(Collections.singletonMap("k1", null));
		Assertions.assertNull(firstRecord.get("k1"));

		Map<String, ?> backToMapNoType = AvroTranscodingHelper.toJavaMap(firstRecord);
		Assertions.assertNull(backToMapNoType.get("k1"));
	}

	@Test
	public void testAvroToByteArray() throws IOException {
		Map<String, String> singleMap = ImmutableMap.of("k1", "v1");
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(singleMap);

		InputStream is =
				AvroStreamHelper.toInputStream(Stream.of(singleMap).map(AvroTranscodingHelper.toGenericRecord(schema)),
						() -> Executors.newSingleThreadExecutor());

		byte[] bytes = ByteStreams.toByteArray(is);

		List<Map<String, ?>> backToList = AvroStreamHelper.toGenericRecord(new ByteArrayInputStream(bytes))
				.map(AvroTranscodingHelper.toJavaMap())
				.collect(Collectors.toList());

		Assertions.assertEquals(1, backToList.size());
		Assertions.assertEquals(singleMap, backToList.get(0));
	}

	@Test
	public void testAvroToByteArray_LocalDate_NoInfoBackToJava() throws IOException {
		Map<String, ?> singleMap = ImmutableMap.of("k1", now);
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(singleMap);

		byte[] bytes;
		try (InputStream is =
				AvroStreamHelper.toInputStream(Stream.of(singleMap).map(AvroTranscodingHelper.toGenericRecord(schema)),
						() -> Executors.newSingleThreadExecutor())) {
			bytes = ByteStreams.toByteArray(is);
		}

		List<Map<String, ?>> backToList = AvroStreamHelper.toGenericRecord(new ByteArrayInputStream(bytes))
				.map(AvroTranscodingHelper.toJavaMap())
				.collect(Collectors.toList());

		Assertions.assertEquals(1, backToList.size());
		Assertions.assertTrue(backToList.get(0).get("k1") instanceof ByteBuffer);
	}

	@Test
	public void testAvroToByteArray_LocalDate_WithInfoBackToJava() throws IOException {
		Map<String, ?> singleMap = ImmutableMap.of("k1", now);
		Schema schema = AvroSchemaHelper.proposeSimpleSchema(singleMap);

		byte[] bytes;
		try (InputStream is =
				AvroStreamHelper.toInputStream(Stream.of(singleMap).map(AvroTranscodingHelper.toGenericRecord(schema)),
						() -> Executors.newSingleThreadExecutor())) {
			bytes = ByteStreams.toByteArray(is);
		}

		List<Map<String, ?>> backToList = AvroStreamHelper.toGenericRecord(new ByteArrayInputStream(bytes))
				.map(AvroTranscodingHelper.toJavaMap(singleMap))
				.collect(Collectors.toList());

		Assertions.assertEquals(1, backToList.size());
		Assertions.assertEquals(singleMap, backToList.get(0));
	}

	// We write as Serializable Object, we read as String
	@Test
	public void testAvroToByteArray_LocalDate_WithInfoAsString() throws IOException {
		Map<String, ?> singleMap = ImmutableMap.of("k1", now);
		Schema schemaWrite = AvroSchemaHelper.proposeSimpleSchema(singleMap);

		byte[] bytes;
		try (InputStream is = AvroStreamHelper.toInputStream(
				Stream.of(singleMap).map(AvroTranscodingHelper.toGenericRecord(schemaWrite)),
				() -> Executors.newSingleThreadExecutor())) {
			bytes = ByteStreams.toByteArray(is);
		}

		// We read as String
		List<Map<String, ?>> backToList = AvroStreamHelper.toGenericRecord(new ByteArrayInputStream(bytes))
				.map(AvroTranscodingHelper.toJavaMap(ImmutableMap.of("k1", "someString")))
				.collect(Collectors.toList());

		Assertions.assertEquals(1, backToList.size());
		Map<String, ?> singleOutput = backToList.get(0);
		Assertions.assertEquals(singleMap.get("k1").toString(), singleOutput.get("k1"));
	}

	// We write as String, we read as Object
	@Test
	public void testAvroToByteArray_String_WithInfoBackToJava() throws IOException {
		Map<String, ?> singleMap = ImmutableMap.of("k1", now.toString());
		Schema schemaWrite = AvroSchemaHelper.proposeSimpleSchema(singleMap);

		byte[] bytes;
		try (InputStream is = AvroStreamHelper.toInputStream(
				Stream.of(singleMap).map(AvroTranscodingHelper.toGenericRecord(schemaWrite)),
				() -> Executors.newSingleThreadExecutor())) {
			bytes = ByteStreams.toByteArray(is);
		}

		// We read as Object
		List<Map<String, ?>> backToList = AvroStreamHelper.toGenericRecord(new ByteArrayInputStream(bytes))
				.map(AvroTranscodingHelper.toJavaMap(ImmutableMap.of("k1", now)))
				.collect(Collectors.toList());

		Assertions.assertEquals(1, backToList.size());
		Map<String, ?> singleOutput = backToList.get(0);
		Assertions.assertEquals(now, singleOutput.get("k1"));
	}

	@Disabled("TODO Fix it. Issue with byte[]")
	@Test
	public void testAllTypes() throws IOException {
		for (Type type : Type.values()) {
			Schema schema;
			try {
				schema = Schema.create(type);
			} catch (AvroRuntimeException e) {
				LOGGER.trace("Invalid type for schema: " + type, e);
				continue;
			}

			Object someValue = AvroSchemaHelper.proposeDefaultValueForType(type).orElse(null);
			Field field = new Field("fieldName", schema, null, someValue);

			Schema record = Schema.createRecord("myrecord", null, "space", false, Arrays.asList(field));

			byte[] bytes;

			// Collections.singletonMap enable a null value (especially for NULL type)
			try (InputStream is = AvroStreamHelper.toInputStream(
					Stream.of(Collections.singletonMap("fieldName", someValue))
							.map(AvroTranscodingHelper.toGenericRecord(record)),
					() -> Executors.newSingleThreadExecutor())) {
				bytes = ByteStreams.toByteArray(is);
			}

			List<GenericRecord> backToMap =
					AvroStreamHelper.toGenericRecord(new ByteArrayInputStream(bytes)).collect(Collectors.toList());

			Assertions.assertEquals(1, backToMap.size());
			GenericRecord single = backToMap.get(0);

			Object toJdk = AvroTranscodingHelper.toJdk(single.get(0), () -> null);
			if (type == Type.NULL) {
				Assertions.assertNull(toJdk);
			} else {
				Assertions.assertNotNull(toJdk, String.valueOf(someValue));
			}
			// Assertions.assertEquals(someValue, AvroTranscodingHelper.toJdk(single.get(0), () -> null));
		}
	}

	@Test
	public void testAllTypes_UnionNull() throws IOException {
		for (Type type : Type.values()) {
			if (type == Type.NULL) {
				// Meaningless to do Union (NULL,NULL)
				continue;
			}

			Schema schema;
			try {
				schema = Schema.create(type);
			} catch (AvroRuntimeException e) {
				LOGGER.trace("Invalid type for schema: " + type, e);
				continue;
			}

			Object someValue = AvroSchemaHelper.proposeDefaultValueForType(type).orElse(null);
			Field field = new Field("fieldName",
					Schema.createUnion(Schema.create(Type.NULL), schema),
					null,
					Schema.NULL_VALUE);

			Schema record = Schema.createRecord("myrecord", null, "space", false, Arrays.asList(field));

			byte[] bytes;
			try (InputStream is = AvroStreamHelper.toInputStream(
					Stream.of(ImmutableMap.of("fieldName", someValue))
							.map(AvroTranscodingHelper.toGenericRecord(record)),
					() -> Executors.newSingleThreadExecutor())) {
				bytes = ByteStreams.toByteArray(is);
			}

			List<GenericRecord> backToMap =
					AvroStreamHelper.toGenericRecord(new ByteArrayInputStream(bytes)).collect(Collectors.toList());

			Assertions.assertEquals(1, backToMap.size());
			GenericRecord single = backToMap.get(0);

			Assertions.assertNotNull(AvroTranscodingHelper.toJdk(single.get(0), () -> null));
			// Assertions.assertEquals(someValue, AvroTranscodingHelper.toJdk(single.get(0), () -> null));
		}
	}
}
