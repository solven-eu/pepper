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
package cormoran.pepper.spark.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cormoran.pepper.hadoop.PepperHadoopHelper;
import cormoran.pepper.parquet.ParquetStreamFactory;

public class RunCsvToParquet {

	protected static final Logger LOGGER = LoggerFactory.getLogger(RunCsvToParquet.class);

	public static void main(String[] args) throws Exception {
		if (!PepperHadoopHelper.isHadoopReady()) {
			throw new IllegalStateException("Hadoop is not ready");
		}

		if (args == null || args.length != 2) {
			throw new IllegalArgumentException(
					"We expected 2 arguments: path to CSV file and path to fodler where to write .parquet");
		}

		Path tmpPath = Paths.get(args[0]);
		Path tmpParquetPath = Paths.get(args[1]);

		csvToParquet(tmpPath, tmpParquetPath);
	}

	public static void csvToParquet(Path csvPath, Path parquetTargetPath) throws FileNotFoundException, IOException {
		LOGGER.info("About to convert {} into folder {}", csvPath, parquetTargetPath);

		if (parquetTargetPath.toFile().isFile()) {
			throw new IllegalArgumentException(
					"Can not write parquet files in folder which is already a file: " + parquetTargetPath);
		}

		// http://stackoverflow.com/questions/38008330/spark-error-a-master-url-must-be-set-in-your-configuration-when-submitting-a
		// https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-local.html
		try (SparkSession spark =
				SparkSession.builder().appName("CsvToParquet").config("spark.master", "local[*]").getOrCreate()) {

			try (JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext())) {
				// http://bytepadding.com/big-data/spark/read-write-parquet-files-using-spark/
				SQLContext sqlContext = spark.sqlContext();
				Dataset<Row> inputDf = sqlContext.read().csv(csvPath.toAbsolutePath().toString());

				inputDf.write().parquet(parquetTargetPath.toAbsolutePath().toString());
			}
		}

		Arrays.stream(
				parquetTargetPath.toFile().listFiles(file -> file.isFile() && file.getName().endsWith(".parquet")))
				.forEach(file -> {
					LOGGER.info("Parquet file: {}", file);

					try {
						ParquetStreamFactory.readParquetAsStream(file.toURI(), Collections.emptyMap())
								.forEach(row -> LOGGER.info("Row: {}", row));
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				});
	}
}
