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

import java.util.Collection;

import org.eclipse.mat.SnapshotException;

/**
 * Interface describing an interactive computer for paths from GC roots to an object. You will get such a computer from
 * the {@link ISnapshot} API.
 * <p>
 * Finding paths from GC roots to an object is handy if you want to learn which objects are responsible for the given
 * object to remain in memory. Since the snapshot implementation artificially creates references from the object to its
 * class and from the class to its class loader you can even see why a class or class loader remains in memory, i.e.
 * which other objects hold references to objects of the class or class loader of interest.
 *
 * @noimplement
 */
public interface IPathsFromGCRootsComputer {
	/**
	 * Get next shortest path. The computer holds the state of the computation and allows to continuously ask for the
	 * next path. If null is returned no path is available anymore.
	 * <p>
	 * This method allows you either to ask for all paths (which could take quite some time and memory but shows you the
	 * complete picture) or one by one (the shortest paths are returned first; more useful in an UI as a user might find
	 * a problem faster among just a few shorter paths).
	 *
	 * @return int array holding the object ids of the objects forming the path from the first element at index 0
	 *         (object for which the computation was started) to the last element in the int array (object identified as
	 *         GC root)
	 * @throws SnapshotException
	 */
	int[] getNextShortestPath() throws SnapshotException;

	/**
	 * Helper method constructing a tree like data structure from the given paths. Either all so far collected paths
	 * could be dropped in here or just the last ones if you want to limit the view.
	 *
	 * @param paths
	 *            paths from GC roots previously returned by {@link #getNextShortestPath}
	 * @return tree like data structure holding the paths from GC roots
	 */
	PathsFromGCRootsTree getTree(Collection<int[]> paths);
}
