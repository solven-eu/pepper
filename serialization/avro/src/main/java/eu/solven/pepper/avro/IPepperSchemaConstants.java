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
package eu.solven.pepper.avro;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Some constants used when converting various Schema (Avro, Parquet, Arrow, ...) to an example Map
 *
 * @author Benoit Lacelle
 *
 */
@SuppressFBWarnings("MS_OOI_PKGPROTECT")
public interface IPepperSchemaConstants {
	/**
	 * Typically used as key in {@link Map}
	 */
	String SOME_KEY_STRING = "someKeyString";

	/**
	 * Used for String field or {@link String} value in a {@link Map}
	 */
	String SOME_STRING = "someString";

	float SOME_FLOAT = 1.2F;

	float[] SOME_FLOAT_ARRAY = { SOME_FLOAT, SOME_FLOAT * 2 };

	boolean SOME_BOOLEAN = true;

	double SOME_DOUBLE = 123.456D;

	int SOME_INT = 456;

	long SOME_LONG = 456_789L;

	LocalDate SOME_LOCALDATE = LocalDate.now();

	List<?> SOME_LIST = ImmutableList.of(SOME_STRING,
			SOME_BOOLEAN,
			SOME_FLOAT,
			SOME_FLOAT_ARRAY,
			SOME_DOUBLE,
			SOME_INT,
			SOME_LONG,
			SOME_LOCALDATE);
}
