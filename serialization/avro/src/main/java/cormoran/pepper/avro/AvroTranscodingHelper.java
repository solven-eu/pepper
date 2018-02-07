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
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.avro.generic.IndexedRecord;
import org.apache.avro.util.Utf8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import cormoran.pepper.io.PepperSerializationHelper;

/**
 * Helps converting avro records to standard Java objects
 * 
 * @author Benoit Lacelle
 *
 */
public class AvroTranscodingHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AvroTranscodingHelper.class);

	protected AvroTranscodingHelper() {
		// hidden
	}

	public static Object toJdk(Object value, Supplier<?> exampleValue) {
		if (value == null) {
			return null;
		}

		boolean endWithToString = false;

		if (value instanceof Utf8) {
			Utf8 asUtf8 = (Utf8) value;
			Object targetType = exampleValue.get();

			if (targetType != null && !(targetType instanceof String)) {
				// We received a String but we expect something else
				// TODO: Should we try to parse as a CharSequence?
				Optional<? extends Object> safeToObject =
						PepperSerializationHelper.safeToObject(targetType.getClass(), targetType.toString());
				if (safeToObject.isPresent()) {
					value = safeToObject.get();
				}
			} else {
				// A String is more vanilla than Utf8, even if the performance penalty may be huge
				value = asUtf8.toString();
			}
		} else if (value instanceof ByteBuffer) {
			Object targetType = exampleValue.get();

			if (targetType != null) {
				// Typically happens on LocalDate
				ByteBuffer byteBuffer = (ByteBuffer) value;

				try {
					value = PepperSerializationHelper.fromBytes(byteBuffer.array());
				} catch (ClassNotFoundException | IOException e) {
					throw new RuntimeException(e);
				}

				if (targetType instanceof String) {
					endWithToString = true;
				}
			}

		} else if (value instanceof GenericData.Fixed) {
			// We received a predefined-length array of bytes
			GenericData.Fixed fixed = (GenericData.Fixed) value;

			Object targetType = exampleValue.get();
			if (targetType != null) {
				if (targetType instanceof double[]) {
					value = convertToDouble(fixed);
				} else if (targetType instanceof float[]) {
					value = convertToFloat(fixed);
				} else {
					throw new RuntimeException("Issue with " + targetType);
				}
			} else {
				// Guess it is double[]
				value = convertToDouble(fixed);
			}
		} else if (value instanceof List<?>) {
			List<?> asList = (List<?>) value;

			// TODO: we should read primitive directly from Parquet
			Object targetType = exampleValue.get();
			Optional<?> opt = toPrimitiveArray(targetType, asList);
			if (opt.isPresent()) {
				value = opt.get();
			}
		} else if (value instanceof byte[]) {
			Object targetType = exampleValue.get();

			if (targetType != null) {
				try {
					value = PepperSerializationHelper.fromBytes((byte[]) value);
				} catch (ClassNotFoundException | IOException e) {
					throw new RuntimeException(e);
				}

				if (targetType instanceof String) {
					endWithToString = true;
				}
			}
		}

		if (endWithToString && value != null) {
			value = value.toString();
		}

		return value;
	}

	public static double[] convertToDouble(GenericData.Fixed fixed) {
		// Convert Fixed (which wraps a byte[]) to a double[]
		ByteBuffer bytes = ByteBuffer.wrap(fixed.bytes());

		// https://stackoverflow.com/questions/3770289/converting-array-of-primitives-to-array-of-containers-in-java
		// TODO use ArrayUtils?
		DoubleBuffer asDoubleBuffer = bytes.asDoubleBuffer();
		double[] array = new double[asDoubleBuffer.capacity()];
		asDoubleBuffer.get(array);
		return array;
	}

	public static float[] convertToFloat(GenericData.Fixed fixed) {
		ByteBuffer bytes = ByteBuffer.wrap(fixed.bytes());

		// https://stackoverflow.com/questions/3770289/converting-array-of-primitives-to-array-of-containers-in-java
		// TODO use ArrayUtils?
		FloatBuffer asDoubleBuffer = bytes.asFloatBuffer();
		float[] array = new float[asDoubleBuffer.capacity()];
		asDoubleBuffer.get(array);
		return array;
	}

	public static Optional<?> toPrimitiveArray(Object targetType, List<?> asList) {
		if (asList.isEmpty()) {
			return Optional.empty();
		} else {
			final boolean targetPrimitiveFloat;
			final boolean targetPrimitiveDouble;

			if (targetType instanceof float[]) {
				targetPrimitiveFloat = true;
				targetPrimitiveDouble = false;
			} else if (targetType instanceof double[]) {
				targetPrimitiveFloat = false;
				targetPrimitiveDouble = true;
			} else {
				Object first = asList.get(0);

				if (first instanceof Float) {
					targetPrimitiveFloat = true;
					targetPrimitiveDouble = false;
				} else if (first instanceof Double) {
					targetPrimitiveFloat = false;
					targetPrimitiveDouble = true;
				} else {
					// TODO: Improve this case?
					targetPrimitiveFloat = false;
					targetPrimitiveDouble = false;
				}
			}

			Object first = asList.get(0);

			if (first instanceof GenericData.Record) {
				GenericData.Record asRecord = (GenericData.Record) first;

				if (asRecord.getSchema().getFields().size() == 1) {
					Field singleField = asRecord.getSchema().getFields().get(0);

					if (holdNumber(singleField)) {
						// TODO: this does not handle the case we haver both double and string in the union
						if (targetPrimitiveFloat) {
							float[] floats = new float[asList.size()];

							for (int i = 0; i < asList.size(); i++) {
								floats[i] = ((Number) ((GenericData.Record) asList.get(i)).get(0)).floatValue();
							}
							return Optional.of(floats);
						} else if (targetPrimitiveDouble) {
							double[] doubles = new double[asList.size()];

							for (int i = 0; i < asList.size(); i++) {
								doubles[i] = ((Number) ((GenericData.Record) asList.get(i)).get(0)).doubleValue();
							}
							return Optional.of(doubles);
						}
					}

					return Optional.empty();
				} else {
					return Optional.empty();
				}
			} else if (targetPrimitiveFloat) {
				return Optional.of(Floats.toArray((List<Number>) asList));
			} else if (targetPrimitiveDouble) {
				return Optional.of(Doubles.toArray((List<Number>) asList));
			} else {
				return Optional.empty();
			}
		}
	}

	/**
	 * Used to detect if a field holds a single number, would it be through a Union with NULL, or a record with a single
	 * number field
	 * 
	 * @param singleField
	 * @return
	 */
	private static boolean holdNumber(Field singleField) {
		return singleField.schema().getType() == Type.DOUBLE || singleField.schema().getType() == Type.FLOAT
				|| singleField.schema().getType() == Type.UNION && singleField.schema()
						.getTypes()
						.stream()
						.filter(s -> s.getType() == Type.DOUBLE || s.getType() == Type.FLOAT)
						.findAny()
						.isPresent();
	}

	/**
	 * 
	 * @param schema
	 *            the schema of the whole record, not only given value
	 * @param value
	 * @return
	 */
	public static Object toAvro(Field schema, Object value) {
		if (value instanceof Number || value instanceof String || value instanceof Boolean) {
			return value;
			// } else if (value instanceof double[]) {
			// // TODO use a buffer byte[] or ByteBuffer
			// double[] doubles = (double[]) value;
			// byte[] bytes = new byte[Ints.checkedCast(doubles.length * IApexMemoryConstants.DOUBLE)];
			// ByteBuffer byteArray = ByteBuffer.wrap(bytes);
			// byteArray.asDoubleBuffer().put(doubles);
			// return new Fixed(schema.schema(), bytes);
			// } else if (value instanceof float[]) {
			// // TODO use a buffer byte[] or ByteBuffer
			// float[] floats = (float[]) value;
			// // byte[] bytes = new byte[Ints.checkedCast(doubles.length * IApexMemoryConstants.FLOAT)];
			// // ByteBuffer byteArray = ByteBuffer.wrap(bytes);
			// // byteArray.asFloatBuffer().put(doubles);
			// return Floats.asList(floats);
		} else if (value instanceof Serializable) {
			try {
				// Avro does not handle byte[], but it is OK with ByteBuffer
				// see org.apache.avro.generic.GenericData.getSchemaName(Object)
				return ByteBuffer.wrap(PepperSerializationHelper.toBytes((Serializable) value));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			return value;
		}
	}

	/**
	 * This method helps transcoding an Avro IndexedRecord to a standard java Map. It may induce a performance penalty,
	 * typically by converting by default all Utf8 to a String
	 * 
	 * @param indexedRecord
	 *            an Avro IndexedRecord
	 * @param exampleTypes
	 *            a Map describing the expected type of each value of the output Map
	 * @return a {@link Map} equivalent o the input IndexedRecord but after having converted values to types as defined
	 *         in the example Map
	 */
	public static Map<String, ?> toJavaMap(IndexedRecord indexedRecord, Map<? extends String, ?> exampleTypes) {
		Map<String, Object> asMap = new LinkedHashMap<>();

		List<Field> fields = indexedRecord.getSchema().getFields();
		for (int i = 0; i < fields.size(); i++) {
			Field f = fields.get(i);
			String fieldName = f.name();

			// We need to convert keys from Utf8 to String
			Object exampleValue = exampleTypes.get(fieldName);

			// To transcode the value, should we rely on the schema or the value? The schema is generally better but the
			// value is useful to interpret raw bytes or complex types
			Object recordValue = indexedRecord.get(i);

			if (recordValue == null) {
				// The input object is not useful: we prefer to rely on the schema
				recordValue = AvroSchemaHelper.exampleValue(f.schema());
			}

			Object cleanValue = AvroTranscodingHelper.toJdk(recordValue, () -> exampleValue);
			asMap.put(fieldName, cleanValue);
		}

		return asMap;
	}

	public static Map<String, ?> toJavaMap(IndexedRecord indexedRecord) {
		return toJavaMap(indexedRecord, Collections.emptyMap());
	}

	/**
	 * 
	 * @param exampleTypes
	 * @return a {@link Function} enabling transcoding in a {@link Stream}
	 */
	public static Function<GenericRecord, Map<String, ?>> toJavaMap(Map<? extends String, ?> exampleTypes) {
		return record -> toJavaMap(record, exampleTypes);
	}

	public static Function<GenericRecord, Map<String, ?>> toJavaMap() {
		return toJavaMap(Collections.emptyMap());
	}

	public static Function<Map<String, ?>, GenericRecord> toGenericRecord(Schema schema) {
		return map -> {
			GenericRecordBuilder record = new GenericRecordBuilder(schema);

			map.forEach((key, value) -> {
				Field field = schema.getField(key);

				if (field == null) {
					LOGGER.trace("We received a Map with a key which does not exist in the schema: " + key);
				} else {
					record.set(key, AvroTranscodingHelper.toAvro(field, value));
				}
			});

			return record.build();
		};
	}
}
