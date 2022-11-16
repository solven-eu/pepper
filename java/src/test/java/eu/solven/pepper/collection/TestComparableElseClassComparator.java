package eu.solven.pepper.collection;

import java.util.Set;
import java.util.TreeSet;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class TestComparableElseClassComparator {
	@Test
	public void testDifferentClasses() {
		Set<Comparable<?>> set = new TreeSet<>(new ComparableElseClassComparator());

		set.add("first");
		set.add(123);

		set.add("second");
		set.add(456);

		Assertions.assertThat(set).containsExactly(123, 456, "first", "second");
	}
}
