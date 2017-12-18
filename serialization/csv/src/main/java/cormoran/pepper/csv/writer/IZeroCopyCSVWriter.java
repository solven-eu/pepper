package cormoran.pepper.csv.writer;

import java.io.Closeable;
import java.util.stream.Stream;

import com.google.common.base.Predicate;

/**
 * This interface wraps the logic of writing rows to a {@link Closeable} resource
 * 
 * @author Benoit Lacelle
 * 
 */
public interface IZeroCopyCSVWriter extends AutoCloseable, Predicate<Stream<?>> {
	// We prefer implementations to return RuntimeExceptions
	@Override
	void close();

	String getFilenameForOutput();

}
