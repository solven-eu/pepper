/**
 * The MIT License
 * Copyright (c) 2023 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.spark.run.pi;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dummy compute application based on Spark
 *
 * @author X119400
 *
 */
public class RunSparkPi {
	private static final Logger LOGGER = LoggerFactory.getLogger(RunSparkPi.class);

	// https://spark.apache.org/examples.html
	private static final double PI_ESTIMATOR_DENOMINATOR = 4.0;

	private static final int NUM_SAMPLES = 10_000;

	protected RunSparkPi() {
		// hidden
	}

	public static void main(String[] args) {
		List<Integer> l = new ArrayList<>(NUM_SAMPLES);
		for (int i = 0; i < NUM_SAMPLES; i++) {
			l.add(i);
		}

		// The point of .getOrCreate is to either rely on SparkSession provided by the caller (e.g. in case of job
		// started with SparkSubmit)
		// Or to rely on given default configuration if started as a plain Java program
		try (SparkSession spark =
				SparkSession.builder().appName("EstimatePi").config("spark.master", "local[*]").getOrCreate()) {

			try (JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext())) {
				long count = jsc.parallelize(l).filter(i -> {
					double x = Math.random();
					double y = Math.random();
					return x * x + y * y < 1;
				}).count();

				LOGGER.info("Pi is roughly {}", PI_ESTIMATOR_DENOMINATOR * count / NUM_SAMPLES);
			}
		}

	}
}
