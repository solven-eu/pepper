/**
 * The MIT License
 * Copyright (c) 2008 Benoit Lacelle - SOLVEN
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

/**
 * Interface for a plain vanilla object instance in the heap dump.
 *
 * @noimplement
 */
public interface IInstance extends IObject {
	/**
	 * Returns all fields of the object.
	 * <p>
	 * Fields are ordered in such a way, that first fields defined in the current class and then fields of the super
	 * class and its super classes are returned. This order is important to know, if a class declares a field by the
	 * same name as the class it inherits from.
	 */
	List<Field> getFields();

	/**
	 * Returns the field identified by the name.
	 * <p>
	 * If declares a member variable by the same name as the parent class does, then the result of this method is
	 * undefined.
	 */
	Field getField(String name);
}
