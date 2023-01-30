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

import org.eclipse.mat.snapshot.ISnapshot;

/**
 * Value of a field if it is a pseudo references. Some references do not actually exist in the heap dump but are
 * automatically generated because they are maintained by the JVM. Examples are the link from an instance to the class
 * and from the class to the class loader.
 */
public class PseudoReference extends NamedReference {
	private static final long serialVersionUID = 1L;

	/**
	 * Create a PseudoReference
	 *
	 * @param snapshot
	 *            the dump
	 * @param address
	 *            the address of the object
	 * @param name
	 *            the description of the reference e.g. &lt;class&gt;, &lt;classloader&gt;
	 */
	public PseudoReference(ISnapshot snapshot, long address, String name) {
		super(snapshot, address, name);
	}
}
