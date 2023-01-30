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
