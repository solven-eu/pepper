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
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.avro.LogicalType;
import org.apache.avro.LogicalTypes;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;

import cormoran.pepper.io.PepperSerializationHelper;
import cormoran.pepper.logging.PepperLogHelper;

/**
 * Various utilities related to Avro schema
 * 
 * @author Benoit Lacelle
 *
 */
public class AvroSchemaHelper {
	protected AvroSchemaHelper() {
		// hidden
	}

	@Deprecated
	public static Object converToAvroValue(Field schema, Object value) {
		return AvroTranscodingHelper.toAvro(schema, value);
	}

	// TODO How is this related to ParquetSchemaConverter?
	public static Schema proposeSimpleSchema(Map<String, ?> schemaAsMap) {
		return proposeSimpleSchema(schemaAsMap, ImmutableBiMap.of());
	}

	public static Schema proposeSchemaForValue(Object value) {
		if (value == null) {
			return Schema.create(Type.NULL);
		} else if (value instanceof CharSequence) {
			return Schema.create(Type.STRING);
		} else if (value instanceof Double) {
			return Schema.create(Type.DOUBLE);
		} else if (value instanceof Float) {
			return Schema.create(Type.FLOAT);
		} else if (value instanceof Long) {
			return Schema.create(Type.LONG);
		} else if (value instanceof Integer) {
			return Schema.create(Type.INT);
		} else if (value instanceof Serializable) {
			// This will catch float[], double[], java.time.LocalDate
			return Schema.create(Type.BYTES);
		} else {
			throw new UnsupportedOperationException("Can not handle " + PepperLogHelper.getObjectAndClass(value));
		}
	}

	/**
	 * @deprecated This method seems meaningless
	 */
	@Deprecated
	public static Optional<?> proposeDefaultValueForValue(Object value) {
		// If default value is set to null, we would get org.apache.avro.AvroRuntimeException: Field portfoliocode
		// type:STRING pos:1 not set and has no default value
		if (value instanceof CharSequence) {
			return Optional.empty();
		} else if (value instanceof Double) {
			return Optional.empty();
		} else if (value instanceof Float) {
			return Optional.empty();
		} else if (value instanceof Long) {
			return Optional.empty();
		} else if (value instanceof Integer) {
			return Optional.empty();
		} else if (value instanceof double[]) {
			return Optional.empty();
		} else if (value instanceof float[]) {
			return Optional.empty();
		} else if (value instanceof List<?>) {
			return Optional.empty();
		} else if (value instanceof Serializable) {
			return Optional.empty();
		} else {
			throw new UnsupportedOperationException("Can not handle " + PepperLogHelper.getObjectAndClass(value));
		}
	}

