package eu.solven.pepper.spark.run.pi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Dummy SpringBoot application wrapping dummy Spark compute operation
 * 
 * @author Benoit Lacelle
 *
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
		// This will fails in Spark cluster as Spark relies on gson-2.2.4.jar
		GsonAutoConfiguration.class })
public class RunSparkPiAsSpringBoot {

	public static void main(String[] args) {
		SpringApplication.run(RunSparkPiAsSpringBoot.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			RunSparkPi.main(args);
		};
	}
}
