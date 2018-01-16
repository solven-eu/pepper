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

import static org.apache.arrow.vector.types.FloatingPointPrecision.SINGLE;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.apache.arrow.vector.BigIntVector;
import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.Float4Vector;
import org.apache.arrow.vector.IntVector;
import org.apache.arrow.vector.VarBinaryVector;
import org.apache.arrow.vector.VectorSchemaRoot;
import org.apache.arrow.vector.dictionary.DictionaryProvider;
import org.apache.arrow.vector.ipc.ArrowFileWriter;
import org.apache.arrow.vector.types.pojo.ArrowType;
import org.apache.arrow.vector.types.pojo.Field;
import org.apache.arrow.vector.types.pojo.FieldType;
import org.apache.arrow.vector.types.pojo.Schema;

import com.google.common.collect.ImmutableList;

import cormoran.pepper.io.PepperFileHelper;
import cormoran.pepper.stream.PepperStreamHelper;

public class ArrowStreamHelper {
	protected ArrowStreamHelper() {
		// hidden
	}

	private static Schema makeSchema() {
		ImmutableList.Builder<Field> childrenBuilder = ImmutableList.builder();
		childrenBuilder.add(new Field("int", FieldType.nullable(new ArrowType.Int(32, true)), null));
		childrenBuilder.add(new Field("long", FieldType.nullable(new ArrowType.Int(64, true)), null));
		childrenBuilder.add(new Field("binary", FieldType.nullable(new ArrowType.Binary()), null));
		childrenBuilder.add(new Field("double", FieldType.nullable(new ArrowType.FloatingPoint(SINGLE)), null));
		return new Schema(childrenBuilder.build(), null);
	}

	public static void writeArrowToIS(Stream<Map<String, ?>> streamOfMap) throws IOException {
		// File arrowFile = validateFile(filename, false);
		// this.fileOutputStream = new FileOutputStream(arrowFile);
		Schema schema = makeSchema();

		BufferAllocator ra = new RootAllocator(Integer.MAX_VALUE);
		VectorSchemaRoot root = VectorSchemaRoot.create(schema, ra);
		DictionaryProvider.MapDictionaryProvider provider = new DictionaryProvider.MapDictionaryProvider();

		Path tmp = PepperFileHelper.createTempPath("ArrowStreamHelper", ".arrow", true);
		try (FileOutputStream fileOutputStream = new FileOutputStream(tmp.toFile());
				ArrowFileWriter arrowFileWriter = new ArrowFileWriter(root, provider, fileOutputStream.getChannel())) {
			try {
				arrowFileWriter.start();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}

			PepperStreamHelper.consumeByPartition(streamOfMap, c -> {
				root.setRowCount(c.size());

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
					default:
						throw new RuntimeException(" Not supported yet type: " + vector.getMinorType());
					}
				}
				try {
					arrowFileWriter.writeBatch();
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}, 1024);
			try {
				arrowFileWriter.end();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

	private static void writeFieldInt(FieldVector fieldVector, Collection<Map<String, ?>> c) {
		IntVector intVector = (IntVector) fieldVector;
		intVector.setInitialCapacity(c.size());
		intVector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				intVector.setNull(currentIndex);
			} else {
				intVector.setSafe(currentIndex, 1, (int) value);
			}
		});

		// how many are set
		fieldVector.setValueCount(c.size());
	}

	private static void writeFieldLong(FieldVector fieldVector, Collection<Map<String, ?>> c) {
		BigIntVector bigIntVector = (BigIntVector) fieldVector;
		bigIntVector.setInitialCapacity(c.size());
		bigIntVector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				bigIntVector.setNull(currentIndex);
			} else {
				bigIntVector.setSafe(currentIndex, 1, (long) value);
			}
		});

		// how many are set
		bigIntVector.setValueCount(c.size());
	}

	private static void writeFieldVarBinary(FieldVector fieldVector, Collection<Map<String, ?>> c) {
		VarBinaryVector varBinaryVector = (VarBinaryVector) fieldVector;
		varBinaryVector.setInitialCapacity(c.size());
		varBinaryVector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				varBinaryVector.setNull(currentIndex);
			} else {
				byte[] bytes = (byte[]) value;

				varBinaryVector.setIndexDefined(currentIndex);
				varBinaryVector.setValueLengthSafe(currentIndex, bytes.length);
				varBinaryVector.setSafe(currentIndex, bytes);
			}
		});

		// how many are set
		varBinaryVector.setValueCount(c.size());
	}

	private static void writeFieldFloat4(FieldVector fieldVector, Collection<Map<String, ?>> c) {
		Float4Vector float4Vector = (Float4Vector) fieldVector;
		float4Vector.setInitialCapacity(c.size());
		float4Vector.allocateNew();

		AtomicInteger index = new AtomicInteger();
		c.forEach(m -> {
			int currentIndex = index.getAndIncrement();

			Object value = m.get(fieldVector.getField().getName());
			if (value == null) {
				float4Vector.setNull(currentIndex);
			} else {
				float4Vector.setSafe(currentIndex, 1, (int) value);
			}
		});

		// how many are set
		float4Vector.setValueCount(c.size());
	}
}
