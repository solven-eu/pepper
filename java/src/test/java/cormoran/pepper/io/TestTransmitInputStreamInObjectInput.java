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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;

import cormoran.pepper.memory.IPepperMemoryConstants;
import cormoran.pepper.thread.PepperExecutorsHelper;

public class TestTransmitInputStreamInObjectInput {

	@SuppressWarnings("resource")
	@Test
	public void testTransittingInputSteam_write_read_write_read_singleChunk()
			throws IOException, ClassNotFoundException {
		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream(pis);

		byte[] bytesFrance = PepperSerializationHelper.toBytes(ImmutableMap.of("k1", "v1"));
		byte[] bytesUs = PepperSerializationHelper.toBytes(ImmutableMap.of("k2", "v2"));

		Assert.assertTrue(bytesFrance.length < PepperObjectInputHelper.DEFAULT_CHUNK_SIZE);
		Assert.assertTrue(bytesUs.length < PepperObjectInputHelper.DEFAULT_CHUNK_SIZE);

		ObjectInputHandlingInputStream objectInput;
		try (ObjectOutputStream oos = new ObjectOutputStream(pos)) {
			// Write an InputStream
			PepperObjectInputHelper.writeInputStream(oos, new ByteArrayInputStream(bytesFrance));

			// Ensure everything is submitted as we will read the pipe in the same thread
			oos.flush();

			objectInput = new ObjectInputHandlingInputStream(new ObjectInputStream(pis));
			Object nextToRead = objectInput.readObject();

			Assert.assertNotNull(nextToRead);
			Assert.assertTrue(nextToRead instanceof InputStream);
			InputStream readIS = (InputStream) nextToRead;

			// The pipe could be open or close, depending on the speed on reading it. It would be certainly open ONLY if
			// the batch size is small enough so that multiple chunks have to be submitted (as here, we have just open
			// the stream, leading to only having read the first chunk)
			Awaitility.await().untilFalse(objectInput.pipedOutputStreamIsOpen);

			// Ensure we are retrieving the whole chunk
			byte[] transmitted = ByteStreams.toByteArray(readIS);
			Assert.assertArrayEquals(bytesFrance, transmitted);

			// We write a second block, but read it after closing ObjectOutputStream: the inputStream should remain
			// open
			PepperObjectInputHelper.writeInputStream(oos, new ByteArrayInputStream(bytesUs));
		}

		// ObjectOutputStream is closed: pipedOutputStreamIsOpen should end being switched to false
		Awaitility.await().untilFalse(objectInput.pipedOutputStreamIsOpen);

		// Check reading after ObjectOutputStream is closed
		{
			Object nextToRead = objectInput.readObject();

			Assert.assertNotNull(nextToRead);
			Assert.assertTrue(nextToRead instanceof InputStream);
			InputStream readIS = (InputStream) nextToRead;

			byte[] transmitted = ByteStreams.toByteArray(readIS);
			Assert.assertArrayEquals(bytesUs, transmitted);

			// Check there is no more bytes
			Assert.assertEquals(-1, readIS.read());
		}

	}

