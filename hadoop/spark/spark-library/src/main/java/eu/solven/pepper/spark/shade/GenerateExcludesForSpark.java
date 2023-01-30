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
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import com.google.common.io.ByteStreams;

/**
 * Helps generating the artifacSet excludes clause, given a local Spark installation
 *
 * @author Benoit Lacelle
 *
 */
public class GenerateExcludesForSpark {
	private static final Logger LOGGER = LoggerFactory.getLogger(GenerateExcludesForSpark.class);

	/**
	 * Generates a full set of dependencies to exclude based on the jars provided by a Spark installation
	 *
	 * @param dependencyTreeResource
	 *            to be generated with 'mvn dependency:tree'
	 * @param fileNames
	 *            the fileNames in your Spark installation
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("PMD.InsufficientStringBufferDeclaration")
	public String generatesExcludes(Resource dependencyTreeResource, Set<String> fileNames) throws IOException {
		byte[] bytes = ByteStreams.toByteArray(dependencyTreeResource.getInputStream());
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

		// Ensure the exclusions are sorted for easy human-readability and git diffs
		Set<String> toExclude = new TreeSet<>();

		fileNames.forEach(fileName -> {
			if (!fileName.endsWith(".jar")) {
				LOGGER.debug("Exclude not jar: path");
				return;
			}

			String artifactInFileName = fileName.substring(0, fileName.lastIndexOf('-'));
			int indexOfUnderscore = artifactInFileName.indexOf('_');

			groupIdArtifactIdInTree.stream()
					// Look for a groupIdArtifactId which has as artifact, the filename of current jar
					.filter(artifactId -> {
						return filterArtifactId(artifactInFileName, indexOfUnderscore, artifactId);
					})
					.findAny()
					.ifPresent(artifact -> {
						toExclude.add(artifact);
					});
		});

		Set<String> toExcludeExpandScala = toExclude.stream().flatMap(a -> {
			if (a.endsWith("_2.10") || a.endsWith("_2.11") || a.endsWith("_2.12")) {
				String prefix = a.substring(0, a.length() - "_2.1N".length());
				return Stream.of(prefix + "_2.10", prefix + "_2.11", prefix + "_2.12");
			} else {
				return Stream.of(a);
			}
		}).collect(Collectors.toCollection(TreeSet::new));

		StringBuilder artifactSetBuilder = new StringBuilder();

		artifactSetBuilder.append("<artifactSet>")
				.append(System.lineSeparator())
				.append("	<excludes>")
				.append(System.lineSeparator())
				.append("		<!-- excludes generated automatically with cormoran.pepper.spark.GenerateExcludesForSpark -->")
				.append(System.lineSeparator());

		toExcludeExpandScala.forEach(oneToExclude -> {
			artifactSetBuilder.append("		<exclude>")
					.append(oneToExclude)
					.append("</exclude>")
					.append(System.lineSeparator());
		});

		artifactSetBuilder.append("	<excludes>")
				.append(System.lineSeparator())
				.append("</artifactSet>")
				.append(System.lineSeparator());
		return artifactSetBuilder.toString();
	}

	private boolean filterArtifactId(String artifactInFileName, int indexOfUnderscore, String artifactId) {
		if (indexOfUnderscore < 0) {
			// In most cases, we get there
			return artifactId.endsWith(":" + artifactInFileName);
		} else if (artifactId.indexOf('_') < 0) {
			// We have a '_' in file name, but not in current artifact
			return artifactId.endsWith(":" + artifactInFileName);
		}

		String groupId = artifactId.substring(0, artifactId.indexOf(':'));

		// org.apache.spark:spark-core_2.12
		// com.fasterxml.jackson.module:jackson-module-scala_2.12
		// org.scala-lang.modules:scala-xml_2.12
		// com.twitter:chill_2.12
		boolean isKnownGroupIdScala =
				("org.apache.spark".equals(groupId) || "com.fasterxml.jackson.module".equals(groupId)
						|| "org.scala-lang.modules".equals(groupId)
						|| "com.twitter".equals(groupId)) && indexOfUnderscore >= 0;
		if (isKnownGroupIdScala || artifactInFileName.endsWith("_2.10")
				|| artifactInFileName.endsWith("_2.11")
				|| artifactInFileName.endsWith("_2.12")
				|| artifactInFileName.endsWith("_2.13")) {
			// Replace 'org.apache.spark:spark-core_2.12' by 'org.apache.spark:spark-core' to handle
			// different scala versions
			String artifactFileNameWithoutScala = artifactInFileName.substring(0, indexOfUnderscore);
			return artifactId.startsWith(groupId + ":" + artifactFileNameWithoutScala + "_");
		}

		// Last resort
		return artifactId.endsWith(":" + artifactInFileName);
	}
}
