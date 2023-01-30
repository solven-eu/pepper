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
package org.eclipse.mat.parser;

import java.util.List;

import org.eclipse.mat.collect.HashMapIntObject;
import org.eclipse.mat.parser.index.IIndexReader;
import org.eclipse.mat.parser.model.ClassImpl;
import org.eclipse.mat.parser.model.XGCRootInfo;
import org.eclipse.mat.parser.model.XSnapshotInfo;

/**
 * Where the parser collect informations when first opening a snapshot
 *
 * @noimplement
 */
public interface IPreliminaryIndex {
	/**
	 * Get basic information about the snapshot
	 *
	 * @return the basic data
	 */
	XSnapshotInfo getSnapshotInfo();

	/**
	 * Store the class id to ClassImpl mapping
	 *
	 * @param classesById
	 */
	void setClassesById(HashMapIntObject<ClassImpl> classesById);

	/**
	 * store the GC roots information
	 *
	 * @param gcRoots
	 */
	void setGcRoots(HashMapIntObject<List<XGCRootInfo>> gcRoots);

	/**
	 * store the thread local variable information
	 *
	 * @param thread2objects2roots
	 */
	void setThread2objects2roots(HashMapIntObject<HashMapIntObject<List<XGCRootInfo>>> thread2objects2roots);

	/**
	 * store the object to outbound references table. The type of the object must be the first reference.
	 *
	 * @param outbound
	 */
	void setOutbound(IIndexReader.IOne2ManyIndex outbound);

	/**
	 * store the object id to address mapping
	 *
	 * @param identifiers
	 */
	void setIdentifiers(IIndexReader.IOne2LongIndex identifiers);

	/**
	 * store the object id to class id mapping
	 *
	 * @param object2classId
	 */
	void setObject2classId(IIndexReader.IOne2OneIndex object2classId);

	/**
	 * store the array to size in bytes mapping
	 *
	 * @param array2size
	 * @since 1.0
	 */
	void setArray2size(IIndexReader.IOne2SizeIndex array2size);

}
