package eu.solven.pepper.collection;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Objects;

import org.springframework.util.comparator.Comparators;

/**
 * Enables comparing {@link Comparable} of different {@link Class} in the same {@link NavigableSet}.
 * 
 * @author Benoit Lacelle
 *
 */
public class ComparableElseClassComparator implements Comparator<Comparable<?>> {
	final Comparator<Comparable<? extends Object>> nullComparator;

	public ComparableElseClassComparator() {
		this(Comparators.nullsHigh());
	}

	public ComparableElseClassComparator(Comparator<Comparable<? extends Object>> nullComparator) {
		this.nullComparator = nullComparator;
	}

	@Override
	public int compare(Comparable<?> o1, Comparable<?> o2) {
		if (o1 == null || o2 == null) {
			return Objects.compare(o1, o2, nullComparator);
		}
		Class<?> c1 = o1.getClass();
		Class<?> c2 = o2.getClass();
		if (c1 != c2) {
			return c1.getName().compareTo(c2.getName());
		}

		return ((Comparable) o1).compareTo((Comparable) o2);
	}

}
