package eu.solven.pepper.pgsql;

import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TestPepperPgsqlSlowQueryListener {
	final PepperPgsqlSlowQueryListener listener = new PepperPgsqlSlowQueryListener();

	@Test
	public void testFastIsNotWarn() {
		Assertions.assertThat(listener.isSlow(100)).isFalse();
		Assertions.assertThat(listener.isSlow(TimeUnit.MINUTES.toNanos(1))).isTrue();
	}

	@Test
	public void testSlowIsNotWarn() {
		Assertions.assertThat(listener.isSlow(TimeUnit.HOURS.toNanos(1))).isTrue();
	}
}
