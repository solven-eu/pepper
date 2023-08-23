/*******************************************************************************
 * Copyright (c) 2008, 2023 SAP AG, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - additional debug information
 *    Netflix (Jason Koch) - refactors for increased performance and concurrency
 *******************************************************************************/
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
