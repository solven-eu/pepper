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
package org.eclipse.mat.parser.model;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.collect.ArrayLong;
import org.eclipse.mat.snapshot.model.Field;
import org.eclipse.mat.snapshot.model.IPrimitiveArray;
import org.eclipse.mat.snapshot.model.NamedReference;
import org.eclipse.mat.snapshot.model.PseudoReference;

/**
 * Implementation of a primitive array of type byte[], short[], int[], long[], boolean, char[], float[], double[].
 *
 * @noextend
 */
public class PrimitiveArrayImpl extends AbstractArrayImpl implements IPrimitiveArray {
	private static final long serialVersionUID = 2L;

	private int type;

	/**
	 * Constructs a primitive array
	 *
	 * @param objectId
	 *            the id of the array
	 * @param address
	 *            the address of the array
	 * @param classInstance
	 *            the type (class) of the array
	 * @param length
	 *            the length in elements
	 * @param type
	 *            the actual type {@link org.eclipse.mat.snapshot.model.IObject.Type}
	 */
	public PrimitiveArrayImpl(int objectId, long address, ClassImpl classInstance, int length, int type) {
		super(objectId, address, classInstance, length);
		this.type = type;
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public Class<?> getComponentType() {
		return COMPONENT_TYPE[type];
	}

	@Override
	public Object getValueAt(int index) {
		Object data = getValueArray(index, 1);
		return data != null ? Array.get(data, 0) : null;
	}

	@Override
	public Object getValueArray() {
		try {
			return source.getHeapObjectReader().readPrimitiveArrayContent(this, 0, getLength());
		} catch (SnapshotException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Object getValueArray(int offset, int length) {
		try {
			return source.getHeapObjectReader().readPrimitiveArrayContent(this, offset, length);
		} catch (SnapshotException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected Field internalGetField(String name) {
		return null;
	}

	@Override
	public ArrayLong getReferences() {
		ArrayLong references = new ArrayLong(1);
		references.add(classInstance.getObjectAddress());
		return references;
	}

	@Override
	public List<NamedReference> getOutboundReferences() {
		List<NamedReference> references = new ArrayList<NamedReference>(1);
		references.add(new PseudoReference(source, classInstance.getObjectAddress(), "<class>"));
		return references;
	}

	@Override
	protected StringBuffer appendFields(StringBuffer buf) {
		return super.appendFields(buf).append(";size=").append(getUsedHeapSize());
	}

	@Override
	public long getUsedHeapSize() {
		try {
			return getSnapshot().getHeapSize(getObjectId());
		} catch (SnapshotException e) {
			return doGetUsedHeapSize(classInstance, length, type);
		}
	}

	/**
	 * Calculates the size of a primitive array
	 *
	 * @param clazz
	 *            the type
	 * @param length
	 *            the length in elements
	 * @param type
	 *            the actual type {@link org.eclipse.mat.snapshot.model.IObject.Type}
	 * @return the size in bytes
	 * @since 1.0
	 */
	public static long doGetUsedHeapSize(ClassImpl clazz, int length, int type) {
		return alignUpTo8(2 * clazz.getHeapSizePerInstance() + 4 + length * (long) ELEMENT_SIZE[type]);
	}

}
