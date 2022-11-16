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
package eu.solven.pepper.buffer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.lang.management.ManagementFactory;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Optional;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Ints;

import eu.solven.pepper.logging.PepperLogHelper;
import eu.solven.pepper.memory.IPepperMemoryConstants;

/**
 * Helpers related to Buffers. Typically enable quick and easy allocating of a ByteBuffer over a blank memory mapped
 * file
 *
 * @author Benoit Lacelle
 *
 */
@Beta
public class PepperBufferHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperBufferHelper.class);

	@SuppressWarnings("PMD.MutableStaticState")
	@VisibleForTesting
	protected static boolean forceNoSpaceDisk = false;
	@SuppressWarnings("PMD.MutableStaticState")
	@VisibleForTesting
	protected static boolean forceNoHeap = false;

	protected PepperBufferHelper() {
		// hidden
	}

	public static CloseableIntBuffer makeIntBuffer(int nbIntegers) throws IOException {
		if (nbIntegers < 0) {
			throw new IllegalArgumentException("Can not allocate a buffer with a negative size");
		}

		long targetNbBytes = IPepperMemoryConstants.INT * nbIntegers;

		if (targetNbBytes > Integer.MAX_VALUE) {
			throw new IllegalStateException("Should rely on .makeIntLargeBuffer");
		}

		Optional<File> tmpFile = prepareIntArrayInFile(".IntArray1NWriter", targetNbBytes);

		if (tmpFile.isPresent()) {
			// FileChannel can be closed as "mapping, once established, is not dependent upon the file channel that was
			// used to create it". See FileChannel.map
			try (RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile.get(), "rw");
					FileChannel fc = randomAccessFile.getChannel()) {
				long fileChannelSize = fc.size();

				// https://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java
				MappedByteBuffer mappedByteBuffer = fc.map(FileChannel.MapMode.READ_WRITE, 0, fileChannelSize);

				return new CloseableIntBuffer(mappedByteBuffer);
			}
		} else {
			long availableHeap = getAvailableHeap();

			if (availableHeap < targetNbBytes) {
				// TODO: Try allocating in direct memory
				throw new IllegalStateException("Not enough disk-space nor memory");
			}

			try {
				int[] array = new int[nbIntegers];

				// Log the switch to heap only if the allocation in the heap succeeded
				LOGGER.warn("The disk seems full, allocating in heap");

				return new CloseableIntBuffer(IntBuffer.wrap(array));
			} catch (OutOfMemoryError oomError) {
				LOGGER.error("There is neither enough spaceDisk nor heap left for " + nbIntegers + " ints", oomError);

				throw oomError;
			}
		}
	}

	@Beta
	public static CloseableCompositeIntBuffer makeIntLargeBuffer(int nbIntegers) throws IOException {
		if (nbIntegers < 0) {
			throw new IllegalArgumentException("Can not allocate a buffer with a negative size");
		}

		long targetNbBytes = IPepperMemoryConstants.INT * nbIntegers;

		Optional<File> tmpFile = prepareIntArrayInFile(".IntArray1NWriter", targetNbBytes);

		if (tmpFile.isPresent()) {
			// FileChannel can be closed as "mapping, once established, is not dependent upon the file channel that was
			// used to create it". See FileChannel.map
			try (RandomAccessFile randomAccessFile = new RandomAccessFile(tmpFile.get(), "rw");
					FileChannel fc = randomAccessFile.getChannel()) {

				// https://stackoverflow.com/questions/8076472/why-does-filechannel-map-take-up-to-integer-max-value-of-data
				long fileChannelSize = fc.size();

				if (fileChannelSize == 0) {
					return new CloseableCompositeIntBuffer(IntBuffer.wrap(new int[0]));
				}

				int nbArrays = Ints.saturatedCast((fileChannelSize - 1) / Integer.MAX_VALUE) + 1;
				MappedByteBuffer[] array = IntStream.range(0, nbArrays).mapToObj(b -> {
					try {
						long start = Math.multiplyExact(b, Integer.MAX_VALUE);
						long end = Math.min(Integer.MAX_VALUE, fileChannelSize - start);

						// https://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java
						return fc.map(FileChannel.MapMode.READ_WRITE, start, end);
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}).toArray(MappedByteBuffer[]::new);

				return new CloseableCompositeIntBuffer(array);
			}
		} else {
			long availableHeap = getAvailableHeap();

			if (availableHeap < targetNbBytes) {
				// TODO: Try allocating in direct memory
				throw new IllegalStateException("Not enough disk-space nor memory");
			}

			try {
				int[] array = new int[nbIntegers];

				// Log the switch to heap only if the allocation in the heap succeeded
				LOGGER.warn("The disk seems full, allocating in heap");

				return new CloseableCompositeIntBuffer(IntBuffer.wrap(array));
			} catch (OutOfMemoryError oomError) {
				LOGGER.error("There is neither enough spaceDisk nor heap left for " + nbIntegers + " ints", oomError);

				throw oomError;
			}
		}
	}

	private static long getAvailableHeap() {
		if (forceNoHeap) {
			return 0;
		} else {
			long maxHeap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
			long usedHeap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();

			// Ensure positive availableHeap in case of GC happening between used and max
			return Math.max(0, maxHeap - usedHeap);
		}
	}

	private static Optional<File> prepareIntArrayInFile(String suffix, long targetNbBytes) throws IOException {
		File tmpFile = File.createTempFile("mat", suffix);
		// We do not need the file to survive the JVM as the goal is just to spare heap
		tmpFile.deleteOnExit();

		long freeSpace = getFreeSpace(tmpFile);
		if (freeSpace < targetNbBytes) {
			LOGGER.debug("There is only {} disk left while requesting for {}",
					PepperLogHelper.humanBytes(freeSpace),
					PepperLogHelper.humanBytes(targetNbBytes));
			return Optional.empty();
		}

		// https://stackoverflow.com/questions/27570052/allocate-big-file
		try (RandomAccessFile out = new RandomAccessFile(tmpFile, "rw")) {
			// This may fail with an OutOfDiskSpace IOException
			out.setLength(targetNbBytes);
		}
		return Optional.of(tmpFile);
	}

	private static long getFreeSpace(File tmpFile) {
		if (forceNoSpaceDisk) {
			return 0;
		} else {
			return tmpFile.getFreeSpace();
		}
	}

}
