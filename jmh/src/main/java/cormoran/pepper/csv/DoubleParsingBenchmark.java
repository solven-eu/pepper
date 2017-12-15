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
package cormoran.pepper.csv;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import cormoran.pepper.primitive.PepperParserHelper;
import javolution.text.TypeFormat;

/**
 * If Exception in thread "main" java.lang.RuntimeException: ERROR: Unable to find the resource:
 * /META-INF/BenchmarkList, "mvn clean install" or "Run as/Maven generate-sources"
 * 
 * @author Benoit Lacelle
 *
 */
public class DoubleParsingBenchmark {

	private static final String PARSE_SMALL_INTEGER = Double.toString(123);
	private static final String PARSE_BIG_LONG = Double.toString(Long.MAX_VALUE - 123);
	private static final String PARSE_ZERO = Double.toString(0D);
	private static final String PARSE_WIDE_FLOAT = Float.toString((float) Math.sqrt(2F));
	private static final String PARSE_WIDE_DOUBLE = Double.toString(Math.sqrt(2D));

	private interface StringToDouble {
		double parseDouble(CharSequence s);
	}

	/**
	 * Holds the parsers
	 * 
	 * @author Benoit Lacelle
	 *
	 */
	@State(Scope.Benchmark)
	public static class SharedCounters {
		StringToDouble jdk = s -> Double.parseDouble(s.toString());
		StringToDouble javolution = s -> TypeFormat.parseDouble(s);
		StringToDouble apex = s -> PepperParserHelper.parseDouble(s);
	}

	@Benchmark
	public double measureApexDoubleFromInteger(SharedCounters state) {
		return state.apex.parseDouble(PARSE_SMALL_INTEGER);
	}

	@Benchmark
	public double measureApexDoubleFromLong(SharedCounters state) {
		return state.apex.parseDouble(PARSE_BIG_LONG);
	}

	@Benchmark
	public double measureApexDoubleFromZero(SharedCounters state) {
		return state.apex.parseDouble(PARSE_ZERO);
	}

	@Benchmark
	public double measureApexDoubleFromWideFloat(SharedCounters state) {
		return state.apex.parseDouble(PARSE_WIDE_FLOAT);
	}

	@Benchmark
	public double measureApexDoubleFromWideDouble(SharedCounters state) {
		return state.apex.parseDouble(PARSE_WIDE_DOUBLE);
	}

	@Benchmark
	public double measureJdkDoubleFromInteger(SharedCounters state) {
		return state.jdk.parseDouble(PARSE_SMALL_INTEGER);
	}

	@Benchmark
	public double measureJdkDoubleFromLong(SharedCounters state) {
		return state.jdk.parseDouble(PARSE_BIG_LONG);
	}

	@Benchmark
	public double measureJdkDoubleFromZero(SharedCounters state) {
		return state.jdk.parseDouble(PARSE_ZERO);
	}

	@Benchmark
	public double measureJdkDoubleFromWideFloat(SharedCounters state) {
		return state.jdk.parseDouble(PARSE_WIDE_FLOAT);
	}

	@Benchmark
	public double measureApexJdkFromWideDouble(SharedCounters state) {
		return state.jdk.parseDouble(PARSE_WIDE_DOUBLE);
	}

	@Benchmark
	public double measureJavolutionDoubleFromInteger(SharedCounters state) {
		return state.javolution.parseDouble(PARSE_SMALL_INTEGER);
	}

	@Benchmark
	public double measureJavolutionDoubleFromLong(SharedCounters state) {
		return state.javolution.parseDouble(PARSE_BIG_LONG);
	}

	@Benchmark
	public double measureJavolutionDoubleFromZero(SharedCounters state) {
		return state.javolution.parseDouble(PARSE_ZERO);
	}

	@Benchmark
	public double measureJavolutionDoubleFromWideFLoat(SharedCounters state) {
		return state.javolution.parseDouble(PARSE_WIDE_FLOAT);
	}

	@Benchmark
	public double measureJavolutionDoubleFromWideDouble(SharedCounters state) {
		return state.javolution.parseDouble(PARSE_WIDE_DOUBLE);
	}

	public static final int WARMUP_ITERATIONS = 3;
	public static final int MEASUREMENTS_ITERATIONS = 3;

	public static void main(String... args) throws Exception {
		Options opts = new OptionsBuilder().include(".*")
				.warmupIterations(WARMUP_ITERATIONS)
				.measurementIterations(MEASUREMENTS_ITERATIONS)
				// .jvmArgs("-server")
				.forks(1)
				// .outputFormat(OutputFormatType.TextReport)
				.build();

		new Runner(opts).run();
	}

	public static final int NB_ITERATIONS = 1000000;

	/**
	 * Enable running the JMH test in a plain JVM, in order to collect Jit data for JitWatch
	 * 
	 * @author Benoit Lacelle
	 *
	 */
	public static class JitWatchMain {
		public static void main(String[] args) {
			long start = System.currentTimeMillis();

			DoubleParsingBenchmark benchmark = new DoubleParsingBenchmark();
			SharedCounters state = new SharedCounters();

			// Run for 1 minute
			while (System.currentTimeMillis() < start + TimeUnit.MINUTES.toMillis(1)) {
				benchmark.measureApexDoubleFromZero(state);
			}
		}
	}
}