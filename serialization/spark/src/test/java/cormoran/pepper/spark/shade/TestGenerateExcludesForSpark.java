package cormoran.pepper.spark.shade;

import java.io.IOException;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class TestGenerateExcludesForSpark {
	final String sparkFile211 = "spark-core_2.11-3.1.2.jar";
	final String sparkFile212 = "spark-core_2.12-3.1.2.jar";

	final GenerateExcludesForSpark generateExcludesForSpark = new GenerateExcludesForSpark();

	@Test
	public void testGenerateArtifactsSets() throws IOException {
		Set<String> fileNames =
				ImmutableSet.of(sparkFile212, "jackson-databind-2.10.0.jar", "some-jar-not-our-dependencies-1.2.3");

		String excludes = generateExcludesForSpark.generatesExcludes(fileNames);

		Assertions.assertThat(excludes)
				.contains("<artifactSet>", "</artifactSet>")
				.contains("<exclude>org.apache.spark:spark-core_2.12</exclude>")
				.contains("<exclude>com.fasterxml.jackson.core:jackson-databind</exclude>")
				.doesNotContain("some-jar-not-our-dependencies");
	}

	@Test
	public void testGenerateArtifactsSets_differentScala() throws IOException {
		Set<String> fileNames =
				ImmutableSet.of(sparkFile211, "jackson-databind-2.10.0.jar", "some-jar-not-our-dependencies-1.2.3");

		String excludes = generateExcludesForSpark.generatesExcludes(fileNames);

		Assertions.assertThat(excludes)
				.contains("<artifactSet>", "</artifactSet>")
				.contains("<exclude>org.apache.spark:spark-core_2.12</exclude>")
				.contains("<exclude>com.fasterxml.jackson.core:jackson-databind</exclude>")
				.doesNotContain("some-jar-not-our-dependencies");
	}
}
