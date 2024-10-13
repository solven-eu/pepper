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

import org.jooq.tools.jdbc.DefaultConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link Connection} decorates another {@link Connection} and counts its uses in the call-stack. It is useful in
 * case of recursive/nested of a single thread.
 * 
 * @author Benoit Lacelle
 *
 */
public class CountingConnectionWrapper extends DefaultConnection {
	private static final Logger LOGGER = LoggerFactory.getLogger(CountingConnectionWrapper.class);

	protected static final ThreadLocal<CountingConnectionWrapper> TL_CONNECTION = new ThreadLocal<>();

	final AtomicInteger nestedCalls;

	CountingConnectionWrapper(Connection wrapped) {
		super(wrapped);

		this.nestedCalls = new AtomicInteger();
	}

	private void incrementUse() {
		nestedCalls.incrementAndGet();
	}

	@Override
	public void close() throws SQLException {
		// Intercept the connection closing, and decrement the usage counter
		int leftNestedCalls = nestedCalls.decrementAndGet();
		if (leftNestedCalls == 0) {
			// Remove current connection from current thread cache as its `.close` may actually break it.
			TL_CONNECTION.remove();
			super.close();
		} else if (leftNestedCalls < 0) {
			throw new IllegalStateException("Closing more often than opening. counter=" + leftNestedCalls);
		} else {
			LOGGER.debug("A Connection is not closed as still used by {} parent nested calls", leftNestedCalls);
		}
	}

	private static CountingConnectionWrapper createConnectionWrapper(Connection wrapperConnection) {
		return new CountingConnectionWrapper(wrapperConnection);
	}

	/**
	 * BEWARE this method would not behave correctly in case of nested calls with different connectionSuppliers.
	 * 
	 * @param connectionSupplier
	 * @return a {@link Connection} from given connectionSupplier, or a cached {@link Connection} if this a nested call.
	 */
	@SuppressWarnings("PMD.CloseResource")
	public static Connection getOrMakeConnection(Supplier<Connection> connectionSupplier) {
		CountingConnectionWrapper countingConnection = TL_CONNECTION.get();
		if (countingConnection == null) {
			// Current thread has no connection in cache: we create a new one and attach it to current thread
			countingConnection = createConnectionWrapper(connectionSupplier.get());
			TL_CONNECTION.set(countingConnection);
		}
		countingConnection.incrementUse();
		return countingConnection;
	}
}