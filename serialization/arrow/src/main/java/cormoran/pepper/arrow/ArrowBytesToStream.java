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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.Float4Vector;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarBinaryVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.ArrowReader;
import org.apache.arrow.vector.ipc.ArrowStreamReader;
import org.apache.arrow.vector.types.Types;
import org.apache.arrow.vector.util.ByteArrayReadableSeekableByteChannel;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import cormoran.pepper.io.IBinaryToStream;

/**
 * Helps converting an InputStream to a Stream of {@link GenericRecord}
 * 
 * @author Benoit Lacelle
 *
 */
// https://github.com/animeshtrivedi/ArrowExample/blob/master/src/main/java/com/github/animeshtrivedi/arrowexample/ArrowRead.java
public class ArrowBytesToStream implements IBinaryToStream<Map<String, ?>> {
	protected static final Logger LOGGER = LoggerFactory.getLogger(ArrowBytesToStream.class);

	public Stream<Map<String, ?>> stream(boolean isFile, SeekableByteChannel channel) throws IOException {
		ArrowReader arrowReader;

		if (isFile) {
			arrowReader = new ArrowFileReader(channel, new RootAllocator(Integer.MAX_VALUE));
		} else {
			arrowReader = new ArrowStreamReader(channel, new RootAllocator(Integer.MAX_VALUE));
		}

		VectorSchemaRoot root = arrowReader.getVectorSchemaRoot();

		return StreamSupport.stream(new AbstractSpliterator<Stream<Map<String, ?>>>(Long.MAX_VALUE, 0) {

			@Override
			public boolean tryAdvance(Consumer<? super Stream<Map<String, ?>>> action) {
				try {
					if (!arrowReader.loadNextBatch()) {
						return false;
					}

					int blockRowCounrt = root.getRowCount();

					Stream<Map<String, ?>> result = IntStream.range(0, blockRowCounrt).mapToObj(rowIndex -> {
						List<FieldVector> fieldVector = root.getFieldVectors();

						Map<String, Object> asMap = new HashMap<>();
						for (int j = 0; j < fieldVector.size(); j++) {
							Types.MinorType mt = fieldVector.get(j).getMinorType();
							switch (mt) {
							case INT:
								showIntAccessor(fieldVector.get(j), rowIndex, asMap);
								break;
							case BIGINT:
								showBigIntAccessor(fieldVector.get(j), rowIndex, asMap);
								break;
							case VARBINARY:
								showVarBinaryAccessor(fieldVector.get(j), rowIndex, asMap);
								break;
							case FLOAT4:
								showFloat4Accessor(fieldVector.get(j), rowIndex, asMap);
								break;
							case FLOAT8:
								showFloat8Accessor(fieldVector.get(j), rowIndex, asMap);
								break;
							case VARCHAR:
								showVarcharAccessor(fieldVector.get(j), rowIndex, asMap);
								break;
							default:
								throw new RuntimeException(" MinorType " + mt);
							}
						}

						return asMap;
					});

					// Accept the whole Stream sa next block. We will later rely on .flatMap
					action.accept(result);

					return true;

				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

		}, false).onClose(() -> {
			try {
				arrowReader.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}).flatMap(s -> s);
	}

	@Override
	public Stream<Map<String, ?>> stream(InputStream inputStream) throws IOException {
		// TODO: Do we really need to load all bytes in memory?
		ByteArrayReadableSeekableByteChannel in =
				new ByteArrayReadableSeekableByteChannel(ByteStreams.toByteArray(inputStream));

		return stream(false, in);
	}

	private void showIntAccessor(FieldVector fx, int rowIndex, Map<String, Object> asMap) {
		IntVector intVector = ((IntVector) fx);
		if (!intVector.isNull(rowIndex)) {
			int value = intVector.get(rowIndex);

			asMap.put(fx.getField().getName(), value);
		}
	}

	private void showBigIntAccessor(FieldVector fx, int rowIndex, Map<String, Object> asMap) {
		BigIntVector intVector = ((BigIntVector) fx);
		if (!intVector.isNull(rowIndex)) {
			long value = intVector.get(rowIndex);

			asMap.put(fx.getField().getName(), value);
		}
	}

	private void showFloat4Accessor(FieldVector fx, int rowIndex, Map<String, Object> asMap) {
		Float4Vector intVector = ((Float4Vector) fx);
		if (!intVector.isNull(rowIndex)) {
			float value = intVector.get(rowIndex);

			asMap.put(fx.getField().getName(), value);
		}
	}

	private void showFloat8Accessor(FieldVector fx, int rowIndex, Map<String, Object> asMap) {
		Float8Vector intVector = ((Float8Vector) fx);
		if (!intVector.isNull(rowIndex)) {
			double value = intVector.get(rowIndex);

			asMap.put(fx.getField().getName(), value);
		}
	}

	private void showVarcharAccessor(FieldVector fx, int rowIndex, Map<String, Object> asMap) {
		VarCharVector intVector = ((VarCharVector) fx);
		if (!intVector.isNull(rowIndex)) {
			String value = new String(intVector.get(rowIndex), StandardCharsets.UTF_8);

			asMap.put(fx.getField().getName(), value);
		}
	}

	private void showVarBinaryAccessor(FieldVector fx, int rowIndex, Map<String, Object> asMap) {
		VarBinaryVector intVector = ((VarBinaryVector) fx);
		if (!intVector.isNull(rowIndex)) {
			byte[] value = intVector.get(rowIndex);

			asMap.put(fx.getField().getName(), value);
		}
	}

	public Stream<Map<String, ?>> stream(File file) throws FileNotFoundException, IOException {
		FileInputStream fis = new FileInputStream(file);
		return stream(true, fis.getChannel()).onClose(() -> {
			try {
				fis.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

}
