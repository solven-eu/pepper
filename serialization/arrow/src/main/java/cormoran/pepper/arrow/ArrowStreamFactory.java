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
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.Float4Vector;
import org.apache.arrow.vector.Float8Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarBinaryVector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.dictionary.DictionaryProvider;
import org.apache.arrow.vector.holders.VarCharHolder;
import org.apache.arrow.vector.ipc.ArrowFileWriter;
import org.apache.arrow.vector.ipc.ArrowStreamWriter;
import org.apache.arrow.vector.ipc.ArrowWriter;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.Schema;

import cormoran.pepper.stream.PepperStreamHelper;
import io.netty.buffer.ArrowBuf;

public class ArrowStreamFactory implements IArrowStreamFactory {
	private static final int PARTITION_TARGET_SIZE = 1024;

	@Override
	public Stream<Map<String, ?>> stream(URI uri) throws IOException {
		if (uri.getScheme().equals("file")) {
			return new ArrowBytesToStream().stream(Paths.get(uri).toFile());
		} else {
			return new ArrowBytesToStream().stream(uri.toURL().openStream());
		}
	}

	@Override
	public long serialize(WritableByteChannel byteChannel,
			boolean outputIsFile,
			Schema schema,
			Stream<? extends Map<String, ?>> rowsToWrite) throws IOException {
		BufferAllocator ra = new RootAllocator(Integer.MAX_VALUE);
		VectorSchemaRoot root = VectorSchemaRoot.create(schema, ra);
		DictionaryProvider.MapDictionaryProvider provider = new DictionaryProvider.MapDictionaryProvider();

		AtomicLong nbRows = new AtomicLong();

		if (outputIsFile) {
			try (ArrowFileWriter arrowFileWriter = new ArrowFileWriter(root, provider, byteChannel)) {
				writeData(rowsToWrite, root, nbRows, arrowFileWriter);
				try {
					arrowFileWriter.end();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		} else {
			try (ArrowStreamWriter arrowFileWriter = new ArrowStreamWriter(root, provider, byteChannel)) {
				writeData(rowsToWrite, root, nbRows, arrowFileWriter);
				try {
					arrowFileWriter.end();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}
		}

		return nbRows.get();
	}

	private static void writeData(Stream<? extends Map<String, ?>> streamOfMap,
			VectorSchemaRoot root,
			AtomicLong nbRows,
			ArrowWriter arrowFileWriter) {
		try {
			arrowFileWriter.start();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		PepperStreamHelper.consumeByPartition(streamOfMap, c -> {
			int partritionRowCount = c.size();
			root.setRowCount(partritionRowCount);
			nbRows.addAndGet(partritionRowCount);

			for (Field field : root.getSchema().getFields()) {
				FieldVector vector = root.getVector(field.getName());
				switch (vector.getMinorType()) {
				case INT:
					writeFieldInt(vector, c);
					break;
				case BIGINT:
					writeFieldLong(vector, c);
					break;
				case VARBINARY:
					writeFieldVarBinary(vector, c);
					break;
				case FLOAT4:
					writeFieldFloat4(vector, c);
					break;
				case FLOAT8:
					writeFieldFloat8(vector, c);
					break;
				case VARCHAR:
					writeFieldVarchar(vector, c);
					break;
				default:
					throw new RuntimeException(" Not supported yet type: " + vector.getMinorType());
				}
			}
			try {
				arrowFileWriter.writeBatch();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}, PARTITION_TARGET_SIZE);
	}

	private static void writeFieldInt(FieldVector fieldVector, Collection<? extends Map<String, ?>> c) {
		IntVector vector = (IntVector) fieldVector;
		vector.setInitialCapacity(c.size());
		vector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				vector.setNull(currentIndex);
			} else {
				vector.setSafe(currentIndex, 1, (int) value);
			}
		});

		// how many are set
		fieldVector.setValueCount(c.size());
	}

	private static void writeFieldLong(FieldVector fieldVector, Collection<? extends Map<String, ?>> c) {
		BigIntVector vector = (BigIntVector) fieldVector;
		vector.setInitialCapacity(c.size());
		vector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				vector.setNull(currentIndex);
			} else {
				vector.setSafe(currentIndex, (long) value);
			}
		});

		// how many are set
		vector.setValueCount(c.size());
	}

	private static void writeFieldVarBinary(FieldVector fieldVector, Collection<? extends Map<String, ?>> c) {
		VarBinaryVector vector = (VarBinaryVector) fieldVector;
		vector.setInitialCapacity(c.size());
		vector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				vector.setNull(currentIndex);
			} else {
				byte[] bytes = (byte[]) value;

				vector.setIndexDefined(currentIndex);
				vector.setValueLengthSafe(currentIndex, bytes.length);
				vector.setSafe(currentIndex, bytes);
			}
		});

		// how many are set
		vector.setValueCount(c.size());
	}

	private static void writeFieldFloat4(FieldVector fieldVector, Collection<? extends Map<String, ?>> c) {
		Float4Vector vector = (Float4Vector) fieldVector;
		vector.setInitialCapacity(c.size());
		vector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				vector.setNull(currentIndex);
			} else {
				vector.setSafe(currentIndex, (float) value);
			}
		});

		// how many are set
		vector.setValueCount(c.size());
	}

	private static void writeFieldFloat8(FieldVector fieldVector, Collection<? extends Map<String, ?>> c) {
		Float8Vector vector = (Float8Vector) fieldVector;
		vector.setInitialCapacity(c.size());
		vector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				vector.setNull(currentIndex);
			} else {
				vector.setSafe(currentIndex, (double) value);
			}
		});

		// how many are set
		vector.setValueCount(c.size());
	}

	private static void writeFieldVarchar(FieldVector fieldVector, Collection<? extends Map<String, ?>> c) {
		VarCharVector vector = (VarCharVector) fieldVector;
		vector.setInitialCapacity(c.size());
		vector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				vector.setNull(currentIndex);
			} else {
				VarCharHolder varCharHolder = new VarCharHolder();

				byte[] bytes = value.toString().getBytes(StandardCharsets.UTF_8);

				ArrowBuf buffer = fieldVector.getAllocator().buffer(bytes.length);
				buffer.setBytes(0, bytes);
				varCharHolder.buffer = buffer;
				varCharHolder.start = 0;
				varCharHolder.end = bytes.length;

				vector.setSafe(currentIndex, varCharHolder);
			}
		});

		// how many are set
		vector.setValueCount(c.size());
	}
}
