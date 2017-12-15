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

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.LongBinaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.Ints;

/**
 * Helps to build {@link IZeroCopyConsumer} instances
 * 
 * @author Benoit Lacelle
 *
 */
public class ZeroCopyConsumers {
	protected static final Logger LOGGER = LoggerFactory.getLogger(ZeroCopyConsumers.class);

	public static IZeroCopyConsumer intConsumer(IntConsumer intConsumer) {
		return intBinaryOperator((rowIndex, r) -> {
			intConsumer.accept(r);
			return rowIndex;
		});
	}

	public static IZeroCopyConsumer intBinaryOperator(IntBinaryOperator rowAndValueOperator) {
		AtomicLong rowIndex = new AtomicLong();

		return new IZeroCopyIntConsumer() {

			@Override
			public void nextRowIsMissing() {
				rowIndex.incrementAndGet();
				LOGGER.trace("No data for row #{}", rowIndex);
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				rowIndex.incrementAndGet();
				LOGGER.trace("Invalid data for row #{}: {}", rowIndex, charSequence);
			}

			@Override
			public void accept(int value) {
				rowAndValueOperator.applyAsInt(Ints.checkedCast(rowIndex.get()), value);

				rowIndex.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				return rowIndex.get();
			}
		};
	}

	public static IZeroCopyConsumer longBinaryOperator(LongBinaryOperator intBinaryOperator) {
		AtomicLong rowIndex = new AtomicLong();

		return new IZeroCopyLongConsumer() {

			@Override
			public void nextRowIsMissing() {
				rowIndex.incrementAndGet();
				LOGGER.trace("No data for row #{}", rowIndex);
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				rowIndex.incrementAndGet();
				LOGGER.trace("Invalid data for row #{}: {}", rowIndex, charSequence);
			}

			@Override
			public void accept(long value) {
				intBinaryOperator.applyAsLong(rowIndex.get(), value);

				rowIndex.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				return rowIndex.get();
			}
		};
	}

	public static IZeroCopyConsumer doubleBinaryOperator(DoubleBinaryOperator intBinaryOperator) {
		AtomicLong rowIndex = new AtomicLong();

		return new IZeroCopyDoubleConsumer() {

			@Override
			public void nextRowIsMissing() {
				rowIndex.incrementAndGet();
				LOGGER.trace("No data for row #{}", rowIndex);
			}

			@Override
			public void nextRowIsInvalid(CharSequence charSequence) {
				rowIndex.incrementAndGet();
				LOGGER.trace("Invalid data for row #{}: {}", rowIndex, charSequence);
			}

			@Override
			public void accept(double value) {
				intBinaryOperator.applyAsDouble(rowIndex.get(), value);

				rowIndex.incrementAndGet();
			}

			@Override
			public long nextValueRowIndex() {
				return rowIndex.get();
			}
		};
	}

}
