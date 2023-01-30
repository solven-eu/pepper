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
package eu.solven.pepper.spark.shade;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

/**
 * Helps generating the artifacSet excludes clause, given a local Spark installation
 *
 * @author Benoit Lacelle
 *
 */
public class ITGenerateExcludesForSpark {
	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateExcludesForSpark.class);

	/**
	 * Generate as artifactSet excludes based on local SPARK_HOME installation
	 *
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Path sparkLib = Paths.get(System.getenv("SPARK_HOME"), "jars");
		Set<String> fileNames = Files.walk(sparkLib).map(p -> p.toFile().getName()).collect(Collectors.toSet());

		String artifactSets = new GenerateExcludesForSpark().generatesExcludes(
				new ClassPathResource("/mvn_dependency_tree/scala2_11-spark_2_4-hadoop_2_10.txt"),
				fileNames);

		LOGGER.info("Generates excludes: {}{}", System.lineSeparator(), artifactSets);
	}
}
