/**
 * The MIT License
 * Copyright (c) 2023-2024 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.spark.run.azure;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.azurebfs.constants.ConfigurationKeys;
import org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider;
import org.apache.hadoop.fs.azurebfs.oauth2.RefreshTokenBasedTokenProvider;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * Dummy SpringBoot application interacting with ABFSS
 *
 * Parquet files can be viewed with:
 *
 * https://github.com/mukunku/ParquetViewer/releases
 *
 * @author Benoit Lacelle
 *
 */
@SpringBootApplication(scanBasePackages = "none")
@EnableAutoConfiguration(exclude = {
		// This will fails in Spark cluster as Spark relies on gson-2.2.4.jar
		GsonAutoConfiguration.class })
public class RunParquetToCsvAsSpringBootFromToAbfss {
	private static final Logger LOGGER = LoggerFactory.getLogger(RunParquetToCsvAsSpringBootFromToAbfss.class);

	public static void main(String[] args) {
		SpringApplication.run(RunParquetToCsvAsSpringBootFromToAbfss.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		Environment env = ctx.getEnvironment();

		String tenantId = env.getRequiredProperty("azure.activedirectory.tenant-id");

		boolean loginWithAccessKey = true;
		boolean loginAsUser = true;

		return args -> {
			String accountName = "someaccountname";
			String accountUrl = accountName + ".dfs.core.windows.net";

			// https://cloudarchitected.com/2019/04/accessing-azure-data-lake-storage-gen2-from-clients/
			Configuration conf = new Configuration();

			conf.set("fs.defaultFS", "abfss://some_container@" + accountUrl);

			if (loginWithAccessKey) {
				String accountKeyEnvKey = "fs.azure.account.key." + accountUrl;
				conf.set(accountKeyEnvKey, env.getRequiredProperty(accountKeyEnvKey));
			} else {
				conf.set(ConfigurationKeys.FS_AZURE_ACCOUNT_AUTH_TYPE_PROPERTY_NAME, "OAuth");

				String oauthProviderType;
				if (loginAsUser) {
					oauthProviderType = RefreshTokenBasedTokenProvider.class.getName();
				} else {
					oauthProviderType = ClientCredsTokenProvider.class.getName();
				}
				conf.set(ConfigurationKeys.FS_AZURE_ACCOUNT_TOKEN_PROVIDER_TYPE_PROPERTY_NAME, oauthProviderType);
				conf.set(ConfigurationKeys.FS_AZURE_ACCOUNT_OAUTH_CLIENT_ID,
						env.getRequiredProperty("fs.azure.account.oauth2.client.id"));
				conf.set(ConfigurationKeys.FS_AZURE_ACCOUNT_OAUTH_CLIENT_SECRET,
						env.getRequiredProperty("fs.azure.account.oauth2.client.secret"));
				conf.set(ConfigurationKeys.FS_AZURE_ACCOUNT_OAUTH_CLIENT_ENDPOINT,
						"https://login.microsoftonline.com/" + tenantId + "/oauth2/token");
			}

			{
				FileSystem fs = FileSystem.get(conf);
				FileStatus[] files = fs.listStatus(new Path("/"));
				for (FileStatus f : files) {
					LOGGER.info("File: {}", f);
				}
			}

			SparkSession.Builder sparkSessionBuilder = SparkSession.builder()
					.appName("Sample Data Anonymizer")
					.config("spark.master", "local[*]")
					// Enable Hive support for ABFSS
					.enableHiveSupport();
			runSpark(sparkSessionBuilder);
		};
	}

	@SuppressWarnings("PMD.UnusedLocalVariable")
	private void runSpark(SparkSession.Builder sparkSessionBuilder) {
		try (SparkSession spark = sparkSessionBuilder.getOrCreate()) {

			try (JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext())) {
				LOGGER.info("TODO");
			}
		}
	}
}
