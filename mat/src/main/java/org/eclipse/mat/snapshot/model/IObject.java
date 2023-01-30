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
package org.eclipse.mat.snapshot.model;

import java.io.Serializable;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;

/**
 * Base interface for all objects found in a snapshot. Other model interfaces derive from this interface, e.g. for
 * classes, plain objects, object arrays, primitive arrays...
 *
 * @noimplement
 */
public interface IObject extends Serializable {
	/**
	 * The type of the primitive array.
	 */
	interface Type {
		int OBJECT = 2;

		int BOOLEAN = 4;
		int CHAR = 5;
		int FLOAT = 6;
		int DOUBLE = 7;
		int BYTE = 8;
		int SHORT = 9;
		int INT = 10;
		int LONG = 11;
	}

	/**
	 * Get id for the snapshot object. The id is not the address, but an internally assigned number fitting into an
	 * <code>int</code> (this helps reducing the memory footprint of the snapshot considerably - addresses are only used
	 * for visualization purposes).
	 *
	 * @return id for the snapshot object
	 */
	int getObjectId();

	/**
	 * Get address for the snapshot object. This is the address at which the object was stored in memory. Use the
	 * address only for visualization purposes and try to use the id wherever possible as the snapshot API is optimized
	 * to handle ids and not addresses. Addresses are bigger ( <code>long</code>), have no consecutive order (with
	 * gaps), and are not used for hashing.
	 *
	 * @return address for the snapshot object
	 */
	long getObjectAddress();

	/**
	 * Get class snapshot object this object is an instance of.
	 *
	 * @return class snapshot object this object is an instance of
	 */
	IClass getClazz();

	/**
	 * Get used heap size of just this object.
	 *
	 * @return used heap size of this object
	 * @since 1.0
	 */
	long getUsedHeapSize();

	/**
	 * Get retained heap size of this object.
	 *
	 * @return retained heap size of this object (returns 0 if the dominator tree wasn't calculated for the
	 *         corresponding snapshot)
	 */
	long getRetainedHeapSize();

	/**
	 * Get technical name of this object which is something like class@address.
	 *
	 * @return technical name of this object which is something like class@address
	 */
	String getTechnicalName();

	/**
	 * Get class specific name of this object which depends on the availability of the appropriate name resolver, e.g.
	 * for a String the value of the char[].
	 *
	 * @return class specific name of the given snapshot object or null if it can't be resolved
	 */
	String getClassSpecificName();

	/**
	 * Get concatenation of {@link #getTechnicalName()} and {@link #getClassSpecificName()}.
	 *
	 * @return concatenation of {@link #getTechnicalName()} and {@link #getClassSpecificName()}
	 */
	String getDisplayName();

	/**
	 * Get list of snapshot objects referenced from this snapshot object with the name of the field over which it was
	 * referenced.
	 *
	 * @return list of snapshot objects referenced from this snapshot object with the name of the field over which it
	 *         was referenced
	 */
	List<NamedReference> getOutboundReferences();

	/**
	 * Resolves and returns the value of a field specified by a dot notation. If the field is a primitive type, the
	 * value the returns the corresponding object wrapper, e.g. a java.lang.Boolean is returned for a field of type
	 * boolean. If the field is an object reference, the corresponding IObject is returned.
	 * <p>
	 * The field can be specified using the dot notation, i.e. object references are followed and its fields are
	 * evaluated. If any of the object references is null, null is returned.
	 *
	 * @param field
	 *            the field name in dot notation
	 * @return the value of the field
	 */
	Object resolveValue(String field) throws SnapshotException;

	/**
	 * Get {@link GCRootInfo} if the object is a garbage collection root or null otherwise. An object may or may not be
	 * a garbage collection root, it may even be one for multiple reasons (described in the {@link GCRootInfo} object).
	 *
	 * @return {@link GCRootInfo} if the object is a garbage collection root or null otherwise
	 */
	GCRootInfo[] getGCRootInfo() throws SnapshotException;

	/**
	 * Returns the snapshot from which this object has been read.
	 *
	 * @return the snapshot from which this object has been read.
	 */
	ISnapshot getSnapshot();
}
