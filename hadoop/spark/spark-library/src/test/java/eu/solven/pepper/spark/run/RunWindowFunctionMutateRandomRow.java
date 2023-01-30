/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.spark.run;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solven.pepper.hadoop.PepperHadoopHelper;
import scala.Tuple2;

/**
 * A procedure demonstrating how one can join efficiently 2 tables, with a logic so that some enrichment columns holding
 * a metrics are filled only one.
 *
 * @author Benoit Lacelle
 *
 */
public class RunWindowFunctionMutateRandomRow {

	protected static final Logger LOGGER = LoggerFactory.getLogger(RunWindowFunctionMutateRandomRow.class);

	protected RunWindowFunctionMutateRandomRow() {
		// hidden
	}

	public static void main(String... args) throws FileNotFoundException, IOException {
		if (!PepperHadoopHelper.isHadoopReady()) {
			throw new IllegalStateException("Hadoop is not ready");
		}

		Path tmpTargetFolder = Files.createTempDirectory("pepper-spark");

		// Spark expect to receive a folder path which does not exists
		tmpTargetFolder.toFile().delete();

		randomJoin(tmpTargetFolder);
	}

	public static void randomJoin(Path targetFolder) throws FileNotFoundException, IOException {

		// http://stackoverflow.com/questions/38008330/spark-error-a-master-url-must-be-set-in-your-configuration-when-submitting-a
		// https://jaceklaskowski.gitbooks.io/mastering-apache-spark/spark-local.html
		try (SparkSession spark =
				SparkSession.builder().appName("CsvToParquet").config("spark.master", "local[*]").getOrCreate()) {

			try (JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext())) {
				List<Tuple2<Integer, Boolean>> mixedTypes =
						Arrays.asList(new Tuple2<>(1, false), new Tuple2<>(1, false), new Tuple2<>(1, false));

				JavaRDD<Row> rowRDD = jsc.parallelize(mixedTypes).map(row -> RowFactory.create(row._1, row._2));

				StructType mySchema = new StructType().add("id", DataTypes.IntegerType, false)
						.add("flag", DataTypes.BooleanType, false);

				Dataset<Row> df = spark.sqlContext().createDataFrame(rowRDD, mySchema).toDF();

				df.write().json(targetFolder.toAbsolutePath().toString());
			}
		}

		Arrays.stream(targetFolder.toFile().listFiles(file -> file.isFile() && file.getName().endsWith(".json")))
				.forEach(file -> {
					LOGGER.info("Json file: {}", file);
				});
	}
}
