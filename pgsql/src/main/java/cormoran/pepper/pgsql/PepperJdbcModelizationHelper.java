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
package cormoran.pepper.pgsql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This defines the SQL model by applying it on given environment
 *
 * @author Benoit Lacelle
 *
 */
public class PepperJdbcModelizationHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(PepperJdbcModelizationHelper.class);

	// https://blog.jooq.org/tag/default/
	// About DEFAULT and NOT NULL: having NOT NULL will reject INSERT with a explicit NULL: it induces enforcing
	// defining all values when doing an insert. When adding field in SP or IDP, it may lead to issues as the BO may
	// not know these new fields yet

	// A value is required explicitly
	public static final String NO_DEFAULT_NOT_NULL = "NOT NULL";
	// A default value is set if not provided or explicitly set to NULL
	public static final String DEFAULT_NULL_TO_DEFAULT = "DEFAULT";
	// A default value requiring not to set to NULL
	public static final String DEFAULT_NULL_TO_FAILURE = "NOT NULL DEFAULT";

	public static final String DEFAULT_TO_NOW = "TIMESTAMP WITH TIME ZONE " + DEFAULT_NULL_TO_FAILURE + " NOW()";

	protected PepperJdbcModelizationHelper() {
		// hidden
	}

	private static boolean needForeignKeyPrefix(Connection connection) throws SQLException {
		if (connection.getMetaData().getDriverName().contains("H2")) {
			LOGGER.debug("H2 does not allow foreign-keys with same name even if on different tables");
			return true;
		} else {
			return false;
		}
	}

	public static String fk(Statement statement, String tableName, String fkName) {
		try {
			if (needForeignKeyPrefix(statement.getConnection())) {
				return tableName + fkName;
			} else {
				return fkName;
			}
		} catch (SQLException e) {
			throw new IllegalStateException(e);
		}
	}

	public static String getTextInKey(Connection connection) throws SQLException {
		// Text and varchar are equivalent
		// https://stackoverflow.com/questions/4848964/postgresql-difference-between-text-and-varchar-character-varying

		if (connection.getMetaData().getDriverName().contains("H2")) {
			// H2 does not appreciate a primaryKey over a TEXT field
			return "VARCHAR(255)";
		} else {
			// But varchar === text for Postgresql
			// https://stackoverflow.com/questions/4848964/postgresql-difference-between-text-and-varchar-character-varying
			return "TEXT";
		}
	}

	public static String getLongVarBinary(Connection connection) throws SQLException {
		// https://documentation.progress.com/output/DataDirect/DataDirectCloud/index.html#page/queries/postgresql-data-types.html
		if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
			return "BYTEA";
		} else {
			return "LONGVARBINARY";
		}
	}

	public static String getVarCharIgnoreCase(Connection connection) throws SQLException {
		if (connection.getMetaData().getDriverName().contains("PostgreSQL")) {
			// 'VARCHAR_IGNORECASE' does not exist in PostgreSQL
			return "VARCHAR";
		} else {
			return "VARCHAR_IGNORECASE";
		}
	}

}
