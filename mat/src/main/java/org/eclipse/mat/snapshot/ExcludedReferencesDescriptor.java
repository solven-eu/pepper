/**
 * The MIT License
 * Copyright (c) 2008-2010 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.snapshot;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A way of describing which references should not be followed when calculating retained sets and other queries
 * involving paths.
 */
public final class ExcludedReferencesDescriptor {
	private int[] objectIds;
	private Set<String> fields;

	/**
	 * Constructor based on objects and fields. Excluded reference if the reference is from one of these objects going
	 * through the named fields.
	 *
	 * @param objectIds
	 *            don't go through these objects
	 * @param fields
	 *            then though these fields. null means all fields.
	 */
	public ExcludedReferencesDescriptor(int[] objectIds, Set<String> fields) {
		this.fields = fields;
		this.objectIds = objectIds;
		Arrays.sort(this.objectIds);
	}

	public ExcludedReferencesDescriptor(int[] objectIds, String... fields) {
		this(objectIds, new HashSet<String>(Arrays.asList(fields)));
	}

	/**
	 * The excluded fields
	 *
	 * @return a set of field names
	 */
	public Set<String> getFields() {
		return fields;
	}

	/**
	 * See if this object is excluded.
	 *
	 * @param objectId
	 * @return true if excluded
	 */
	public boolean contains(int objectId) {
		return Arrays.binarySearch(objectIds, objectId) >= 0;
	}

	/**
	 * All the excluded object ids.
	 *
	 * @return an array of object ids.
	 */
	public int[] getObjectIds() {
		return objectIds;
	}
}
