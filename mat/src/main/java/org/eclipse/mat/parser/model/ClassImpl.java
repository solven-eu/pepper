/**
 * The MIT License
 * Copyright (c) 2008-2016 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.parser.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.collect.ArrayInt;
import org.eclipse.mat.collect.ArrayLong;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.FieldDescriptor;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.snapshot.model.ObjectReference;
import org.eclipse.mat.snapshot.model.PseudoReference;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.VoidProgressListener;

/**
 * Implementation of a Java object representing a java.lang.Class object. As well as some standard object information it
 * contains information about the class and summary details about instances of this class.
 *
 * @noextend
 */
public class ClassImpl extends AbstractObjectImpl implements IClass, Comparable<ClassImpl> {
	private static final long serialVersionUID = 22L;

	public static final String JAVA_LANG_CLASS = IClass.JAVA_LANG_CLASS;

	protected String name;
	protected int superClassId = -1;
	protected long superClassAddress;
	protected int classLoaderId = -1;
	protected long classLoaderAddress;
	protected Field[] staticFields;
	protected FieldDescriptor[] fields;
	protected int usedHeapSize;
	protected int instanceSize;
	protected int instanceCount;
	protected long totalSize;
	protected boolean isArrayType;

	private List<IClass> subClasses;

	private Serializable cacheEntry;

	/**
	 * Construct a class object based on name, address and fields.
	 *
	 * @param address
	 *            the address of the class object
	 * @param name
	 *            the class name, using '.' as package separator
	 * @param superAddress
	 *            the address of the superclass, or 0 if none.
	 * @param loaderAddress
	 *            the address of the class loader
	 * @param staticFields
	 *            all the static fields, with values
	 * @param fields
	 *            all the instance fields as descriptors
	 */
	public ClassImpl(long address,
			String name,
			long superAddress,
			long loaderAddress,
			Field[] staticFields,
			FieldDescriptor[] fields) {
		super(-1, address, null);

		this.name = name;
		this.superClassAddress = superAddress;
		this.classLoaderAddress = loaderAddress;
		this.staticFields = staticFields;
		this.fields = fields;
		this.instanceSize = -1;

		this.totalSize = 0;
		this.isArrayType = name.endsWith("[]");
	}

	/**
	 * Gets the key for extra information about this class.
	 *
	 * @return the key
	 */
	public Serializable getCacheEntry() {
		return cacheEntry;
	}

	/**
	 * Sets the key for extra information about this class.
	 *
	 * @param cacheEntry
	 *            the key
	 */
	public void setCacheEntry(Serializable cacheEntry) {
		this.cacheEntry = cacheEntry;
	}

	/**
	 * Sets the superclass index. May need to be changed after reindexing of a snapshot.
	 *
	 * @param superClassIndex
	 *            the new index
	 */
	public void setSuperClassIndex(int superClassIndex) {
		this.superClassId = superClassIndex;
	}

	/**
	 * Sets the class loader index. May need to be changed after reindexing of a snapshot.
	 *
	 * @param classLoaderIndex
	 *            the new index
	 */
	public void setClassLoaderIndex(int classLoaderIndex) {
		this.classLoaderId = classLoaderIndex;
	}

	@Override
	public int[] getObjectIds() throws SnapshotException {
		try {
			return source.getIndexManager().c2objects().getObjectsOf(this.cacheEntry);
		} catch (IOException e) {
			throw new SnapshotException(e);
		}
	}

	@Override
	public long getRetainedHeapSizeOfObjects(boolean calculateIfNotAvailable,
			boolean approximation,
			IProgressListener listener) throws SnapshotException {
		long answer = this.source.getRetainedSizeCache().get(getObjectId());

		if (answer > 0 || !calculateIfNotAvailable)
			return answer;

		if (answer < 0 && approximation)
			return answer;

		if (listener == null)
			listener = new VoidProgressListener();

		ArrayInt ids = new ArrayInt();
		ids.add(getObjectId());
		ids.addAll(getObjectIds());

		int[] retainedSet;
		long retainedSize = 0;

		if (!approximation) {
			retainedSet = source.getRetainedSet(ids.toArray(), listener);
			if (listener.isCanceled())
				return 0;
			retainedSize = source.getHeapSize(retainedSet);
		} else {
			retainedSize = source.getMinRetainedSize(ids.toArray(), listener);
			if (listener.isCanceled())
				return 0;
		}

		if (approximation)
			retainedSize = -retainedSize;

		this.source.getRetainedSizeCache().put(getObjectId(), retainedSize);
		return retainedSize;

	}

	@Override
	public long getUsedHeapSize() {
		return usedHeapSize;
	}

	@Override
	public ArrayLong getReferences() {
		ArrayLong answer = new ArrayLong(staticFields.length);

		answer.add(classInstance.getObjectAddress());
		if (superClassAddress != 0)
			answer.add(superClassAddress);
		answer.add(classLoaderAddress);

		for (int ii = 0; ii < staticFields.length; ii++) {
			if (staticFields[ii].getValue() instanceof ObjectReference) {
				ObjectReference ref = (ObjectReference) staticFields[ii].getValue();
				answer.add(ref.getObjectAddress());
			}
		}

		return answer;
	}

