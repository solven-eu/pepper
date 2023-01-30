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
 * Describes a field of an object, i.e. its name and signature.
 */
public class FieldDescriptor implements Serializable {
	private static final long serialVersionUID = 2L;

	protected String name;
	protected int type;

	/**
	 * Create a field for a class - just contains the field name and type, not the value
	 *
	 * @param name
	 *            field name
	 * @param type
	 *            field type from {@link IObject.Type}
	 */
	public FieldDescriptor(String name, int type) {
		this.name = name;
		this.type = type;
	}

	/**
	 * Gets the field name
	 *
	 * @return the actual field name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the type as a number.
	 *
	 * @return as {@link IObject.Type}
	 */
	public int getType() {
		return type;
	}

	/**
	 * Sets the name of the field. Normally the name should not be changed.
	 *
	 * @param name
	 *            the name of the field.
	 * @noreference
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets the type of the field. Normally the type should not be changed.
	 *
	 * @param type
	 *            the type of the field as {@link IObject.Type}
	 * @noreference
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Returns the type of the field. Used for example by the object inspector pane.
	 *
	 * @return ref byte short int long boolean char float double
	 */
	public String getVerboseSignature() {
		if (type == IObject.Type.OBJECT)
			return "ref";

		String t = IPrimitiveArray.TYPE[type];
		return t.substring(0, t.length() - 2);
	}
}
