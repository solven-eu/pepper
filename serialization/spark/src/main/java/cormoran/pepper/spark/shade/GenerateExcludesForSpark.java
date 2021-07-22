package cormoran.pepper.spark.shade;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import com.google.common.io.ByteStreams;

public class GenerateExcludesForSpark {
	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateExcludesForSpark.class);

	public static void main(String[] args) throws IOException {
		Path sparkLib = Paths.get(System.getenv("SPARK_HOME"), "jars");
		Set<String> fileNames = Files.walk(sparkLib).map(p -> p.toFile().getName()).collect(Collectors.toSet());

		String artifactSets = new GenerateExcludesForSpark().generatesExcludes(fileNames);

		LOGGER.info("Generates excludes: {}{}", System.lineSeparator(), artifactSets);
	}

	public String generatesExcludes(Set<String> fileNames) throws IOException {
		byte[] bytes = ByteStreams
				.toByteArray(new ClassPathResource("/mvn_dependency_tree/mvn-dependency_tree.txt").getInputStream());
		String dependencyTree = new String(bytes, StandardCharsets.UTF_8);

		Set<String> groupIdArtifactIdInTree = new TreeSet<>();

		// e.g. '[INFO] | | | +- org.springframework:spring-aop:jar:5.3.6:compile'
		// '_' is important to capture scala version in 'spark-core_2.12'
		Pattern pattern = Pattern.compile("- ([^:]+:[^:]+):");

		Stream.of(dependencyTree.split("[\r\n]+")).forEach(row -> {
			Matcher m = pattern.matcher(row);

			if (m.find()) {
				String artifactId = m.group(1);

				groupIdArtifactIdInTree.add(artifactId);
			}
		});

		List<String> toExclude = new ArrayList<>();

		fileNames.forEach(fileName -> {
			if (!fileName.endsWith(".jar")) {
				LOGGER.debug("Exclude not jar: path");
				return;
			}

			String artifactInFileName = fileName.substring(0, fileName.lastIndexOf('-'));

			groupIdArtifactIdInTree.stream()
					// Look for a groupIdArtifactId which has as artifact, the filename of current jar
					.filter(artifactId -> {
						int indexOfUnderscore = artifactInFileName.indexOf('_');
						if (artifactId.startsWith("org.apache.spark:") && indexOfUnderscore >= 0) {
							// Replace 'org.apache.spark:spark-core_2.12' by 'org.apache.spark:spark-core' to handle
							// different scala versions
							String artifactFileNameWithoutScala =
									artifactInFileName.substring(0, indexOfUnderscore);
							return artifactId.startsWith("org.apache.spark:" + artifactFileNameWithoutScala + "_");
						} else {
							return artifactId.endsWith(":" + artifactInFileName);
						}
					})
					.findAny()
					.ifPresent(artifact -> {
						toExclude.add(artifact);
					});
			;
		});

		StringBuilder artifactSetBuilder = new StringBuilder();

		artifactSetBuilder.append("<artifactSet>");
		artifactSetBuilder.append(System.lineSeparator());
		artifactSetBuilder.append("	<excludes>");
		artifactSetBuilder.append(System.lineSeparator());
		artifactSetBuilder.append(
				"		<!-- excludes generated automatically with cormoran.pepper.spark.GenerateExcludesForSpark -->");
		artifactSetBuilder.append(System.lineSeparator());

		toExclude.forEach(oneToExclude -> {
			artifactSetBuilder.append("		<exclude>");
			artifactSetBuilder.append(oneToExclude);
			artifactSetBuilder.append("</exclude>");
			artifactSetBuilder.append(System.lineSeparator());
		});

		artifactSetBuilder.append("	<excludes>");
		artifactSetBuilder.append(System.lineSeparator());
		artifactSetBuilder.append("</artifactSet>");
		artifactSetBuilder.append(System.lineSeparator());
		return artifactSetBuilder.toString();
	}
}
