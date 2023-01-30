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
package org.eclipse.mat.snapshot;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.util.IProgressListener;

/**
 * Objects of this type can be used to work with paths to many objects
 *
 * @noimplement
 */
public interface IMultiplePathsFromGCRootsComputer {
	/**
	 * Calculates (if not yet calculated) and returns all the paths. The paths are grouped by the GC root object, i.e.
	 * all paths starting from one and the same GC root will be packed in one MultiplePathsFromGCRootsRecord. This
	 * record can be used to get the objects at the next level in the path, etc...
	 *
	 * @param progressListener
	 *            - used to track the progress of the computation
	 * @return MultiplePathsFromGCRootsRecord[] one record for each group of paths starting from the same GC root
	 * @throws SnapshotException
	 */
	public MultiplePathsFromGCRootsRecord[] getPathsByGCRoot(IProgressListener progressListener)
			throws SnapshotException;

	/**
	 * Calculates (if not yet calculated) and returns all the paths. Each element in the Object[] is an int[]
	 * representing the path. The first element in the int[] is the specified object, and the last is the GC root object
	 *
	 * @param progressListener
	 *            - used to track the progress of the computation
	 * @return Object[] - each element in the array is an int[] representing a path
	 * @throws SnapshotException
	 */
	public Object[] getAllPaths(IProgressListener progressListener) throws SnapshotException;

}
