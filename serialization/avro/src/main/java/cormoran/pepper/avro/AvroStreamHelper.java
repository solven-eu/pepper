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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.IndexedRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cormoran.pepper.io.PepperByteStreamHelper;

/**
 * Helps working with Avro GenericRecords and java8 {@link Stream}
 * 
 * @author Benoit Lacelle
 *
 */
@Deprecated
public class AvroStreamHelper {
	protected static final Logger LOGGER = LoggerFactory.getLogger(AvroStreamHelper.class);

	protected AvroStreamHelper() {
		// hidden
	}

	@Deprecated
	public static Map<String, ?> toJavaMap(IndexedRecord indexedRecord, Map<? extends String, ?> exampleTypes) {
		return AvroTranscodingHelper.toJavaMap(indexedRecord, exampleTypes);
	}

	@Deprecated
	public static Map<String, ?> toJavaMap(IndexedRecord indexedRecord) {
		return AvroTranscodingHelper.toJavaMap(indexedRecord);
	}

	@Deprecated
	public static Function<GenericRecord, Map<String, ?>> toJavaMap(Map<? extends String, ?> exampleTypes) {
		return AvroTranscodingHelper.toJavaMap(exampleTypes);
	}

	@Deprecated
	public static Function<GenericRecord, Map<String, ?>> toJavaMap() {
		return AvroTranscodingHelper.toJavaMap();
	}

	@Deprecated
	public static Function<Map<String, ?>, GenericRecord> toGenericRecord(Schema schema) {
		return AvroTranscodingHelper.toGenericRecord(schema);
	}

	/**
	 * This is useful for tests, as it enable retrieving the avro binary through an InputStream
	 * 
	 * @param rowsToWrite
	 *            the stream of {@link GenericRecord} to write
	 * @param executor
	 *            an Async {@link Executor} supplier. It MUST not be synchronous else the underlying PipedInputStream
	 *            may dead-lock waiting for the PipedInputStream to be filled
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public static InputStream toInputStream(Stream<? extends GenericRecord> rowsToWrite, Supplier<Executor> executor)
			throws IOException {
		return PepperByteStreamHelper.toInputStream(os -> toOuputStream(rowsToWrite, os), executor);
	}

	/**
	 * The {@link OutputStream} should be pumped asynchronously, else this call may lock when OutputStream buffer is
	 * filled. Beware the pumping should be triggered BEFORE running this command
	 * 
	 * @param rowsToWrite
	 * @param outputStream
	 * @throws IOException
	 */
	@Deprecated
	public static long toOuputStream(Stream<? extends GenericRecord> rowsToWrite, OutputStream outputStream)
			throws IOException {
		return new AvroStreamFactory().serialize(outputStream, rowsToWrite);
	}

	@Deprecated
	public static Stream<GenericRecord> toGenericRecord(InputStream inputStream) throws IOException {
		return new AvroBytesToStream().stream(inputStream);
	}
}