	@Override
	public List<NamedReference> getOutboundReferences() {
		List<NamedReference> answer = new LinkedList<NamedReference>();
		answer.add(new PseudoReference(source, classInstance.getObjectAddress(), "<class>"));
		if (superClassAddress != 0)
			answer.add(new PseudoReference(source, superClassAddress, "<super>"));
		answer.add(new PseudoReference(source, classLoaderAddress, "<classloader>"));

		for (int ii = 0; ii < staticFields.length; ii++) {
			if (staticFields[ii].getValue() instanceof ObjectReference) {
				ObjectReference ref = (ObjectReference) staticFields[ii].getValue();
				String fieldName = staticFields[ii].getName();
				if (fieldName.startsWith("<"))
					answer.add(new PseudoReference(source, ref.getObjectAddress(), fieldName));
				else
					answer.add(new NamedReference(source, ref.getObjectAddress(), fieldName));
			}
		}

		return answer;
	}

	@Override
	public long getClassLoaderAddress() {
		return classLoaderAddress;
	}

	public void setClassLoaderAddress(long address) {
		this.classLoaderAddress = address;
	}

	@Override
	public List<FieldDescriptor> getFieldDescriptors() {
		return Arrays.asList(fields);
	}

	@Override
	public int getNumberOfObjects() {
		return instanceCount;
	}

	/**
	 * @since 1.0
	 */
	@Override
	public long getHeapSizePerInstance() {
		return instanceSize;
	}

	/**
	 * @since 1.0
	 */
	public void setHeapSizePerInstance(long size) {
		instanceSize = (int) Math.min(size, Integer.MAX_VALUE);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public List<Field> getStaticFields() {
		return Arrays.asList(staticFields);
	}

	public long getSuperClassAddress() {
		return superClassAddress;
	}

	@Override
	public int getSuperClassId() {
		return superClassId;
	}

	@Override
	public ClassImpl getSuperClass() {
		try {
			if (superClassAddress != 0) {
				return (ClassImpl) this.source.getObject(superClassId);
			} else {
				return null;
			}
		} catch (SnapshotException e) {
			throw new RuntimeException(e);
		}
	}

	public long getTotalSize() {
		return totalSize;
	}

	@Override
	public boolean hasSuperClass() {
		return this.superClassAddress != 0;
	}

	@Override
	public int compareTo(ClassImpl other) {
		final long myAddress = getObjectAddress();
		final long otherAddress = other.getObjectAddress();
		if (myAddress > otherAddress) {
			return 1;
		} else {
			if (myAddress == otherAddress) {
				return 0;
			} else {
				return -1;
			}
		}
	}

	/**
	 * @since 1.0
	 */
	public void addInstance(long usedHeapSize) {
		this.instanceCount++;
		this.totalSize += usedHeapSize;
	}

	/**
	 * @since 1.0
	 */
	public void removeInstance(long heapSizePerInstance) {
		this.instanceCount--;
		this.totalSize -= heapSizePerInstance;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<IClass> getSubclasses() {
		if (subClasses != null) {
			return subClasses;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	@Override
	public List<IClass> getAllSubclasses() {
		if (subClasses == null || subClasses.isEmpty())
			return new ArrayList<IClass>();

		List<IClass> answer = new ArrayList<IClass>(subClasses.size() * 2);
		answer.addAll(this.subClasses);
		for (IClass subClass : this.subClasses)
			answer.addAll(subClass.getAllSubclasses());
		return answer;
	}

	@Override
	protected StringBuffer appendFields(StringBuffer buf) {
		return super.appendFields(buf).append(";name=").append(getName());
	}

	@Override
	public boolean isArrayType() {
		return isArrayType;
	}

	@Override
	public String getTechnicalName() {
		StringBuilder builder = new StringBuilder(256);
		builder.append("class ");
		builder.append(getName());
		builder.append(" @ 0x");
		builder.append(Long.toHexString(getObjectAddress()));
		return builder.toString();
	}

	@Override
	protected Field internalGetField(String name) {
		for (Field f : staticFields)
			if (f.getName().equals(name))
				return f;
		return null;
	}

	@Override
	public int getClassLoaderId() {
		return classLoaderId;
	}

	public void addSubClass(ClassImpl clazz) {
		if (subClasses == null)
			subClasses = new ArrayList<IClass>();
		subClasses.add(clazz);
	}

	public void removeSubClass(ClassImpl clazz) {
		subClasses.remove(clazz);
	}

	/**
	 * @since 1.0
	 */
	public void setUsedHeapSize(long usedHeapSize) {
		this.usedHeapSize = (int) Math.min(usedHeapSize, Integer.MAX_VALUE);
	}

	@Override
	public boolean doesExtend(String className) throws SnapshotException {
		if (className.equals(this.name))
			return true;

		if (hasSuperClass()) {
			return ((ClassImpl) source.getObject(this.superClassId)).doesExtend(className);
		} else {
			return false;
		}
	}

	@Override
	public void setSnapshot(ISnapshot dump) {
		super.setSnapshot(dump);

		// object reference need (for convenience) a reference to the snapshot
		// inject after restoring class objects

		for (Field f : this.staticFields) {
			if (f.getValue() instanceof ObjectReference) {
				ObjectReference ref = (ObjectReference) f.getValue();
				f.setValue(new ObjectReference(dump, ref.getObjectAddress()));
			}
		}
	}

}
