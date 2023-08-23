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
package org.eclipse.mat.hprof;

import java.io.IOException;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.collect.ArrayLong;
import org.eclipse.mat.collect.HashMapLongObject;
import org.eclipse.mat.parser.IPreliminaryIndex;
import org.eclipse.mat.parser.index.IIndexReader.IOne2LongIndex;
import org.eclipse.mat.parser.model.ClassImpl;
import org.eclipse.mat.parser.model.XSnapshotInfo;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.util.IProgressListener;

public interface IHprofParserHandler {
	String IDENTIFIER_SIZE = "ID_SIZE";
	String CREATION_DATE = "CREATION_DATE";
	String VERSION = "VERSION";
	String REFERENCE_SIZE = "REF_SIZE";

	class HeapObject {
		public int objectId;
		public long objectAddress;
		public ClassImpl clazz;
		public long usedHeapSize;

		// TODO Could we reserve capacity depending on class fields?
		public ArrayLong references = new ArrayLong();
		public boolean isArray = false;

		public HeapObject(int objectId, long objectAddress, ClassImpl clazz, long usedHeapSize) {
			this.objectId = objectId;
			this.objectAddress = objectAddress;
			this.clazz = clazz;
			this.usedHeapSize = usedHeapSize;
			this.isArray = false;
		}
	}

	// //////////////////////////////////////////////////////////////
	// lifecycle
	// //////////////////////////////////////////////////////////////

	void beforePass1(XSnapshotInfo snapshotInfo) throws IOException;

	void beforePass2(IProgressListener monitor) throws IOException, SnapshotException;

	IOne2LongIndex fillIn(IPreliminaryIndex index) throws IOException;

	void cancel();

	// //////////////////////////////////////////////////////////////
	// report parsed entities
	// //////////////////////////////////////////////////////////////

	void addProperty(String name, String value) throws IOException;

	void addGCRoot(long id, long referrer, int rootType) throws IOException;

	void addClass(ClassImpl clazz, long filePosition) throws IOException;

	void addObject(HeapObject object, long filePosition) throws IOException;

	void reportInstance(long id, long filePosition);

	void reportRequiredObjectArray(long arrayClassID);

	void reportRequiredPrimitiveArray(int arrayType);

	void reportRequiredClass(long classID, int size);

	// //////////////////////////////////////////////////////////////
	// lookup heap infos
	// //////////////////////////////////////////////////////////////

	int getIdentifierSize();

	HashMapLongObject<String> getConstantPool();

	IClass lookupClass(long classId);

	IClass lookupClassByName(String name, boolean failOnMultipleInstances);

	IClass lookupClassByIndex(int objIndex);

	List<IClass> resolveClassHierarchy(long classId);

	int mapAddressToId(long address);

	XSnapshotInfo getSnapshotInfo();

	long getObjectArrayHeapSize(ClassImpl arrayType, int size);

	long getPrimitiveArrayHeapSize(byte elementType, int size);
}
