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
package cormoran.pepper.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

public class ITTransmitInputStreamInObjectInput {

	// Ensure we do not leak the ExecutorService used to transfer bytes
	@Test
	public void testNoLeak() throws IOException, ClassNotFoundException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		// Keep ImmutableMap for Serializability
		ImmutableMap<String, String> serializable = ImmutableMap.of("k", "v");

		// The leak is triggered by transmitting InputStream
		try (ObjectOutputStream o = new ObjectOutputStream(baos)) {
			PepperObjectInputHelper.writeInputStream(o,
					new ByteArrayInputStream(PepperSerializationHelper.toBytes(serializable)));
		}

		// Get the bytes once for all
		byte[] byteArray = baos.toByteArray();

		// Process in parallel to stress even more the classes
		IntStream.range(0, 100 * 1000).parallel().unordered().forEach(i -> {
			try (ObjectInput ois = PepperObjectInputHelper
					.wrapToHandleInputStream(new ObjectInputStream(new ByteArrayInputStream(byteArray)))) {
				InputStream is = (InputStream) ois.readObject();

				Assert.assertEquals(serializable, PepperSerializationHelper.fromBytes(ByteStreams.toByteArray(is)));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
