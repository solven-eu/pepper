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
package org.eclipse.mat.snapshot;

import java.util.HashMap;

/**
 * This class holds the paths from one single object to its GC roots.
 *
 * @noinstantiate
 */
public final class PathsFromGCRootsTree {
	private int ownId;
	private int[] objectIds;
	private HashMap<Integer, PathsFromGCRootsTree> objectInboundReferers;

	public PathsFromGCRootsTree(int ownId,
			HashMap<Integer, PathsFromGCRootsTree> objectInboundReferers,
			int[] objectIds) {
		this.ownId = ownId;
		this.objectInboundReferers = objectInboundReferers;
		this.objectIds = objectIds;
	}

	/**
	 * Get object being the root for this tree.
	 *
	 * @return object being the root for this tree
	 */
	public int getOwnId() {
		return ownId;
	}

	/**
	 * Get referencing objects.
	 *
	 * @return referencing objects
	 */
	public int[] getObjectIds() {
		return objectIds;
	}

	/**
	 * Get sub tree for a referencing object.
	 *
	 * @param objId
	 *            referencing object from which on the sub tree is requested
	 * @return sub tree for a referencing object
	 */
	public PathsFromGCRootsTree getBranch(int objId) {
		return objectInboundReferers.get(objId);
	}
}
