package cormoran.pepper.csv.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
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

	@Override
	public boolean apply(Stream<?> input) {
		Joiner joiner = Joiner.on(separator).useForNull("");
		try {
			Stream<CharSequence> escaped = input.filter(Objects::nonNull).map(part -> {
				CharSequence asCharSequence;
				if (part instanceof CharSequence) {
					// DO not materialize the String if possible
					asCharSequence = (CharSequence) part;
				} else {
					asCharSequence = part.toString();
				}

				if (StringUtils.contains(asCharSequence, separator) || StringUtils.contains(asCharSequence, newLine)) {
					// Escape only when necessary
					return "\"" + asCharSequence + "\"";
				} else {
					return asCharSequence;
				}
			});

			// We write to proxy to count characters
			Appendable output = joiner.appendTo(proxy, escaped.iterator());
			// For the sake of SpotBugs
			assert output == proxy;

			writer.write(newLine);
			nbLinesWritten.incrementAndGet();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return true;
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
