/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.arrow;

import java.util.Map;

import org.apache.arrow.vector.types.FloatingPointPrecision;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;

import com.google.common.collect.ImmutableList;

import eu.solven.pepper.logging.PepperLogHelper;

/**
 * Helps generating a schema for Avro format given a Map with example types as values
 *
 * @author Benoit Lacelle
 *
 */
public class ArrowStreamHelper {

	private static final int INT_BIT_WIDTH = 32;
	private static final int LONG_BIT_WIDTH = INT_BIT_WIDTH * 2;

	protected ArrowStreamHelper() {
		// hidden
	}

	public static Schema guessSchema(Map<String, ?> exampleMap) {
		ImmutableList.Builder<Field> fieldsBuilder = ImmutableList.builder();

		exampleMap.forEach((fieldName, fieldValue) -> {
			if (fieldValue instanceof Integer) {
				fieldsBuilder
						.add(new Field(fieldName, FieldType.nullable(new ArrowType.Int(INT_BIT_WIDTH, true)), null));
			} else if (fieldValue instanceof Long) {
				fieldsBuilder
						.add(new Field(fieldName, FieldType.nullable(new ArrowType.Int(LONG_BIT_WIDTH, true)), null));
			} else if (fieldValue instanceof byte[]) {
				fieldsBuilder.add(new Field(fieldName, FieldType.nullable(new ArrowType.Binary()), null));
			} else if (fieldValue instanceof Float) {
				fieldsBuilder.add(new Field(fieldName,
						FieldType.nullable(new ArrowType.FloatingPoint(FloatingPointPrecision.SINGLE)),
						null));
			} else if (fieldValue instanceof Double) {
				fieldsBuilder.add(new Field(fieldName,
						FieldType.nullable(new ArrowType.FloatingPoint(FloatingPointPrecision.DOUBLE)),
						null));
			} else if (fieldValue instanceof CharSequence) {
				fieldsBuilder.add(new Field(fieldName, FieldType.nullable(new ArrowType.Utf8()), null));
			} else {
				throw new IllegalArgumentException(
						"We can not handle " + PepperLogHelper.getObjectAndClass(fieldValue));
			}
		});

		return new Schema(fieldsBuilder.build(), null);
	}
}
