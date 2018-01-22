package cormoran.pepper.avro;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.io.PepperFileHelper;

public class TestWriteLargeAvro {
	@Test
	public void testWriteLarge() throws IOException {
		AvroStreamFactory factory = new AvroStreamFactory();

		Path tmpPath = PepperFileHelper.createTempPath("TestWriteAvro", ".avro", true);

		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.of("key", 1));

		int nbRows1 = 1000 * 1000;
		Stream<GenericRecord> stream = IntStream.range(0, nbRows1).mapToObj(i -> ImmutableMap.of("key", i)).map(
				AvroStreamHelper.toGenericRecord(schema));

		// Parallel and unordered: suggest data can be processed concurrently: we want to check we do not try to write
		// concurrently in the outputstream
		stream = stream.parallel().unordered();
		long nbRows = factory.writeToPath(tmpPath.toUri(), stream);

		Assert.assertEquals(nbRows1, nbRows);

		// Read back to ensure the data is readable (it may fail if concurrent write have corrupted the flow)
		Assert.assertEquals(nbRows1, factory.stream(tmpPath.toUri()).count());
	}
}
