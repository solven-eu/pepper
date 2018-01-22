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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.Float4Vector;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarBinaryVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.ipc.ArrowFileReader;
import org.apache.arrow.vector.ipc.message.ArrowBlock;
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

	@Override
	public Stream<Map<String, ?>> stream(InputStream inputStream) throws IOException {
		ArrowFileReader arrowFileReader =
				new ArrowFileReader(new ByteArrayReadableSeekableByteChannel(ByteStreams.toByteArray(inputStream)),
						new RootAllocator(Integer.MAX_VALUE));

		VectorSchemaRoot root = arrowFileReader.getVectorSchemaRoot();

		List<ArrowBlock> arrowBlocks = arrowFileReader.getRecordBlocks();
		return arrowBlocks.stream().onClose(() -> {
			try {
				arrowFileReader.close();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}).flatMap(rbBlock -> {
			try {
				if (!arrowFileReader.loadRecordBatch(rbBlock)) {
					throw new IOException("Expected to read record batch");
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			return IntStream.range(0, root.getRowCount()).mapToObj(rowIndex -> {
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
					default:
						throw new RuntimeException(" MinorType " + mt);
					}
				}

				return asMap;
			});
		});
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

	private void showVarBinaryAccessor(FieldVector fx, int rowIndex, Map<String, Object> asMap) {
		VarBinaryVector intVector = ((VarBinaryVector) fx);
		if (!intVector.isNull(rowIndex)) {
			byte[] value = intVector.get(rowIndex);

			asMap.put(fx.getField().getName(), value);
		}
	}

}
