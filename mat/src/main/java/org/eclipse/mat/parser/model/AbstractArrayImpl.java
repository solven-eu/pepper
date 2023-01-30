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

import org.eclipse.mat.snapshot.model.IArray;

/**
 * The general implementation of any Java array object (primitive array, object array).
 *
 * @noextend
 */
public abstract class AbstractArrayImpl extends AbstractObjectImpl implements IArray {
	private static final long serialVersionUID = 2L;

	protected int length;

	private Object info;

	/**
	 * Construct a general object, called from subclass.
	 *
	 * @param objectId
	 *            the index of the object
	 * @param address
	 *            the actual address
	 * @param classInstance
	 *            the type of the object
	 * @param length
	 *            the length of the array in elements
	 */
	public AbstractArrayImpl(int objectId, long address, ClassImpl classInstance, int length) {
		super(objectId, address, classInstance);
		this.length = length;
	}

	/**
	 * Gets the cached information about the contents of the array.
	 *
	 * @return the cached data (parser specific).
	 */
	public Object getInfo() {
		return info;
	}

	/**
	 * Sets the cached information about the contents of the array.
	 */
	public void setInfo(Object content) {
		this.info = content;
	}

	@Override
	public int getLength() {
		return length;
	}

	/**
	 * Sets the length in elements.
	 *
	 * @param i
	 *            the new length
	 */
	public void setLength(int i) {
		length = i;
	}

	@Override
	protected StringBuffer appendFields(StringBuffer buf) {
		return super.appendFields(buf).append(";length=").append(length);
	}

	@Override
	public String getTechnicalName() {
		StringBuilder builder = new StringBuilder(256);
		String name = getClazz().getName();

		int p = name.indexOf('[');
		if (p < 0)
			builder.append(name);
		else
			builder.append(name.subSequence(0, p + 1)).append(getLength()).append(name.substring(p + 1));

		builder.append(" @ 0x");
		builder.append(Long.toHexString(getObjectAddress()));
		return builder.toString();
	}

}
