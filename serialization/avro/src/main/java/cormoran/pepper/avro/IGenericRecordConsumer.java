package cormoran.pepper.avro;

import java.io.IOException;
import java.util.function.Consumer;

import org.apache.avro.generic.GenericRecord;

public interface IGenericRecordConsumer extends Consumer<GenericRecord>, AutoCloseable {
	@Override
	void close() throws IOException;
}