	/**
	 * @deprecated This method seems meaningless
	 */
	@Deprecated
	public static Optional<?> proposeDefaultValueForType(Type type) {
		// If default value is set to null, we would get org.apache.avro.AvroRuntimeException: Field portfoliocode
		// type:STRING pos:1 not set and has no default value
		if (type == Type.ARRAY) {
			return Optional.empty();
		} else if (type == Type.BOOLEAN) {
			return Optional.of(true);
		} else if (type == Type.BYTES) {
			try {
				return Optional.of(PepperSerializationHelper.toBytes(LocalDate.now()));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		} else if (type == Type.DOUBLE) {
			return Optional.of(123.456D);
		} else if (type == Type.ENUM) {
			return Optional.empty();
		} else if (type == Type.FIXED) {
			return Optional.empty();
		} else if (type == Type.FLOAT) {
			return Optional.of(1.2F);
		} else if (type == Type.INT) {
			return Optional.of(123);
		} else if (type == Type.LONG) {
			return Optional.of(12345L);
		} else if (type == Type.MAP) {
			return Optional.of(ImmutableMap.of("key", "value"));
		} else if (type == Type.NULL) {
			return Optional.empty();
		} else if (type == Type.RECORD) {
			return Optional.empty();
		} else if (type == Type.STRING) {
			return Optional.of("someString");
		} else if (type == Type.UNION) {
			// TODO: recursive call
			return Optional.empty();
		} else {
			throw new UnsupportedOperationException("Can not handle " + PepperLogHelper.getObjectAndClass(type));
		}
	}

	public static Schema proposeSimpleSchema(Map<String, ?> schemaAsMap, BiMap<String, String> sourceToTarget) {
		List<Field> fields = schemaAsMap.entrySet().stream().map(entry -> {

			Schema schema;
			Optional<?> defaultValue;
			try {
				schema = proposeSchemaForValue(entry.getValue());
				defaultValue = proposeDefaultValueForValue(entry.getValue());
			} catch (RuntimeException e) {
				throw new IllegalArgumentException("Can not guess schema for key=" + entry.getKey(), e);
			}

			// The field may be renamed for target Parquet file
			String targetFieldname = sourceToTarget.getOrDefault(entry.getKey(), entry.getKey());

			if (defaultValue.isPresent()) {
				return new Field(targetFieldname, schema, null, defaultValue.get());
			} else {
				// https://stackoverflow.com/questions/22938124/avro-field-default-values
				// https://avro.apache.org/docs/1.7.7/spec.html#Unions
				return new Field(targetFieldname,
						Schema.createUnion(Schema.create(Type.NULL), schema),
						null,
						Schema.NULL_VALUE);
			}

		}).collect(Collectors.toList());
		return Schema.createRecord("myrecord", null, "space", false, fields);
	}

	@Deprecated
	public static Map<String, Object> convertSparkSchemaToExampleMap(Schema schema) {
		return exampleMap(schema);
	}

	public static Map<String, Object> exampleMap(Schema schema) {
		return schema.getFields().stream().collect(Collectors.toMap(f -> f.name(), f -> exampleValue(f.schema())));
	}

	public static Object exampleValue(Schema schema) {
		Schema nonNullSchema = getNonNull(schema);

		if (nonNullSchema.getType() == Type.STRING) {
			return IPepperSchemaConstants.SOME_STRING;
		} else if (nonNullSchema.getType() == Type.INT) {
			return IPepperSchemaConstants.SOME_INT;
		} else if (nonNullSchema.getType() == Type.LONG) {
			return IPepperSchemaConstants.SOME_LONG;
		} else if (nonNullSchema.getType() == Type.FLOAT) {
			return IPepperSchemaConstants.SOME_FLOAT;
		} else if (nonNullSchema.getType() == Type.DOUBLE) {
			return IPepperSchemaConstants.SOME_DOUBLE;
		} else if (nonNullSchema.getType() == Type.NULL) {
			return null;
		} else if (nonNullSchema.getType() == Type.ARRAY) {
			Schema arrayType = nonNullSchema.getTypes().stream().filter(t -> t.getType() == Type.ARRAY).findAny().get();
			Schema elementType = arrayType.getElementType();

			if (elementType.getFields().size() == 1) {
				return Collections.singletonList(exampleValue(elementType));
			} else {
				throw new RuntimeException("Not handled: " + schema);
			}
		} else if (nonNullSchema.getType() == Type.MAP) {
			return schema.getFields().stream().collect(Collectors.toMap(f -> f, f -> exampleValue(f.schema())));
		} else if (nonNullSchema.getType() == Type.BYTES) {
			// Is it legit?
			return IPepperSchemaConstants.SOME_LOCALDATE;
		} else {
			throw new IllegalArgumentException("Not handled: " + schema);
		}
	}

	/**
	 * Given a schema, check to see if it is a union of a null type and a regular schema, and then return the non-null
	 * sub-schema. Otherwise, return the given schema.
	 *
	 * @param schema
	 *            The schema to check
	 * @return The non-null portion of a union schema, or the given schema
	 */
	// Duplicated from org.apache.parquet.avro.AvroSchemaConverter
	public static Schema getNonNull(Schema schema) {
		if (schema.getType().equals(Schema.Type.UNION)) {
			List<Schema> schemas = schema.getTypes();
			if (schemas.size() == 2) {
				if (schemas.get(0).getType().equals(Schema.Type.NULL)) {
					return schemas.get(1);
				} else if (schemas.get(1).getType().equals(Schema.Type.NULL)) {
					return schemas.get(0);
				} else {
					return schema;
				}
			} else {
				return schema;
			}
		} else {
			return schema;
		}
	}
}
