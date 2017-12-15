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
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;

public class TestZeroCopyCSVParser {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestZeroCopyCSVParser.class);

	ZeroCopyCSVParser parser = new ZeroCopyCSVParser();

	AtomicLong nbEvents = new AtomicLong();
	AtomicLong nbMissing = new AtomicLong();
	AtomicLong nbInvalid = new AtomicLong();

	@Test
	public void testEmptyString_NoConsumer() throws IOException {
		parser.parse(new StringReader(""), ',', Collections.emptyList());
	}

	@Test
	public void testEmptyString_OneConsumer() throws IOException {
		parser.parse(new StringReader(""), ',', Collections.singletonList(new IZeroCopyIntConsumer() {

			@Override
			public void accept(int value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}));

		Assert.assertEquals(0, nbInvalid.get());
		// We may discuss if we should consider the first row is missing or not
		// Assert.assertEquals(1, nbMissing.get());
		Assert.assertEquals(0, nbMissing.get());
		Assert.assertEquals(0, nbEvents.get());
	}

	@Test
	public void testEmptyStringAndSeparator_OneConsumer() throws IOException {
		parser.parse(new StringReader(","), ',', Collections.singletonList(new IZeroCopyIntConsumer() {

			@Override
			public void accept(int value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}));

		Assert.assertEquals(0, nbInvalid.get());
		Assert.assertEquals(1, nbMissing.get());
		Assert.assertEquals(0, nbEvents.get());
	}

	@Test
	public void testSingleColumn_IntColumn_OneIntConsumer() throws IOException {
		parser.parse(new StringReader("123"), ',', Collections.singletonList(new IZeroCopyIntConsumer() {

			@Override
			public void accept(int value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}));

		Assert.assertEquals(0, nbInvalid.get());
		Assert.assertEquals(0, nbMissing.get());
		Assert.assertEquals(1, nbEvents.get());
	}

	@Test
	public void testSingleColumn_IntColumn_OneIntConsumer_MultipleEOL() throws IOException {
		parser.parse(new StringReader("123\r\n\r\n"), ',', Collections.singletonList(new IZeroCopyIntConsumer() {

			@Override
			public void accept(int value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}));

		Assert.assertEquals(0, nbInvalid.get());
		Assert.assertEquals(0, nbMissing.get());
		Assert.assertEquals(1, nbEvents.get());
	}

	@Test
	public void testSingleColumn_IntColumn_OneIntConsumer_TwoRows() throws IOException {
		parser.parse(new StringReader("123\r234"), ',', Collections.singletonList(new IZeroCopyIntConsumer() {

			@Override
			public void accept(int value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}));

		Assert.assertEquals(0, nbInvalid.get());
		Assert.assertEquals(0, nbMissing.get());
		Assert.assertEquals(2, nbEvents.get());
	}

	@Test
	public void testTwoColumn_IntColumns_OneIntConsumer() throws IOException {
		parser.parse(new StringReader("123,234"), ',', Collections.singletonList(new IZeroCopyIntConsumer() {

			@Override
			public void accept(int value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}));

		Assert.assertEquals(0, nbInvalid.get());
		Assert.assertEquals(0, nbMissing.get());
		// We have no consumer or the second column
		Assert.assertEquals(1, nbEvents.get());
	}

	@Test
	public void testTwoColumn_IntColumns_OneIntConsumerOneLongConsumer() throws IOException {
		parser.parse(new StringReader("123,234"), ',', Arrays.asList(new IZeroCopyIntConsumer() {

			@Override
			public void accept(int value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}, new IZeroCopyLongConsumer() {

			@Override
			public void accept(long value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}));

		Assert.assertEquals(0, nbInvalid.get());
		Assert.assertEquals(0, nbMissing.get());
		Assert.assertEquals(2, nbEvents.get());
	}

	@Test
	public void testTwoColumn_IntColumns_OneIntConsumerOneLongConsumer_TwoRows() throws IOException {
		parser.parse(new StringReader("123,234\n345,456"), ',', Arrays.asList(new IZeroCopyIntConsumer() {

			@Override
			public void accept(int value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}, new IZeroCopyLongConsumer() {

			@Override
			public void accept(long value) {
				nbEvents.incrementAndGet();
			}

			@Override
			public void nextRowIsMissing() {
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				throw new UnsupportedOperationException();
			}
		}));

		Assert.assertEquals(0, nbInvalid.get());
		Assert.assertEquals(0, nbMissing.get());
		Assert.assertEquals(4, nbEvents.get());
	}

	@Test
	public void testParseToPrimitiveArrays() throws IOException {
		int[] firstColumn = new int[2];
		long[] secondColumn = new long[2];

		parser.parse(new StringReader("123,234\n345,456"), ',', Arrays.asList(new IZeroCopyIntConsumer() {
			AtomicLong rowIndex = new AtomicLong();

			@Override
			public void accept(int value) {
				firstColumn[Ints.checkedCast(rowIndex.getAndIncrement())] = value;
			}

			@Override
			public void nextRowIsMissing() {
				rowIndex.getAndIncrement();
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				rowIndex.getAndIncrement();
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				return rowIndex.get();
			}
		}, new IZeroCopyLongConsumer() {

			AtomicLong rowIndex = new AtomicLong();

			@Override
			public void accept(long value) {
				secondColumn[Ints.checkedCast(rowIndex.getAndIncrement())] = value;
			}

			@Override
			public void nextRowIsMissing() {
				rowIndex.getAndIncrement();
				nbMissing.incrementAndGet();
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				LOGGER.error("Invalid: '{}'", charSequence);
				rowIndex.getAndIncrement();
				nbInvalid.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				return rowIndex.get();
			}
		}));

		Assert.assertEquals(0, nbInvalid.get());
		Assert.assertEquals(0, nbMissing.get());
		Assert.assertArrayEquals(new int[] { 123, 345 }, firstColumn);
		Assert.assertArrayEquals(new long[] { 234, 456 }, secondColumn);
	}
}
