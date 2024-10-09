/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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

import java.util.concurrent.TimeUnit;

import org.jooq.ExecuteContext;
import org.jooq.impl.DefaultExecuteListener;
import org.jooq.tools.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solven.pepper.logging.PepperLogHelper;

/**
 * Helps logging slow PGSQL queries
 *
 * @author Benoit Lacelle
 *
 */
public class PepperPgsqlSlowQueryListener extends DefaultExecuteListener {
	private static final long serialVersionUID = 2214139431519435266L;

	private static final Logger LOGGER = LoggerFactory.getLogger(AOverDatasource.class);

	private static final long LOG_WARN_SECONDS = 5;

	private transient StopWatch watch;

	@Override
	public void executeStart(ExecuteContext ctx) {
		super.executeStart(ctx);
		watch = new StopWatch();
	}

	@Override
	public void executeEnd(ExecuteContext ctx) {
		super.executeEnd(ctx);
		long nanoDuration = watch.split();
		onQueryEnd(ctx, nanoDuration);
	}

	protected void onQueryEnd(ExecuteContext ctx, long nanoDuration) {
		if (isSlow(nanoDuration)) {
			onSlowQuery(ctx, nanoDuration);
		}
	}

	protected boolean isSlow(long nanoDuration) {
		return nanoDuration > TimeUnit.SECONDS.toNanos(LOG_WARN_SECONDS);
	}

	protected void onSlowQuery(ExecuteContext ctx, long nanoDuration) {
		LOGGER.warn("jOOQ Meta executed a slow ({}) query. {}",
				PepperLogHelper.humanDuration(nanoDuration, TimeUnit.NANOSECONDS),
				ctx.query());
	}
}