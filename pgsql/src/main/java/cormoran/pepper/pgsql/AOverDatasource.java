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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultExecuteListenerProvider;

import com.google.common.annotations.VisibleForTesting;

import cormoran.pepper.time.PepperDateHelper;

/**
 * Helps building classes depending over a {@link DataSource}.
 * 
 * @param <T>
 * 
 * @author Benoit Lacelle
 */
public abstract class AOverDatasource<T extends org.jooq.Table<?>> {
	@VisibleForTesting
	protected final AtomicLong nbWarnings = new AtomicLong();
	@VisibleForTesting
	protected final AtomicReference<OffsetDateTime> unitTestsNow = new AtomicReference<>();

	protected final Supplier<Connection> datasource;

	public AOverDatasource(Supplier<Connection> datasource) {
		this.datasource = datasource;
	}

	protected abstract T t();

	@VisibleForTesting
	public long getNbWarnings() {
		return nbWarnings.get();
	}

	@VisibleForTesting
	public void setUnitTestsNow(OffsetDateTime now) {
		this.unitTestsNow.set(now);
	}

	protected LocalDateTime nowForPGSQL() {
		return forPGSQL(now());
	}

	public static LocalDateTime forPGSQL(OffsetDateTime now) {
		return PepperDateHelper.asUTCLocalDateTime(now);
	}

	protected OffsetDateTime now() {
		if (unitTestsNow.get() != null) {
			return unitTestsNow.get();
		} else {
			return PepperDateHelper.now();
		}
	}

	protected <R> R onDSLContext(Function<DSLContext, R> contextConsumer,
			Function<SQLException, RuntimeException> exceptionTranslator) {

		try (Connection connection = datasource.get(); DSLContext context = makeDslContext(connection)) {
			return contextConsumer.apply(context);
		} catch (SQLException e) {
			throw exceptionTranslator.apply(e);
		}
	}

	protected <R> R onDSLContext(Function<DSLContext, R> contextConsumer, Supplier<String> exceptionMessageSupplier) {
		return onDSLContext(contextConsumer, e -> new IllegalArgumentException(exceptionMessageSupplier.get(), e));
	}

	protected DSLContext makeDslContext(Connection connection) {
		Configuration configuration = new DefaultConfiguration().set(connection);
		configuration.set(new DefaultExecuteListenerProvider(new PepperPgsqlSlowQueryListener()));

		return DSL.using(connection);
	}
}
