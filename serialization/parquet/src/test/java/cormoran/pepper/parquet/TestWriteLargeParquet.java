package cormoran.pepper.parquet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.avro.AvroSchemaHelper;
import cormoran.pepper.avro.AvroStreamHelper;
import cormoran.pepper.io.PepperFileHelper;

public class TestWriteLargeParquet {
	@Test
	public void testWriteLarge() throws IOException {
		ParquetStreamFactory factory = new ParquetStreamFactory();

		Path tmpPath = PepperFileHelper.createTempPath("TestWriteParquet", ".parquet", true);

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