	@SuppressWarnings("resource")
	@Test
	public void testTransittingInputSteam_write_read_write_read_MultipleChunks()
			throws IOException, ClassNotFoundException {
		// We ensure a large buffer as we will have a large overhead because of chunks
		PipedInputStream pis = new PipedInputStream(IPepperMemoryConstants.MB_INT);
		PipedOutputStream pos = new PipedOutputStream(pis);

		byte[] bytesFrance = PepperSerializationHelper.toBytes(ImmutableMap.of("k1", "v1"));
		byte[] bytesUs = PepperSerializationHelper.toBytes(ImmutableMap.of("k2", "v2"));

		// We force very small chunks: very slow but good edge-case to test
		int chunkSize = 1;
		Assert.assertTrue(bytesFrance.length > chunkSize);
		Assert.assertTrue(bytesUs.length > chunkSize);

		ObjectInputHandlingInputStream objectInput;
		try (ObjectOutputStream oos = new ObjectOutputStream(pos)) {
			// Write an InputStream
			PepperObjectInputHelper.writeInputStream(oos, new ByteArrayInputStream(bytesFrance), chunkSize);

			// Ensure everything is submitted as we will read the pipe in the same thread
			oos.flush();

			objectInput = new ObjectInputHandlingInputStream(new ObjectInputStream(pis)) {
				@Override
				protected PipedInputStream makePipedInputStream() {
					// Ensure the IS can not be fully read in background pipe
					return new PipedInputStream(bytesFrance.length / 2);
				}
			};
			Object nextToRead = objectInput.readObject();

			Assert.assertNotNull(nextToRead);
			Assert.assertTrue(nextToRead instanceof InputStream);
			InputStream readIS = (InputStream) nextToRead;

			// We ensured publishing multiple chunks: the stream remains open as we have just transmitted the first one
			Assert.assertTrue(objectInput.pipedOutputStreamIsOpen.get());

			// Ensure we are retrieving the whole chunk
			byte[] transmitted = ByteStreams.toByteArray(readIS);
			Assert.assertArrayEquals(bytesFrance, transmitted);

			// We write a second block, but read it after closing ObjectOutputStream: the inputStream should remain
			// open
			PepperObjectInputHelper.writeInputStream(oos, new ByteArrayInputStream(bytesUs));
		}

		// ObjectOutputStream is closed: pipedOutputStreamIsOpen should end being switched to false
		Awaitility.await().untilFalse(objectInput.pipedOutputStreamIsOpen);

		// Check reading after ObjectOutputStream is closed
		{
			Object nextToRead = objectInput.readObject();

			Assert.assertNotNull(nextToRead);
			Assert.assertTrue(nextToRead instanceof InputStream);
			InputStream readIS = (InputStream) nextToRead;

			byte[] transmitted = ByteStreams.toByteArray(readIS);
			Assert.assertArrayEquals(bytesUs, transmitted);

			// Check there is no more bytes
			Assert.assertEquals(-1, readIS.read());
		}

	}

