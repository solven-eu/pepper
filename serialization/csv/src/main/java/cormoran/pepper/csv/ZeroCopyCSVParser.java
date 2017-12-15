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
package cormoran.pepper.csv;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cormoran.pepper.primitive.Jdk9CharSequenceParsers;
import cormoran.pepper.primitive.PepperParserHelper;

/**
 * Default implementation for {@link IZeroCopyCSVParser}
 * 
 * @author Benoit Lacelle
 *
 */
public class ZeroCopyCSVParser implements IZeroCopyCSVParser {

	private final class Youpi implements IZeroCopyConsumer, Consumer<CharSequence> {
		private final List<String> pendingStrings;

		private Youpi(List<String> pendingStrings) {
			this.pendingStrings = pendingStrings;
		}

		@Override
		public void nextRowIsMissing() {
			pendingStrings.add("");
		}

		@Override
		public void nextRowIsInvalid(CharSequence charSequence) {
			pendingStrings.add(charSequence.toString());
		}

		@Override
		public void accept(CharSequence t) {
			pendingStrings.add(t.toString());
		}
	}

	protected static final Logger LOGGER = LoggerFactory.getLogger(ZeroCopyCSVParser.class);

	private static final int DEFAULT_BUFFER_SIZE = 1024;

	protected final int bufferSize;

	public ZeroCopyCSVParser() {
		bufferSize = DEFAULT_BUFFER_SIZE;
	}

