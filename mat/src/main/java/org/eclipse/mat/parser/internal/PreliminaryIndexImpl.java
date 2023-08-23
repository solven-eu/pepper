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
package org.eclipse.mat.parser.internal;

import java.util.List;

import org.eclipse.mat.collect.HashMapIntObject;
import org.eclipse.mat.parser.IPreliminaryIndex;
import org.eclipse.mat.parser.index.IIndexReader;
import org.eclipse.mat.parser.model.ClassImpl;
import org.eclipse.mat.parser.model.XGCRootInfo;
import org.eclipse.mat.parser.model.XSnapshotInfo;

/* package */class PreliminaryIndexImpl implements IPreliminaryIndex {
	/**
	 * The basic snapshot data
	 */
	XSnapshotInfo snapshotInfo;

	/**
	 * id -> class impl
	 */
	HashMapIntObject<ClassImpl> classesById;

	/**
	 * GC roots (by ids)
	 */
	HashMapIntObject<List<XGCRootInfo>> gcRoots;
	/**
	 * thread object to list of GC roots (by ids)
	 */
	HashMapIntObject<HashMapIntObject<List<XGCRootInfo>>> thread2objects2roots;

	/**
	 * Outbound references. id -> id*
	 */
	IIndexReader.IOne2ManyIndex outbound = null;

	/**
	 * id -> address
	 */
	IIndexReader.IOne2LongIndex identifiers = null;

	/**
	 * Object to type. id -> id
	 */
	IIndexReader.IOne2OneIndex object2classId = null;

	/**
	 * Array and other non-fixed size objects id -> int (size compressed to an int)
	 */
	IIndexReader.IOne2SizeIndex array2size = null;

	public PreliminaryIndexImpl(XSnapshotInfo snapshotInfo) {
		this.snapshotInfo = snapshotInfo;
	}

	@Override
	public XSnapshotInfo getSnapshotInfo() {
		return snapshotInfo;
	}

	@Override
	public void setClassesById(HashMapIntObject<ClassImpl> classesById) {
		this.classesById = classesById;
	}

	@Override
	public void setGcRoots(HashMapIntObject<List<XGCRootInfo>> gcRoots) {
		this.gcRoots = gcRoots;
	}

	@Override
	public void setThread2objects2roots(HashMapIntObject<HashMapIntObject<List<XGCRootInfo>>> thread2objects2roots) {
		this.thread2objects2roots = thread2objects2roots;
	}

	@Override
	public void setOutbound(IIndexReader.IOne2ManyIndex outbound) {
		this.outbound = outbound;
	}

	@Override
	public void setIdentifiers(IIndexReader.IOne2LongIndex identifiers) {
		this.identifiers = identifiers;
	}

	@Override
	public void setObject2classId(IIndexReader.IOne2OneIndex object2classId) {
		this.object2classId = object2classId;
	}

	@Override
	public void setArray2size(IIndexReader.IOne2SizeIndex array2size) {
		this.array2size = array2size;
	}

	public void delete() {
	}
}
