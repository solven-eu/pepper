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

import java.time.LocalDate;
import java.util.Arrays;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class TestAvroProposeSchema implements IPepperSchemaConstants {
	@Test
	public void testProposeSchema_Double() {
		Double object = 123.456D;

		Schema schema = AvroSchemaHelper.proposeSchemaForValue(object);
		Object exampleValue = AvroSchemaHelper.exampleValue(schema);

		Assert.assertEquals(object.getClass(), exampleValue.getClass());
	}

	@Test
	public void testProposeSchema_null() {
		Schema schema = AvroSchemaHelper.proposeSchemaForValue(null);
		Object exampleValue = AvroSchemaHelper.exampleValue(schema);

		Assert.assertNull(exampleValue);
	}

	@Test
	public void testProposeSchema_Map() {
		// Should we return a schema for a Map, or for a record?
		Schema schema = AvroSchemaHelper.proposeSchemaForValue(ImmutableMap.of("someWeirdKey", "someWeirdValue"));
		Object exampleValue = AvroSchemaHelper.exampleValue(schema);

		Assert.assertEquals(ImmutableMap.of("key", "someString"), exampleValue);
	}

	@Test
	public void testProposeValue_Record() {
		Object exampleValue = AvroSchemaHelper.exampleValue(Schema.createRecord(Arrays
				.asList(new Schema.Field("someWeirdKey", Schema.create(Schema.Type.STRING), null, "someWeirdValue"))));

		Assert.assertEquals(ImmutableMap.of("someWeirdKey", "someString"), exampleValue);
	}

	@Test
	public void testProposeValue_Union() {
		Object exampleValue = AvroSchemaHelper
				.exampleValue(Schema.createUnion(Schema.create(Schema.Type.NULL), Schema.create(Schema.Type.STRING)));

		Assert.assertEquals("someString", exampleValue);
	}

	@Test
	public void testProposeValue_Array() {
		Object exampleValue = AvroSchemaHelper.exampleValue(Schema.createArray(Schema.create(Schema.Type.FLOAT)));

		Assert.assertEquals(Arrays.asList(1.2F), exampleValue);
	}

	@Test
	public void testProposeSchema_EACH() {
		SOME_LIST.forEach(object -> {
			Schema schema = AvroSchemaHelper.proposeSchemaForValue(object);
			Object exampleValue = AvroSchemaHelper.exampleValue(schema);

			if (object instanceof float[]) {
				// Given a schema with type BYTES, we did not have enough information to guess a float[]
				Assert.assertEquals(LocalDate.class, exampleValue.getClass());
			} else {
				Assert.assertEquals(object.getClass(), exampleValue.getClass());
			}
		});

	}
}
