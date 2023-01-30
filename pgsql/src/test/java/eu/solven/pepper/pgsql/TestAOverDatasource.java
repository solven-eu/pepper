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
package eu.solven.pepper.pgsql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.junit.Test;
import org.mockito.Mockito;

public class TestAOverDatasource {
	@Test
	public void testRequestListener() throws SQLException {
		Table<?> table = Mockito.mock(Table.class);
		Connection connection = Mockito.mock(Connection.class);

		DatabaseMetaData dbMetadata = Mockito.mock(DatabaseMetaData.class);
		Mockito.when(dbMetadata.getURL()).thenReturn("someurl");

		Mockito.when(connection.getMetaData()).thenReturn(dbMetadata);

		Supplier<Connection> connectionSupplier = () -> connection;

		AOverDatasource<Table<?>> dataSource = new AOverDatasource<Table<?>>(connectionSupplier) {

			@Override
			protected Table<?> t() {
				return table;
			}
		};

		DSLContext dslContext = dataSource.makeDslContext(connectionSupplier.get());

		Assertions.assertThat(dslContext.configuration().executeListenerProviders()).hasSize(1);
		Assertions.assertThat(dslContext.configuration().dialect()).isEqualTo(SQLDialect.DEFAULT);
	}
}
