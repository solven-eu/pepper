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
 * The class represents a references from a running thread object to objects which are local for this thread. Such
 * objects could be for example java local variables, objects used for synchronization in this thread, etc...
 */
public class ThreadToLocalReference extends PseudoReference {
	private static final long serialVersionUID = 1L;
	private int localObjectId;
	private GCRootInfo[] gcRootInfo;

	/**
	 * Create a thread to local reference
	 *
	 * @param snapshot
	 *            the snapshot
	 * @param address
	 *            the address of the object
	 * @param name
	 *            the description of the reference e.g. the root types surrounded by '&lt;' '&gt;'
	 * @param localObjectId
	 *            the local reference object id
	 * @param gcRootInfo
	 *            a description of the root type e.g. Java local etc.
	 */
	public ThreadToLocalReference(ISnapshot snapshot,
			long address,
			String name,
			int localObjectId,
			GCRootInfo[] gcRootInfo) {
		super(snapshot, address, name);
		this.localObjectId = localObjectId;
		this.gcRootInfo = gcRootInfo;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.mat.snapshot.model.ObjectReference#getObjectId()
	 */
	@Override
	public int getObjectId() {
		return localObjectId;
	}

	/**
	 * The description of the thread root information Not currently used, so might be removed.
	 *
	 * @return an array of GC information for the local reference
	 */
	public GCRootInfo[] getGcRootInfo() {
		return gcRootInfo;
	}
}
