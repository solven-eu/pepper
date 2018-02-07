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
package cormoran.pepper.csv.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * 
 * @author Benoit Lacelle
 *
 */
public class ZeroCopyCSVWriter implements IZeroCopyCSVWriter {
	protected static final Logger LOGGER = LoggerFactory.getLogger(ZeroCopyCSVWriter.class);

	public static final char DEFAULT_SEPARATOR = ';';
	public static final String DEFAULT_NEWLINE = "\r\n";

	protected final Writer writer;
	protected final Appendable proxy;

	protected final String separator;
	protected final String newLine;

	// protected final AtomicLong timeFirstWrite = new AtomicLong(-1);
	protected final AtomicLong nbCharWritten = new AtomicLong();
	protected final AtomicLong nbLinesWritten = new AtomicLong();

	protected final String filenameForOutput;

	public ZeroCopyCSVWriter(Writer writer, String filenameForOutput) {
		this(writer, filenameForOutput, DEFAULT_SEPARATOR, DEFAULT_NEWLINE);
	}

	public ZeroCopyCSVWriter(Writer writer, String filenameForOutput, char separator, String newLine) {
		this(writer, filenameForOutput, String.valueOf(separator), newLine);
	}

	public ZeroCopyCSVWriter(Writer writer, String filenameForOutput, String separator, String newLine) {
		this.writer = writer;
		proxy = makeProxy(writer);

		this.filenameForOutput = filenameForOutput;

		this.separator = separator;
		this.newLine = newLine;
	}

	/**
	 * final as it is used in the constructor
	 */
	protected final Appendable makeProxy(final Appendable writer) {
		return new Appendable() {

			@Override
			public Appendable append(CharSequence csq, int start, int end) throws IOException {
				nbCharWritten.addAndGet(end - start);
				return writer.append(csq, start, end);
			}

			@Override
			public Appendable append(char c) throws IOException {
				nbCharWritten.incrementAndGet();
				return writer.append(c);
			}

			@Override
			public Appendable append(CharSequence csq) throws IOException {
				nbCharWritten.addAndGet(csq.length());
				return writer.append(csq);
			}
		};
	}

	/**
	 * @return the number of written rows
	 */
	@Override
	public long applyAsLong(Stream<?> input) {
		Joiner joiner = Joiner.on(separator).useForNull("");
		try {
			AtomicLong nbRows = new AtomicLong();

			Stream<CharSequence> escaped = input.filter(Objects::nonNull).map(part -> {
				CharSequence asCharSequence;
				if (part instanceof CharSequence) {
					// DO not materialize the String if possible
					asCharSequence = (CharSequence) part;
				} else {
					asCharSequence = part.toString();
				}

				if (contains(asCharSequence, separator) || contains(asCharSequence, newLine)) {
					// Escape only when necessary
					return "\"" + asCharSequence + "\"";
				} else {
					return asCharSequence;
				}
			}).peek(cs -> nbRows.incrementAndGet());

			// We write to proxy to count characters
			Appendable output = joiner.appendTo(proxy, escaped.iterator());
			// For the sake of SpotBugs
			assert output == proxy;

			writer.write(newLine);
			nbLinesWritten.incrementAndGet();

			return nbRows.get();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected boolean contains(CharSequence containined, CharSequence contained) {
		if (containined == null || contained == null) {
			// TODO: Should we really prevent
			return false;
		}
		// TODO : zero-copy
		return containined.toString().contains(contained.toString());
	}

	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		LOGGER.info("We wrote {} chars for {} rows in '{}'",
				getNbCharsWritten(),
				getNbLinesWritten(),
				filenameForOutput);
	}

	public Writer getWriter() {
		return writer;
	}

	public long getNbCharsWritten() {
		return nbCharWritten.get();
	}

	public long getNbLinesWritten() {
		return nbLinesWritten.get();
	}

	@Override
	public String getFilenameForOutput() {
		return filenameForOutput;
	}
}
