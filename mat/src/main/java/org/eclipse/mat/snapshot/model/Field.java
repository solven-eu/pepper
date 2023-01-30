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

import java.io.Serializable;

/**
 * Describes a member variable, i.e. name, signature and value.
 */
public final class Field extends FieldDescriptor implements Serializable {
	private static final long serialVersionUID = 2L;

	protected Object value;

	/**
	 * Create a representation of member variable
	 *
	 * @param name
	 *            the name of the field
	 * @param type
	 *            the type {@link IObject.Type}
	 * @param value
	 *            value is one of ObjectReference - for an object field Byte - for a byte field Short - for a short
	 *            field Integer - for an int field Long - for a long field Boolean - for a boolean field Char - for a
	 *            char field Float - for a float field Double - for a double field
	 */
	public Field(String name, int type, Object value) {
		super(name, type);
		this.value = value;
	}

	/**
	 * Gets the value of the field.
	 *
	 * @return ObjectReference - for an object field Byte - for a byte field Short - for a short field Integer - for an
	 *         int field Long - for a long field Boolean - for a boolean field Char - for a char field Float - for a
	 *         float field Double - for a double field
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Set the value of the field. Normally the value should not be changed. Currently this is used after deserializing
	 * static fields to change the object reference to one having a link to the current snapshot.
	 *
	 * @param object
	 *            ObjectReference - for an object field Byte - for a byte field Short - for a short field Integer - for
	 *            an int field Long - for a long field Boolean - for a boolean field Char - for a char field Float - for
	 *            a float field Double - for a double field
	 */
	public void setValue(Object object) {
		value = object;
	}

	/**
	 * A readable representation of the field. Do not rely on the format of the result.
	 *
	 * @return a description of this field.
	 */
	@Override
	public String toString() {
		return type + " " + name + ": \t" + value;
	}
}