	@Test
	public void testTransittingInputSteam_write_write_read_read_singleChunk()
			throws IOException, ClassNotFoundException {
		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream(pis);

		byte[] bytesFrance = PepperSerializationHelper.toBytes(ImmutableMap.of("k1", "v1"));
		byte[] bytesUs = PepperSerializationHelper.toBytes(ImmutableMap.of("k2", "v2"));

		Assert.assertTrue(bytesFrance.length < PepperObjectInputHelper.DEFAULT_CHUNK_SIZE);
		Assert.assertTrue(bytesUs.length < PepperObjectInputHelper.DEFAULT_CHUNK_SIZE);

		try (ObjectOutputStream oos = new ObjectOutputStream(pos)) {
			// Write consecutively 2 inputStreams
			PepperObjectInputHelper.writeInputStream(oos, new ByteArrayInputStream(bytesFrance));
			PepperObjectInputHelper.writeInputStream(oos, new ByteArrayInputStream(bytesUs));
		}

		try (ObjectInputHandlingInputStream objectInput =
				new ObjectInputHandlingInputStream(new ObjectInputStream(pis));) {
			{
				Object nextToRead = objectInput.readObject();

				Assert.assertNotNull(nextToRead);
				Assert.assertTrue(nextToRead instanceof InputStream);
				InputStream readIS = (InputStream) nextToRead;

				// We have not close the ObjectOutputStream: the transmitter should remain open as next item could be
				// another ByteArrayMarker
				// Assert.assertTrue(objectInput.pipedOutputStreamIsOpen.get());
				Awaitility.await().untilFalse(objectInput.pipedOutputStreamIsOpen);

				// Ensure we are retrieving the whole chunk
				byte[] transmitted = ByteStreams.toByteArray(readIS);
				Assert.assertArrayEquals(bytesFrance, transmitted);
			}

			// Check reading after ObjectOutputStream is closed
			{
				Object nextToRead = objectInput.readObject();

				Assert.assertNotNull(nextToRead);
				Assert.assertTrue(nextToRead instanceof InputStream);
				InputStream readIS = (InputStream) nextToRead;

				byte[] transmitted = ByteStreams.toByteArray(readIS);
				Assert.assertArrayEquals(bytesUs, transmitted);

				// Check there is no more bytes
				Assert.assertEquals(-1, readIS.read());
			}
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testExceptionInIS() throws IOException, ClassNotFoundException {
		byte[] bytesFrance = PepperSerializationHelper.toBytes(ImmutableMap.of("k1", "v1"));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			// Write an InputStream: ensure it is split in multiple chunks
			PepperObjectInputHelper
					.writeInputStream(oos, new ByteArrayInputStream(bytesFrance), bytesFrance.length / 2);
		}

		ObjectInputHandlingInputStream objectInput;
		byte[] bytesToTransmit = baos.toByteArray();
		objectInput =
				new ObjectInputHandlingInputStream(new ObjectInputStream(new ByteArrayInputStream(bytesToTransmit))) {
					@Override
					protected PipedInputStream makePipedInputStream() {
						// Prevent all chunks to be pumped right away
						return new PipedInputStream(bytesFrance.length / 2);
					}
				};
		Object nextToRead = objectInput.readObject();

		Assert.assertNotNull(nextToRead);
		Assert.assertTrue(nextToRead instanceof InputStream);
		InputStream readIS = (InputStream) nextToRead;

		// Here, we have written only the first chunk in the IS
		{
			// Check we have read some byte BEFORE having done reading the pis
			Assert.assertTrue(objectInput.pipedOutputStreamIsOpen.get());

			// ERROR BLOCK: we corrupt the data, including data not submitted yet to Pipe
			ByteBuffer.wrap(bytesToTransmit).put(new byte[bytesToTransmit.length]);
		}

		// Ensure we are retrieving the whole chunk
		byte[] transmitted = ByteStreams.toByteArray(readIS);
		Assert.assertTrue(bytesFrance.length > transmitted.length);
		Awaitility.await().untilFalse(objectInput.pipedOutputStreamIsOpen);

		Exception exceptionToRethrow = objectInput.ouch.get();
		Assert.assertNotNull(exceptionToRethrow);

		try {
			objectInput.readObject();
			Assert.fail("Should have thrown");
		} catch (Exception e) {
			Assert.assertSame(exceptionToRethrow, e.getCause());
		}
	}

	@Test
	public void testExecutorServiceIsClosed() throws IOException, ClassNotFoundException {
		// We ensure a large buffer as we will have a large overhead because of chunks
		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream(pis);

		ObjectInputHandlingInputStream objectInput;
		try (ObjectOutputStream oos = new ObjectOutputStream(pos)) {
			objectInput = new ObjectInputHandlingInputStream(new ObjectInputStream(pis));
		}

		// By default not shutdown
		Assert.assertFalse(objectInput.inputStreamFiller.get().isShutdown());

		// Closing the OOS has shutdown the ES
		objectInput.close();
		Assert.assertTrue(objectInput.inputStreamFiller.get().isShutdown());
	}

	@Test
	public void testExecutorServiceIsClosed_noclose() throws IOException, ClassNotFoundException {
		// We ensure a large buffer as we will have a large overhead because of chunks
		PipedInputStream pis = new PipedInputStream();
		PipedOutputStream pos = new PipedOutputStream(pis);

		ObjectInputHandlingInputStream objectInput;
		try (ObjectOutputStream oos = new ObjectOutputStream(pos)) {
			objectInput = new ObjectInputHandlingInputStream(new ObjectInputStream(pis),
					PepperExecutorsHelper.newSingleThreadExecutor("testExecutorServiceIsClosed_noclose"),
					false);
		}

		// By default not shutdown
		Assert.assertFalse(objectInput.inputStreamFiller.get().isShutdown());

		// Closing the OOS has NOT shutdown the ES
		objectInput.close();
		Assert.assertFalse(objectInput.inputStreamFiller.get().isShutdown());
	}
}
