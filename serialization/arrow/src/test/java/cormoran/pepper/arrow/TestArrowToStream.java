package cormoran.pepper.arrow;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

import cormoran.pepper.io.PepperFileHelper;

public class TestArrowToStream {

	// TODO: We check test specifically writing in a File and writing in an OutputStream
	@Test
	public void testToStream() throws IOException {
		ArrowBytesToStream toSteam = new ArrowBytesToStream();

		// ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Path tmpPath = PepperFileHelper.createTempPath("TestWriteArrow", ".arrow", true);

		long nbRows = ArrowStreamHelper.writeArrowToIS(tmpPath.toUri(),
				IntStream.range(0, 10).mapToObj(i -> ImmutableMap.of("key", i)));

		Assert.assertEquals(10, nbRows);

		// ByteArrayInputStream inputStream = new ByteArrayInputStream(baos.toByteArray());

		Assert.assertEquals(10, toSteam.stream(new FileInputStream(tmpPath.toFile())).count());
	}
}