	public ZeroCopyCSVParser(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	// Minimal memory consumption: bufferSize * (4(CharBuffer) + 4 (char[] buffer))
	@Override
	public void parse(Reader reader, char separator, List<IZeroCopyConsumer> consumers) throws IOException {
		consumers.stream().filter(Objects::nonNull).forEach(consumer -> {
			if (consumer instanceof IntConsumer) {
				LOGGER.trace("We like IntConsumer");
			} else if (consumer instanceof LongConsumer) {
				LOGGER.trace("We like LongConsumer");
			} else if (consumer instanceof DoubleConsumer) {
				LOGGER.trace("We like DoubleConsumer");
			} else if (consumer instanceof Consumer<?>) {
				LOGGER.trace("We hope it is a Consumer<CharSequence>");
			} else {
				throw new IllegalArgumentException("You need to be any java.util.function.*Consumer");
			}
		});

		IntFunction<IZeroCopyConsumer> indexToConsumer = index -> {
			if (index < consumers.size()) {
				return consumers.get(index);
			} else {
				return null;
			}
		};

		CharBuffer charBuffer = CharBuffer.allocate(bufferSize);
		CharBuffer nextValue = CharBuffer.allocate(bufferSize);

		// Do like we were at the end of the buffer
		charBuffer.position(charBuffer.limit());

		// We need to read at least once
		boolean moreToRead = true;

		int columnIndex = -1;
		// int firstValueCharIndex = -1;

		char[] buffer = new char[charBuffer.capacity()];

		while (moreToRead) {
			charBuffer.compact();

			// We do not use Reader.read(CharBuffer) as it would allocate a transient char[]
			int nbRead = reader.read(buffer, 0, Math.min(buffer.length, charBuffer.remaining()));
			if (nbRead > 0) {
				charBuffer.put(buffer, 0, nbRead);
			}

			if (nbRead == 0) {
				// Is it legal ? We may have 0 bytes if it is buffered but the buffer is not filled yet
				// Or is it a bug in our code? Or is it the buffer is too small?
				throw new IllegalStateException("Unable to read data");
			}
			charBuffer.flip();

			if (nbRead < 0) {
				moreToRead = false;
			}

			while (charBuffer.hasRemaining()) {
				// for (int charIndex = leftover; charIndex < charBuffer.limit(); charIndex++) {
				// Next char is the one not yet processed
				// int nextChar = charBuffer.get(charIndex);
				char nextChar = charBuffer.get();

				if (nextChar == separator) {
					// We are closing a column: publish the column content
					columnIndex = flushColumn(indexToConsumer, nextValue, columnIndex, true);
				} else if (nextChar == '\r' || nextChar == '\n') {
					columnIndex = flushColumn(indexToConsumer, nextValue, columnIndex, false);
					warnConsumersWithoutColumn(indexToConsumer, columnIndex, consumers.size());

					// Reset the columnIndex
					columnIndex = -1;
				} else {
					// We have an interesting character
					if (columnIndex < 0 && nextValue.position() == 0) {
						// First character of current row
						columnIndex++;
					}

					nextValue.put(nextChar);
				}
			}

			if (!moreToRead) {
				// We are at the end of the file
				columnIndex = flushColumn(indexToConsumer, nextValue, columnIndex, false);
				warnConsumersWithoutColumn(indexToConsumer, columnIndex, consumers.size());
			}
		}
	}

	public Stream<String[]> parseAsStringArrays(Reader reader, char separator) {
		CharBuffer charBuffer = CharBuffer.allocate(bufferSize);

		// Do like we were at the end of the buffer
		charBuffer.position(charBuffer.limit());

		// We need to read at least once
		AtomicBoolean moreToRead = new AtomicBoolean(true);

		AtomicInteger columnIndex = new AtomicInteger(0);

		final char[] buffer = new char[charBuffer.capacity()];

		CharBuffer nextValue = CharBuffer.allocate(bufferSize);

		// We have a single ArrayList: its capacity will increase each time we encounter a longest row (which is nice,
		// at will typically be nearly fully prepared on first row)
		List<String> pendingStrings = new LinkedList<>();

		IZeroCopyConsumer consumer = new Youpi(pendingStrings);

		IntFunction<IZeroCopyConsumer> indexToConsumer = index -> consumer;

		return StreamSupport.stream(new Spliterators.AbstractSpliterator<String[]>(Long.MAX_VALUE, 0) {

			@Override
			public boolean tryAdvance(Consumer<? super String[]> action) {
				boolean actionHasBeenTriggered = false;

				// Continue until there is bytes to process
				// Stop if no more bytes, or else if a row have been processed
				while ((moreToRead.get() || charBuffer.hasRemaining()) && !actionHasBeenTriggered) {
					fillBuffer(reader, charBuffer, moreToRead, buffer);

					while (charBuffer.hasRemaining() && !actionHasBeenTriggered) {
						actionHasBeenTriggered = processBuffer(separator,
								charBuffer,
								columnIndex,
								nextValue,
								pendingStrings,
								indexToConsumer,
								action);
					}

					if (!moreToRead.get() && !charBuffer.hasRemaining() && !actionHasBeenTriggered) {
						// We are at the end of the file
						// We have detected the beginning of an input and then encounter EOF: we have to flush the
						// column

						columnIndex.set(flushColumn(indexToConsumer, nextValue, columnIndex.get(), false));
						warnConsumersWithoutColumn(indexToConsumer, columnIndex.get(), -1);
					}
				}

				return actionHasBeenTriggered;
			}

			protected void fillBuffer(Reader reader,
					CharBuffer charBuffer,
					AtomicBoolean moreToRead,
					final char[] buffer) {
				if (moreToRead.get() && !charBuffer.hasRemaining()) {
					charBuffer.compact();

					// We do not use Reader.read(CharBuffer) as it would allocate a transient char[]
					int nbRead;
					try {
						nbRead = reader.read(buffer, 0, Math.min(buffer.length, charBuffer.remaining()));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
					if (nbRead > 0) {
						charBuffer.put(buffer, 0, nbRead);
					}

					if (nbRead == 0) {
						// Is it legal ? We may have 0 bytes if it is buffered but the buffer is not filled yet
						// Or is it a bug in our code? Or is it the buffer is too small?
						throw new IllegalStateException("Unable to read data");
					}
					charBuffer.flip();

					if (nbRead < 0) {
						moreToRead.set(false);
					}
				}
			}

			protected boolean processBuffer(char separator,
					CharBuffer charBuffer,
					AtomicInteger columnIndex,
					CharBuffer nextValue,
					List<String> pendingStrings,
					IntFunction<IZeroCopyConsumer> indexToConsumer,
					Consumer<? super String[]> action) {
				boolean actionHasBeenTriggered = false;

				// Next char is the one not yet processed
				char nextChar = charBuffer.get();

				if (nextChar == separator) {
					// We are closing a column: publish the column content
					columnIndex.set(flushColumn(indexToConsumer, nextValue, columnIndex.get(), true));
				} else if (nextChar == '\r' || nextChar == '\n') {
					columnIndex.set(flushColumn(indexToConsumer, nextValue, columnIndex.get(), false));

					warnConsumersWithoutColumn(indexToConsumer, columnIndex.get(), -1);

					if (!pendingStrings.isEmpty()) {
						if (action != null) {
							action.accept(pendingStrings.toArray(new String[pendingStrings.size()]));
						}
						pendingStrings.clear();
						actionHasBeenTriggered = true;
					}

					// Reset the columnIndex
					columnIndex.set(-1);
				} else {
					// We have an interesting character
					if (columnIndex.get() < 0 && nextValue.position() == 0) {
						// First character of current row
						columnIndex.incrementAndGet();
					}
					nextValue.put(nextChar);
				}
				return actionHasBeenTriggered;
			}
		}, false);
	}

	protected void warnConsumersWithoutColumn(IntFunction<IZeroCopyConsumer> consumers, int columnIndex, int maxIndex) {
		if (columnIndex < 0) {
			// empty row
			return;
		}

		for (int i = columnIndex; i < maxIndex; i++) {
			// Warn the consumers that will not receive any data
			// We have no constrain about the order in which consumers are notified for a given row
			IZeroCopyConsumer consumer = consumers.apply(i);
			if (consumer != null) {
				consumer.nextRowIsMissing();
			}
		}
	}

	protected int flushColumn(IntFunction<IZeroCopyConsumer> consumers,
			CharBuffer nextValue,
			final int flushedColumnIndex,
			boolean hasMoreSameRow) {
		if (flushedColumnIndex <= -1) {
			assert nextValue.position() == 0;

			if (hasMoreSameRow) {
				IZeroCopyConsumer consumer = consumers.apply(0);
				if (consumer != null) {
					// No data to flush
					consumer.nextRowIsMissing();
				}

				// We are on an empty first column
				return 1;
			} else {
				// This is an empty row: fully skip
				return -1;
			}

		} else {
			nextValue.flip();

			IZeroCopyConsumer consumer = consumers.apply(flushedColumnIndex);
			if (consumer != null) {
				if (nextValue.hasRemaining()) {
					flushContent(consumer, nextValue, flushedColumnIndex);
				} else {
					// No data to flush
					consumer.nextRowIsMissing();
				}
			}

			nextValue.limit(nextValue.position());
			nextValue.compact();

			return flushedColumnIndex + 1;
		}
	}

	protected void flushContent(IZeroCopyConsumer consumer, CharSequence charBuffer, int columnIndex) {
		if (consumer == null) {
			return;
		}
		// We have a consumer: let's process the column
		CharSequence subSequence = charBuffer;

		try {
			if (consumer instanceof IntConsumer) {
				if (subSequence.length() == 0) {
					consumer.nextRowIsMissing();
				} else {
					((IntConsumer) consumer)
							.accept(Jdk9CharSequenceParsers.parseInt(subSequence, 0, charBuffer.length(), 10));
				}
			} else if (consumer instanceof LongConsumer) {
				if (subSequence.length() == 0) {
					consumer.nextRowIsMissing();
				} else {
					((LongConsumer) consumer)
							.accept(Jdk9CharSequenceParsers.parseLong(subSequence, 0, charBuffer.length(), 10));
				}
			} else if (consumer instanceof DoubleConsumer) {
				if (subSequence.length() == 0) {
					consumer.nextRowIsMissing();
				} else {
					((DoubleConsumer) consumer).accept(PepperParserHelper.parseDouble(subSequence));
				}
			} else if (consumer instanceof Consumer<?>) {
				// You have better to be a CharSequence consumer
				((Consumer) consumer).accept(subSequence);
			} else {
				throw new IllegalArgumentException("Not a consumer ?!");
			}
		} catch (NumberFormatException e) {
			if (LOGGER.isTraceEnabled()) {
				// check.isTraceEnabled to spare the transient memory of the message
				LOGGER.trace("Ouch on " + subSequence, e);
			}
			consumer.nextRowIsInvalid(subSequence);
		}
	}
}
