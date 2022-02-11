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
package eu.solven.pepper.pgsql;

import org.jooq.codegen.GenerationTool;
import org.jooq.codegen.JavaGenerator;
import org.jooq.meta.h2.H2Database;
import org.jooq.meta.jaxb.Database;
import org.jooq.meta.jaxb.Generate;
import org.jooq.meta.jaxb.Generator;
import org.jooq.meta.jaxb.Jdbc;
import org.jooq.meta.jaxb.Target;
import org.jooq.meta.postgres.PostgresDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * This will initialize the JDBC model, and generate JOOQ classes accordingly. One may first drop all tables:
 *
 * https://stackoverflow.com/questions/3327312/drop-all-tables-in-postgresql
 *
 * DO NOT COPY THE DROP SCHEMA COMMAND HERE, as it is too dangerous
 *
 * If you get something like: "Caused by: java.io.FileNotFoundException: /Users/blacelle/.postgresql/root.crt (No such
 * file or directory)", you should copy "./exec/mitrust/BaltimoreCyberTrustRoot.crt.pem" in specified location (after
 * the required 'mkdir /Users/blacelle/.postgresql')
 *
 * https://stackoverflow.com/questions/11919391/postgresql-error-fatal-role-username-does-not-exist
 *
 * In case of 'FATAL: le rôle « username » n'existe pas', execute 'sudo -u postgres -i'
 *
 * @author Benoit Lacelle
 *
 */
public class PepperJooqHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(PepperJooqHelper.class);

	protected PepperJooqHelper() {
		// hidden
	}

	public static void generateJooq(Environment env, String includes, String packageName) {
		LOGGER.info("About to generate JooQ classes from '{}' to '{}'", includes, packageName);

		Class<? extends org.jooq.meta.Database> jooqDatabaseClass;

		String driverClassName = env.getRequiredProperty("spring.datasource.driverClassName");

		if (driverClassName.contains("postgresql")) {
			jooqDatabaseClass = PostgresDatabase.class;
		} else if (driverClassName.contains("h2")) {
			jooqDatabaseClass = H2Database.class;
		} else {
			throw new IllegalStateException("");
		}

		Database jooqDatabase = new Database().withName(jooqDatabaseClass.getName())
				.withIncludes(includes)
				.withInputSchema("public")
				// https://stackoverflow.com/questions/44455793/jooq-dynamically-change-dbs-schema-in-generated-query
				// Try to make jooq compatible with test in h2
				.withOutputSchemaToDefault(true);
		Jdbc jooqJdbc = new Jdbc().withDriver(driverClassName)
				.withUsername(env.getProperty("spring.datasource.username", ""))
				.withPassword(env.getProperty("spring.datasource.password", ""))
				.withUrl(env.getRequiredProperty("spring.datasource.url"));
		Generator joogGenerator = new Generator().withName(JavaGenerator.class.getName())
				.withDatabase(jooqDatabase)
				.withGenerate(new Generate().withJavaTimeTypes(true))
				.withTarget(new Target().withPackageName(packageName).withDirectory("./src/main/java"));

		try {
			GenerationTool
					.generate(new org.jooq.meta.jaxb.Configuration().withJdbc(jooqJdbc).withGenerator(joogGenerator));
		} catch (Exception e) {
			throw new RuntimeException("Issue with JooQ", e);
		}

		LOGGER.info("Done generating JooQ classes from '{}' to '{}'", includes, packageName);
	}

}
