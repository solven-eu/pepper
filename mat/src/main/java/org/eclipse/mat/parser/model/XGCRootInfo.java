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

import org.eclipse.mat.snapshot.model.GCRootInfo;

/**
 * Holds details about a garbage collection root. Allows the object id and the context id (the source reference) to be
 * set once the snapshot is reindexed.
 */
public final class XGCRootInfo extends GCRootInfo {
	private static final long serialVersionUID = 1L;

	/**
	 * Create a record of one GC root.
	 *
	 * @param objectAddress
	 *            the object being retained
	 * @param contextAddress
	 *            the object doing the retention such as a thread
	 * @param type
	 *            the type {@link org.eclipse.mat.snapshot.model.GCRootInfo.Type} of the root such as Java local, system
	 *            class.
	 */
	public XGCRootInfo(long objectAddress, long contextAddress, int type) {
		super(objectAddress, contextAddress, type);
	}

	/**
	 * @param objectId
	 *            the object
	 * @see #getObjectId()
	 */
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}

	/**
	 *
	 * @param objectId
	 * @see #getContextId()
	 */
	public void setContextId(int objectId) {
		this.contextId = objectId;
	}
}
