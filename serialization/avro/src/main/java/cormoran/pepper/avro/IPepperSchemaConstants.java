package cormoran.pepper.avro;

import java.time.LocalDate;
import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * Some constants used when converting various Schema (Avro, Parquet, Arrow, ...) to an example Map
 * 
 * @author Benoit Lacelle
 *
 */
public interface IPepperSchemaConstants {
	String SOME_STRING = "someString";

	float SOME_FLOAT = 1.2F;
	float[] SOME_FLOAT_ARRAY = new float[] { SOME_FLOAT, SOME_FLOAT * 2 };

	double SOME_DOUBLE = 123.456D;

	int SOME_INT = 456;

	long SOME_LONG = 456789;

	LocalDate SOME_LOCALDATE = LocalDate.now();

	List<?> SOME_LIST = ImmutableList
			.of(SOME_STRING, SOME_FLOAT, SOME_FLOAT_ARRAY, SOME_DOUBLE, SOME_INT, SOME_LONG, SOME_LOCALDATE);
}
