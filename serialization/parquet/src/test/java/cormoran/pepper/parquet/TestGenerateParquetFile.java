package cormoran.pepper.parquet;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.avro.AvroSchemaHelper;
import cormoran.pepper.avro.AvroTranscodingHelper;
import cormoran.pepper.io.PepperFileHelper;

public class TestGenerateParquetFile {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TestGenerateParquetFile.class);

	@Test
	public void testWriteManyDifferentTypes() throws IOException {
		ParquetStreamFactory factory = new ParquetStreamFactory();

		Schema schema = AvroSchemaHelper.proposeSimpleSchema(ImmutableMap.<String, Object>builder()
				.put("keyInteger", 123)
				.put("keyLong", 123456L)
				.put("keyFloat", 1.2F)
				.put("keyDouble", 987.654D)
				.put("keyString", "someString")
				.put("keyLocalDate", LocalDate.now())
				.build());

		int nbRows1 = 7;
		Stream<GenericRecord> stream = IntStream.range(0, nbRows1)
				.mapToObj(i -> ImmutableMap.<String, Object>builder()
						.put("keyInteger", i)
						.put("keyLong", 0L + i)
						.put("keyFloat", i / 7F)
						.put("keyDouble", i / 13D)
						.put("keyString", "someString" + i)
						.put("keyLocalDate", LocalDate.now().plusDays(i))
						.build())
				.map(AvroTranscodingHelper.toGenericRecord(schema));

		Path tmpPath = PepperFileHelper.createTempPath("TestWriteParquet", ".parquet", true);

		long nbRows = factory.serialize(tmpPath.toUri(), stream);
		LOGGER.debug("7 rows have been written in {}", tmpPath);

		Assert.assertEquals(nbRows1, nbRows);

		// Read back to ensure the data is readable (it may fail if concurrent write have corrupted the flow)
		Assert.assertEquals(nbRows1, factory.stream(tmpPath.toUri()).count());
	}
}
