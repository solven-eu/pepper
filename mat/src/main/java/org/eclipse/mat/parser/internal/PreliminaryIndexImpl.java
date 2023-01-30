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
