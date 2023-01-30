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
package org.eclipse.mat.parser.internal.snapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.mat.snapshot.PathsFromGCRootsTree;

public class PathsFromGCRootsTreeBuilder {
	private int ownId;
	private ArrayList<Integer> objectIds = new ArrayList<Integer>();
	private HashMap<Integer, PathsFromGCRootsTreeBuilder> objectInboundReferers;

	public PathsFromGCRootsTreeBuilder(int ownId) {
		this.ownId = ownId;
		objectInboundReferers = new HashMap<Integer, PathsFromGCRootsTreeBuilder>();
	}

	public HashMap<Integer, PathsFromGCRootsTreeBuilder> getObjectReferers() {
		return objectInboundReferers;
	}

	public PathsFromGCRootsTree toPathsFromGCRootsTree() {
		HashMap<Integer, PathsFromGCRootsTree> data =
				new HashMap<Integer, PathsFromGCRootsTree>(objectInboundReferers.size());
		for (Map.Entry<Integer, PathsFromGCRootsTreeBuilder> entry : objectInboundReferers.entrySet()) {
			data.put(entry.getKey(), entry.getValue().toPathsFromGCRootsTree());
		}
		int[] children = new int[objectIds.size()];
		for (int i = 0; i < children.length; i++) {
			children[i] = objectIds.get(i);
		}
		return new PathsFromGCRootsTree(ownId, data, children);
	}

	public int getOwnId() {
		return ownId;
	}

	public void addObjectReferer(PathsFromGCRootsTreeBuilder referer) {
		if (objectInboundReferers.put(referer.getOwnId(), referer) == null) {
			objectIds.add(referer.getOwnId());
		}
	}
}
