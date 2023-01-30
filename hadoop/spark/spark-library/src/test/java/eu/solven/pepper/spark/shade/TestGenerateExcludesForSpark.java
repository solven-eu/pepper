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
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.google.common.collect.ImmutableSet;

public class TestGenerateExcludesForSpark {
	final String sparkFile211 = "spark-core_2.11-3.1.2.jar";
	final String sparkFile212 = "spark-core_2.12-3.1.2.jar";

	final GenerateExcludesForSpark generateExcludesForSpark = new GenerateExcludesForSpark();

	final ClassPathResource exampleDependencyTree =
			new ClassPathResource("/mvn_dependency_tree/mvn-dependency_tree.txt");

	@Test
	public void testGenerateArtifactsSets() throws IOException {
		Set<String> fileNames =
				ImmutableSet.of(sparkFile212, "jackson-databind-2.10.0.jar", "some-jar-not-our-dependencies-1.2.3");
		String excludes = generateExcludesForSpark.generatesExcludes(exampleDependencyTree, fileNames);

		Assertions.assertThat(excludes)
				.contains("<artifactSet>", "</artifactSet>")
				.contains("<exclude>org.apache.spark:spark-core_2.11</exclude>")
				.contains("<exclude>org.apache.spark:spark-core_2.12</exclude>")
				.contains("<exclude>com.fasterxml.jackson.core:jackson-databind</exclude>")
				.doesNotContain("some-jar-not-our-dependencies");
	}

	@Test
	public void testGenerateArtifactsSets_differentScala() throws IOException {
		Set<String> fileNames =
				ImmutableSet.of(sparkFile211, "jackson-databind-2.10.0.jar", "some-jar-not-our-dependencies-1.2.3");

		String excludes = generateExcludesForSpark.generatesExcludes(exampleDependencyTree, fileNames);

		Assertions.assertThat(excludes)
				.contains("<artifactSet>", "</artifactSet>")
				.contains("<exclude>org.apache.spark:spark-core_2.11</exclude>")
				.contains("<exclude>org.apache.spark:spark-core_2.12</exclude>")
				.contains("<exclude>com.fasterxml.jackson.core:jackson-databind</exclude>")
				.doesNotContain("some-jar-not-our-dependencies");
	}

	@Test
	public void testGenerateArtifactsSets_scalaModule() throws IOException {
		Set<String> fileNames = ImmutableSet.of("scala-xml_2.12-1.2.0.jar");

		String excludes = generateExcludesForSpark.generatesExcludes(exampleDependencyTree, fileNames);

		Assertions.assertThat(excludes)
				.contains("<artifactSet>", "</artifactSet>")
				.contains("<exclude>org.scala-lang.modules:scala-xml_2.10</exclude>")
				.contains("<exclude>org.scala-lang.modules:scala-xml_2.11</exclude>")
				.contains("<exclude>org.scala-lang.modules:scala-xml_2.12</exclude>")
				.doesNotContain("some-jar-not-our-dependencies");
	}
}
