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
package eu.solven.pepper.parquet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This will log a parquet file into the console.
 * 
 * You'd better using DuckDB: https://duckdb.org/
 *
 * @author Benoit Lacelle
 *
 */
public class RunParquetVisualizer {

	protected static final Logger LOGGER = LoggerFactory.getLogger(RunParquetVisualizer.class);

	protected RunParquetVisualizer() {
		// hidden
	}

	public static void main(String[] args) throws IOException {
		Path path = getParquetFile(args);

		AtomicLong rowIndex = new AtomicLong();
		new ParquetBytesToStream().stream(path.toUri().toURL().openStream())
				.forEach(row -> LOGGER.info("row #{}: {}", rowIndex.getAndIncrement(), row));
	}

	protected static Path getParquetFile(String... args) {
		if (args == null || args.length < 1) {
			throw new IllegalArgumentException("We expect at least one argument being the path top the parquet file");
		}

		String pathAsString = args[0];
		Path path = Paths.get(pathAsString);

		if (!path.toFile().isFile()) {
			throw new IllegalArgumentException(path + " is not a file");
		}
		return path;
	}
}
