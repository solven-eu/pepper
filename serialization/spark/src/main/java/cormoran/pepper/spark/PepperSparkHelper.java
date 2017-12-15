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
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.ArrayType;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DoubleType;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.common.primitives.Doubles;

import cormoran.pepper.io.PepperSerializationHelper;
import scala.collection.JavaConverters;
import scala.collection.mutable.WrappedArray;
import scala.compat.java8.JFunction;

/**
 * Some basic utilities for Spark
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperSparkHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperSparkHelper.class);

	protected PepperSparkHelper() {
		// hidden
	}

	public static InputStream toAvro(Schema outputSchema,
			Iterator<Row> f,
			BiMap<String, String> inputToOutputColumnMapping) throws IOException {
		// We write IndexedRecord instead of Map<?,?> as it is implied by the schema: a schema holding a Map would not
		// defines the fields
		DatumWriter<IndexedRecord> userDatumWriter = new SpecificDatumWriter<IndexedRecord>(outputSchema);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Use DataFileWriter to write the schema in the bytes
		try (DataFileWriter<IndexedRecord> fileWriter = new DataFileWriter<>(userDatumWriter)) {
			fileWriter.create(outputSchema, baos);

			Streams.stream(f).forEach(row -> {
				try {
					Map<String, ?> asMap = rowToMap(outputSchema, row, inputToOutputColumnMapping);
					IndexedRecord record = mapToIndexedRecord(outputSchema, asMap);
					fileWriter.append(record);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}

		return new ByteArrayInputStream(baos.toByteArray());
	}

	private static Map<String, ?> rowToMap(Schema outputSchema, Row row, BiMap<String, String> columnMapping) {
		return outputSchema.getFields()
				.stream()
				.map(f -> columnMapping.inverse().getOrDefault(f.name(), f.name()))
				.collect(
						Collectors.toMap(fName -> columnMapping.getOrDefault(fName, fName), fName -> row.getAs(fName)));
	}

	private static IndexedRecord mapToIndexedRecord(Schema schema, Map<?, ?> row) {
		Record r = new Record(schema);

		for (Field field : r.getSchema().getFields()) {
			Object valueToWrite = row.get(field.name());

			valueToWrite = convertFromSparkToAvro(field, valueToWrite, s -> {
				try {
					return ByteBuffer.wrap(PepperSerializationHelper.toBytes(s));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});

			r.put(field.name(), valueToWrite);
		}

		return r;
	}

	public static Object convertFromSparkToAvro(Field field,
			Object valueToWrite,
			Function<Serializable, ByteBuffer> serializer) {
		if (valueToWrite instanceof WrappedArray<?>) {
			List<?> asList = ImmutableList
					.copyOf(JavaConverters.asJavaCollectionConverter(((WrappedArray<?>) valueToWrite).toIterable())
							.asJavaCollection());
			valueToWrite = asList;

			if (field.schema().getType() == Schema.Type.UNION
					&& field.schema().getTypes().contains(Schema.create(Schema.Type.BYTES))) {
				// byte[] bytes = new byte[Ints.checkedCast(IApexMemoryConstants.DOUBLE * asList.size())];
				// ByteBuffer.wrap(bytes).asDoubleBuffer().put(primitiveArray);

				double[] primitiveArray = Doubles.toArray((Collection<? extends Number>) asList);

				// Avro requires a ByteBuffer. See org.apache.avro.generic.GenericData.getSchemaName(Object)
				// Parquet seems to handle both byte[] and ByteBuffer
				valueToWrite = serializer.apply(primitiveArray);
			}
		}
		return valueToWrite;
	}

	public static Map<String, Object> convertSparkSchemaToExampleMap(StructType schema) {
		Map<String, Object> schemaAsMap = new HashMap<>();
		schema.foreach(JFunction.func(arg0 -> {
			if (arg0.dataType().typeName().equals("string")) {
				schemaAsMap.put(arg0.name(), "someString");
			} else if (arg0.dataType().typeName().equals("integer")) {
				schemaAsMap.put(arg0.name(), 1);
			} else if (arg0.dataType().typeName().equals("double")) {
				schemaAsMap.put(arg0.name(), 1D);
			} else if (arg0.dataType().typeName().equals("array")) {
				ArrayType arrayType = (ArrayType) arg0.dataType();
				DataType elementType = arrayType.elementType();

				if (elementType instanceof DoubleType) {
					schemaAsMap.put(arg0.name(), Collections.singletonList(1D));
				} else {
					throw new RuntimeException("Not handled: " + arg0);
				}
			} else {
				throw new RuntimeException("Not handled: " + arg0);
			}

			return null;
		}));

		return schemaAsMap;
	}

}
