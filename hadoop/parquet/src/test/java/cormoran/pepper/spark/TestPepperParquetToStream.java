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

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

import cormoran.pepper.avro.AvroTranscodingHelper;
import cormoran.pepper.parquet.ParquetStreamFactory;

public class TestPepperParquetToStream {
	@Test
	public void testCacheDefaultConfiguration() {
		Configuration config = ParquetStreamFactory.cloneDefaultConfiguration();

		Field field = ReflectionUtils.findField(Configuration.class, "properties", Properties.class);

		ReflectionUtils.makeAccessible(field);

		Properties p = (Properties) ReflectionUtils.getField(field, config);

		// Check the properties is already initialized with default configuration
		Assert.assertNotNull(p);
	}

	@Test
	public void testEmptyListNoTarget() {
		Assert.assertFalse(AvroTranscodingHelper.toPrimitiveArray(null, Arrays.asList()).isPresent());
	}

	@Test
	public void testListDoubleToFloat() {
		float listElement = 1F;
		Assert.assertArrayEquals(new double[] { 1D },
				(double[]) AvroTranscodingHelper.toPrimitiveArray(new double[0], Arrays.asList(listElement)).get(),
				0.001D);
	}

	@Test
	public void testListFloatToDouble() {
		Double listElement = 1D;
		Assert.assertArrayEquals(new float[] { 1F },
				(float[]) AvroTranscodingHelper.toPrimitiveArray(new float[0], Arrays.asList(listElement)).get(),
				0.001F);
	}
}
