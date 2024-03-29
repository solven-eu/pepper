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

import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.util.IProgressListener;

/**
 * Interface for a class instance in the heap dump.
 *
 * @noimplement
 */
public interface IClass extends IObject {
	String JAVA_LANG_CLASS = "java.lang.Class";
	String JAVA_LANG_CLASSLOADER = "java.lang.ClassLoader";

	/**
	 * Returns the fully qualified class name of this class. The package components are separated by dots '.'. Inner
	 * classes use $ to separate the parts.
	 */
	String getName();

	/**
	 * Returns the number of instances of this class present in the heap dump.
	 */
	int getNumberOfObjects();

	/**
	 * Ids of all instances of this class (an empty array if there are no instances of the class)
	 */
	int[] getObjectIds() throws SnapshotException;

	/**
	 * Returns the id of the class loader which loaded this class.
	 */
	int getClassLoaderId();

	/**
	 * Returns the address of the class loader which loaded this class.
	 */
	long getClassLoaderAddress();

	/**
	 * Returns field descriptors for all member variables of instances of this class. If the snapshot data format does
	 * not contain field data then this will be an empty list.
	 */
	List<FieldDescriptor> getFieldDescriptors();

	/**
	 * Returns the static fields and it values. If the snapshot data format does not contain field data then this will
	 * be an empty list.
	 */
	List<Field> getStaticFields();

	/**
	 * Returns the heap size of one instance of this class. Not valid if this class represents an array.
	 *
	 * @since 1.0
	 */
	long getHeapSizePerInstance();

	/**
	 * Returns the retained size of all objects of this instance including the class instance.
	 */
	long getRetainedHeapSizeOfObjects(boolean calculateIfNotAvailable,
			boolean calculateMinRetainedSize,
			IProgressListener listener) throws SnapshotException;

	/**
	 * Returns the id of the super class. -1 if it has no super class, i.e. if it is java.lang.Object.
	 */
	int getSuperClassId();

	/**
	 * Returns the super class.
	 */
	IClass getSuperClass();

	/**
	 * Returns true if the class has a super class.
	 */
	boolean hasSuperClass();

	/**
	 * Returns the direct sub-classes.
	 */
	List<IClass> getSubclasses();

	/**
	 * Returns all sub-classes including sub-classes of its sub-classes.
	 */
	List<IClass> getAllSubclasses();

	/**
	 * Does this class extend a class of the supplied name? With multiple class loaders the supplied name might not be
	 * the class you were intending to find.
	 *
	 * @param className
	 * @return true if it does extend
	 * @throws SnapshotException
	 */
	boolean doesExtend(String className) throws SnapshotException;

	/**
	 * Returns true if the class is an array class.
	 */
	boolean isArrayType();
}
