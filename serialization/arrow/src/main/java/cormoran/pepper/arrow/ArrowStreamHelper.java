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
package cormoran.pepper.arrow;

import java.util.Map;

import org.apache.arrow.vector.types.FloatingPointPrecision;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;

import com.google.common.collect.ImmutableList;

import cormoran.pepper.logging.PepperLogHelper;

/**
 * Helps generating a schema for Avro format given a Map with example types as values
 * 
 * @author Benoit Lacelle
 *
 */
public class ArrowStreamHelper {

	protected ArrowStreamHelper() {
		// hidden
	}

	public static Schema guessSchema(Map<String, ?> exampleMap) {
		ImmutableList.Builder<Field> childrenBuilder = ImmutableList.builder();

		exampleMap.forEach((fieldName, fieldValue) -> {
			if (fieldValue instanceof Integer) {
				childrenBuilder.add(new Field(fieldName, FieldType.nullable(new ArrowType.Int(32, true)), null));
			} else if (fieldValue instanceof Long) {
				childrenBuilder.add(new Field(fieldName, FieldType.nullable(new ArrowType.Int(64, true)), null));
			} else if (fieldValue instanceof byte[]) {
				childrenBuilder.add(new Field(fieldName, FieldType.nullable(new ArrowType.Binary()), null));
			} else if (fieldValue instanceof Float) {
				childrenBuilder.add(new Field(fieldName,
						FieldType.nullable(new ArrowType.FloatingPoint(FloatingPointPrecision.SINGLE)),
						null));
			} else if (fieldValue instanceof Double) {
				childrenBuilder.add(new Field(fieldName,
						FieldType.nullable(new ArrowType.FloatingPoint(FloatingPointPrecision.DOUBLE)),
						null));
			} else if (fieldValue instanceof CharSequence) {
				childrenBuilder.add(new Field(fieldName, FieldType.nullable(new ArrowType.Utf8()), null));
			} else {
				throw new IllegalArgumentException(
						"We can not handle " + PepperLogHelper.getObjectAndClass(fieldValue));
			}
		});

		return new Schema(childrenBuilder.build(), null);
	}
}
