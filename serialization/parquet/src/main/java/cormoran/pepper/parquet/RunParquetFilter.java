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
package cormoran.pepper.parquet;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cormoran.pepper.io.PepperFileHelper;

/**
 * This will log a parquet file into the console
 * 
 * @author Benoit Lacelle
 *
 */
public class RunParquetFilter {

	protected static final Logger LOGGER = LoggerFactory.getLogger(RunParquetFilter.class);

	public static void main(String[] args) throws IOException {
		Path path = RunParquetVisualizer.getParquetFile(args);

		URI output;
		if (args.length >= 2) {
			output = Paths.get(args[1]).toUri();
		} else {
			output = PepperFileHelper.createTempPath("RunParquetFilter", ".parquet", false).toUri();
		}

		// Configure with '-Dpepper.limit=123'
		int limit = Integer.getInteger("pepper.limit", 10);

		ParquetBytesToStream parquetBytesToStream = new ParquetBytesToStream();

		LOGGER.info("About to read rows from {}", path);
		Stream<GenericRecord> streamToWrite =
				parquetBytesToStream.stream(path.toUri().toURL().openStream()).limit(limit);

		LOGGER.info("About to write rows to {}", output);
		new ParquetStreamFactory().serialize(output, streamToWrite);
	}
}
