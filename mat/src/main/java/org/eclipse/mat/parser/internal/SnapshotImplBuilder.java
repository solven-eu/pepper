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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.collect.BitField;
import org.eclipse.mat.collect.HashMapIntObject;
import org.eclipse.mat.parser.IObjectReader;
import org.eclipse.mat.parser.index.IndexManager;
import org.eclipse.mat.parser.internal.util.ParserRegistry.Parser;
import org.eclipse.mat.parser.model.ClassImpl;
import org.eclipse.mat.parser.model.XGCRootInfo;
import org.eclipse.mat.parser.model.XSnapshotInfo;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.util.IProgressListener;

public class SnapshotImplBuilder {
	private XSnapshotInfo snapshotInfo;
	/* package */HashMapIntObject<ClassImpl> classCache;
	/* package */Map<String, List<IClass>> classCacheByName;
	private HashMapIntObject<XGCRootInfo[]> roots;
	private HashMapIntObject<HashMapIntObject<XGCRootInfo[]>> rootsPerThread;

	/* package */BitField arrayObjects;

	/* package */IndexManager indexManager;

	public SnapshotImplBuilder(XSnapshotInfo snapshotInfo) {
		this.snapshotInfo = snapshotInfo;
	}

	public XSnapshotInfo getSnapshotInfo() {
		return snapshotInfo;
	}

	public void setIndexManager(IndexManager indexManager) {
		this.indexManager = indexManager;
	}

	public IndexManager getIndexManager() {
		return indexManager;
	}

	public void setClassCache(HashMapIntObject<ClassImpl> classCache) {
		this.classCache = classCache;
	}

	public HashMapIntObject<ClassImpl> getClassCache() {
		return classCache;
	}

	public void setRoots(HashMapIntObject<XGCRootInfo[]> roots) {
		this.roots = roots;
	}

	public HashMapIntObject<XGCRootInfo[]> getRoots() {
		return roots;
	}

	public void setRootsPerThread(HashMapIntObject<HashMapIntObject<XGCRootInfo[]>> rootsPerThread) {
		this.rootsPerThread = rootsPerThread;
	}

	public void setArrayObjects(BitField arrayObjects) {
		this.arrayObjects = arrayObjects;
	}

	public SnapshotImpl create(Parser parser, IProgressListener listener) throws IOException, SnapshotException {
		IObjectReader heapObjectReader = parser.createObjectReader();
		return SnapshotImpl.create(snapshotInfo,
				parser.getUniqueIdentifier(),
				heapObjectReader,
				classCache,
				roots,
				rootsPerThread,
				arrayObjects,
				indexManager,
				listener);
	}

}
