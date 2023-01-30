/**
 * The MIT License
 * Copyright (c) 2008-2009 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.snapshot.model;

import java.util.Comparator;

/**
 * Factory of heap object comparators.
 */
public class ObjectComparators {
	public static Comparator<IObject> getComparatorForTechnicalNameAscending() {
		return new Comparator<IObject>() {
			@Override
			public int compare(IObject o1, IObject o2) {
				return o1.getTechnicalName().compareTo(o2.getTechnicalName());
			}
		};
	}

	public static Comparator<IObject> getComparatorForTechnicalNameDescending() {
		return new Comparator<IObject>() {
			@Override
			public int compare(IObject o1, IObject o2) {
				return o2.getTechnicalName().compareTo(o1.getTechnicalName());
			}
		};
	}

	public static Comparator<IObject> getComparatorForClassSpecificNameAscending() {
		return new Comparator<IObject>() {

			@Override
			public int compare(IObject o1, IObject o2) {
				String name1 = o1.getClassSpecificName();
				if (name1 == null)
					return -1;

				String name2 = o2.getClassSpecificName();
				if (name2 == null)
					return 1;
				return name1.compareTo(name2);
			}
		};
	}

	public static Comparator<IObject> getComparatorForClassSpecificNameDescending() {
		return new Comparator<IObject>() {
			@Override
			public int compare(IObject o1, IObject o2) {
				String name1 = o1.getClassSpecificName();
				if (name1 == null)
					return 1;

				String name2 = o2.getClassSpecificName();
				if (name2 == null)
					return -1;
				return name2.compareTo(name1);
			}
		};
	}

	public static Comparator<IObject> getComparatorForUsedHeapSizeAscending() {
		return new Comparator<IObject>() {
			@Override
			public int compare(IObject o1, IObject o2) {
				if (o1.getUsedHeapSize() < o2.getUsedHeapSize())
					return -1;
				if (o1.getUsedHeapSize() > o2.getUsedHeapSize())
					return 1;
				return 0;
			}
		};
	}

	public static Comparator<IObject> getComparatorForUsedHeapSizeDescending() {
		return new Comparator<IObject>() {
			@Override
			public int compare(IObject o1, IObject o2) {
				if (o1.getUsedHeapSize() < o2.getUsedHeapSize())
					return 1;
				if (o1.getUsedHeapSize() > o2.getUsedHeapSize())
					return -1;
				return 0;
			}
		};
	}

	public static Comparator<IObject> getComparatorForRetainedHeapSizeAscending() {
		return new Comparator<IObject>() {
			@Override
			public int compare(IObject o1, IObject o2) {
				if (o1.getRetainedHeapSize() < o2.getRetainedHeapSize())
					return -1;
				if (o1.getRetainedHeapSize() > o2.getRetainedHeapSize())
					return 1;
				return 0;
			}
		};
	}

	public static Comparator<IObject> getComparatorForRetainedHeapSizeDescending() {
		return new Comparator<IObject>() {
			@Override
			public int compare(IObject o1, IObject o2) {
				if (o1.getRetainedHeapSize() < o2.getRetainedHeapSize())
					return 1;
				if (o1.getRetainedHeapSize() > o2.getRetainedHeapSize())
					return -1;
				return 0;
			}
		};
	}
}
