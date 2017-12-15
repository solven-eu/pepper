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

/**
 * Interface for column consumer for {@link ZeroCopyCSVParser}
 * 
 * @author Benoit Lacelle
 *
 */
public interface IZeroCopyConsumer {
	/**
	 * Called-back by the parser when the column does not appear in the CSV (e.g. there is only 3 columns while this
	 * consumer is attached to the fourth column), or this is a primitive consumer and the column content is empty
	 */
	void nextRowIsMissing();

	/**
	 * Called-back when the parser encounter invalid chars given current column (e.g. chars not parseable as an Integer
	 * when this is an IntPredicate)
	 * 
	 * @param charSequence
	 *            the charSequence would can not be consumed
	 */
	void nextRowIsInvalid(CharSequence charSequence);
}
