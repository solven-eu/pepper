package eu.solven.pepper.pgsql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.jooq.DSLContext;
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
	}
}
