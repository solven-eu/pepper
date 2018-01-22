package cormoran.pepper.arrow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.arrow.vector.types.pojo.Schema;

import com.google.common.annotations.Beta;

import cormoran.pepper.io.PepperURLHelper;

/**
 * This interface enables write and reading of GenericRecord from/to bytes. We work with {@link URI} and not
 * {@link InputStream} as some implementation may not produce/require an actual stream of b ytes like parquet.
 * 
 * @author Benoit Lacelle
 *
 */
@Beta
public interface IArrowStreamFactory {

	Stream<Map<String, ?>> stream(URI uri) throws IOException;

	default long transcode(URI uri, Schema schema, Stream<? extends Map<String, ?>> rowsToWrite) throws IOException {
		if (uri.getScheme().equals("file")) {
			try (FileOutputStream fos = new FileOutputStream(Paths.get(uri).toFile())) {
				return transcode(fos.getChannel(), true, schema, rowsToWrite);
			}
		} else {
			return transcode(Channels.newChannel(PepperURLHelper.outputStream(uri)), false, schema, rowsToWrite);
		}
	}

	long transcode(WritableByteChannel byteChannel,
			boolean outputIsFile,
			Schema schema,
			Stream<? extends Map<String, ?>> rowsToWrite) throws IOException;
}
