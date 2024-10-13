/**
 * The MIT License
 * Copyright (c) 2024 Benoit Lacelle - SOLVEN
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
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

public class TestCountingConnectionWrapper {
	@Test
	public void testCountingConnectionWrapper() throws SQLException {
		Connection realConnection = Mockito.mock(Connection.class);

		AtomicInteger nbOpen = new AtomicInteger();

		Supplier<Connection> trivialConnectionSupplier = () -> {
			nbOpen.incrementAndGet();
			return realConnection;
		};

		// A Supplier<Connection> as requested by AOverDatasource
		Supplier<Connection> connectionSupplier =
				() -> CountingConnectionWrapper.getOrMakeConnection(trivialConnectionSupplier);

		Assertions.assertThat(nbOpen.get()).isEqualTo(0);

		Connection first = connectionSupplier.get();
		Assertions.assertThat(nbOpen.get()).isEqualTo(1);

		{
			Connection second = connectionSupplier.get();
			Assertions.assertThat(nbOpen.get()).isEqualTo(1);

			{
				Mockito.verify(realConnection, Mockito.times(0)).close();
			}

			second.close();
			Mockito.verify(realConnection, Mockito.times(0)).close();
		}

		first.close();
		Mockito.verify(realConnection, Mockito.times(1)).close();
	}
}
