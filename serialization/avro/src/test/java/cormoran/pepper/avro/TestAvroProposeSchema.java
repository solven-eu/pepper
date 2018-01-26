package cormoran.pepper.avro;

import java.time.LocalDate;

import org.apache.avro.Schema;
import org.junit.Assert;
import org.junit.Test;

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
