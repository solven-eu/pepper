package cormoran.pepper.arrow;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.file.Path;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.io.PepperFileHelper;

public class TestArrowToStream {

	// When writing to a File, Arrow add a magic header
	// Here, we test writing in a file
	@Test
	public void testToFile() throws IOException {
		Path tmpPath = PepperFileHelper.createTempPath("TestWriteArrow", ".arrow", true);

		long nbRows = new ArrowStreamFactory().transcode(tmpPath.toUri(),
				ArrowStreamHelper.guessSchema(ImmutableMap.of("key", 0)),
				IntStream.range(0, 10).mapToObj(i -> ImmutableMap.of("key", i)));

		Assert.assertEquals(10, nbRows);

		ArrowBytesToStream toSteam = new ArrowBytesToStream();
		Assert.assertEquals(10, toSteam.stream(tmpPath.toFile()).count());

		// Check firstRow
		Assert.assertEquals(0, toSteam.stream(tmpPath.toFile()).findFirst().get().get("key"));
	}

	private void testTranscodedValue(Object value) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		long nbRows = new ArrowStreamFactory().transcode(Channels.newChannel(baos),
				false,
				ArrowStreamHelper.guessSchema(ImmutableMap.of("key", value)),
				IntStream.range(0, 10).mapToObj(i -> ImmutableMap.of("key", value)));

		Assert.assertEquals(10, nbRows);

		ArrowBytesToStream toSteam = new ArrowBytesToStream();
		Assert.assertEquals(10, toSteam.stream(new ByteArrayInputStream(baos.toByteArray())).count());

		// Check firstRow
		Assert.assertEquals(value,
				toSteam.stream(new ByteArrayInputStream(baos.toByteArray())).findFirst().get().get("key"));
	}

	// When writing to a File, Arrow add a magic header
	// Here we test writing NOT in a file
	@Test
	public void testToByteArray_Integer() throws IOException {
		Object value = 123;

		testTranscodedValue(value);
	}

	@Test
	public void testToByteArray_Long() throws IOException {
		Object value = 123L;

		testTranscodedValue(value);
	}

	@Test
	public void testToByteArray_Float() throws IOException {
		Object value = 123.1F;

		testTranscodedValue(value);
	}

	@Test
	public void testToByteArray_Double() throws IOException {
		Object value = 123.999D;

		testTranscodedValue(value);
	}

	@Test
	public void testToByteArray_String() throws IOException {
		Object value = "123L";

		testTranscodedValue(value);
	}
}
