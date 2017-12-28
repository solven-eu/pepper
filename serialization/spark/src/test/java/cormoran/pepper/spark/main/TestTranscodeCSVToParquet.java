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
package cormoran.pepper.spark.main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import cormoran.pepper.hadoop.PepperHadoopHelper;
import cormoran.pepper.io.PepperFileHelper;
import cormoran.pepper.spark.run.RunCsvToParquet;

/**
 * Split Parquet files from HDFS and transcode columns in a Spark job
 * 
 * @author Benoit Lacelle
 */
public class TestTranscodeCSVToParquet {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TestTranscodeCSVToParquet.class);

	@Before
	public void assumeHaddopIsReady() {
		Assume.assumeTrue(PepperHadoopHelper.isHadoopReady());
	}

	@Test
	public void testCSVToParquet() throws IOException {
		Path csvPath = PepperFileHelper.createTempPath("TestTranscodeCSVToParquet", ".csv", true);

		Path tmpParquetPath = PepperFileHelper.createTempPath("TestTranscodeCSVToParquet", ".parquet", true);

		// Ensure the file does not exist, else Spark fails writing into it
		Assert.assertFalse(tmpParquetPath.toFile().exists());

		try (BufferedWriter writer = Files.newWriter(csvPath.toFile(), StandardCharsets.UTF_8)) {
			writer.write("A|2");
			writer.newLine();

			writer.write("B|2");
			writer.newLine();
		}

		RunCsvToParquet.csvToParquet(csvPath, tmpParquetPath);

		// TODO: it is unclear if delete on exit will delete the folder recursively
		Assert.assertTrue("Delete " + csvPath.toFile(), csvPath.toFile().delete());

		if (tmpParquetPath.toFile().isDirectory()) {
			LOGGER.info("Parquet files are in {}", tmpParquetPath);
			AtomicLong tryIndex = new AtomicLong();

			try {
				Awaitility.await().ignoreExceptionsInstanceOf(IOException.class).untilAsserted(() -> {
					// In mvn, this test tends to fails on first occurences
					LOGGER.info("Try {} to delete {}", tryIndex.getAndIncrement(), tmpParquetPath);
					FileUtils.deleteDirectory(tmpParquetPath.toFile());
				});
			} catch (Throwable t) {
				// And 'mvn -Dmaven.surefire.debug test' fails with a stack
				LOGGER.error("TODO: for an unknown reason, the directory is not deleted when runned in 'mvn test'", t);
			}
		} else {
			Assert.fail("The parquet folder is empty");
		}
	}

}
