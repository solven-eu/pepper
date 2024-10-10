/**
 * The MIT License
 * Copyright (c) 2023-2024 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.collection;

import java.util.Comparator;
import java.util.NavigableSet;
import java.util.Objects;

import org.springframework.util.comparator.Comparators;

//import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Enables comparing {@link Comparable} of different {@link Class} in the same {@link NavigableSet}.
 *
 * @author Benoit Lacelle
 *
 */
// @SuppressFBWarnings("SE_COMPARATOR_SHOULD_BE_SERIALIZABLE")
public class ComparableElseClassComparator implements Comparator<Comparable<?>> {
	final Comparator<Comparable<? extends Object>> nullComparator;

	public ComparableElseClassComparator() {
		this(Comparators.nullsHigh());
	}

	public ComparableElseClassComparator(Comparator<Comparable<? extends Object>> nullComparator) {
		this.nullComparator = nullComparator;
	}

	@Override
	@SuppressWarnings("PMD.UnnecessaryCast")
	public int compare(Comparable<?> o1, Comparable<?> o2) {
		if (o1 == null || o2 == null) {
			return Objects.compare(o1, o2, nullComparator);
		}
		Class<?> c1 = o1.getClass();
		Class<?> c2 = o2.getClass();
		if (c1 != c2) {
			return c1.getName().compareTo(c2.getName());
		}

		return ((Comparable) o1).compareTo(o2);
	}

}
