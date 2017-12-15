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
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.avro.Schema.Field;
import org.apache.avro.Schema.Type;
import org.apache.avro.generic.GenericData;
import org.apache.avro.util.Utf8;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Floats;

import cormoran.pepper.io.PepperSerializationHelper;

/**
 * Helps converting avro records to standard Java objects
 * 
 * @author Benoit Lacelle
 *
 */
public class AvroFieldHelper {
	protected AvroFieldHelper() {
		// hidden
	}

	public static Object cleanValue(Object value, Supplier<?> exampleValue) {
		if (value == null) {
			return null;
		}

		if (value instanceof Utf8) {
			// A String is more vanilla than Utf8, even if the performance penalty may be huge
			value = value.toString();
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
			}
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

}
