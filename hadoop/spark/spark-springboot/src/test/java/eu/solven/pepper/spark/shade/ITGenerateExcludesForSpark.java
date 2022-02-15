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
