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

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;

/**
 * The value of a field if it is an object reference.
 */
public class ObjectReference implements Serializable {
	private static final long serialVersionUID = 1L;

	private transient ISnapshot snapshot;
	private long address;

	/**
	 * Create a reference to an object based on its address but in a form where the object id can be found.
	 *
	 * @param snapshot
	 * @param address
	 */
	public ObjectReference(ISnapshot snapshot, long address) {
		this.snapshot = snapshot;
		this.address = address;
	}

	/**
	 * The actual address of the object
	 *
	 * @return the address
	 */
	public long getObjectAddress() {
		return address;
	}

	/**
	 * The id of the object
	 *
	 * @return the object id
	 * @throws SnapshotException
	 */
	public int getObjectId() throws SnapshotException {
		return snapshot.mapAddressToId(address);
	}

	/**
	 * Get a detailed view of the object
	 *
	 * @return the object detail
	 * @throws SnapshotException
	 */
	public IObject getObject() throws SnapshotException {
		return snapshot.getObject(getObjectId());
	}

	/**
	 * A simple view of the object as an address
	 *
	 * @return the object address as a hexadecimal number.
	 */
	@Override
	public String toString() {
		return "0x" + Long.toHexString(address);
	}
}
