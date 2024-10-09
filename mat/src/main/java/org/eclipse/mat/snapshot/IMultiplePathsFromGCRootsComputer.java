/*******************************************************************************
 * Copyright (c) 2008, 2023 SAP AG, IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    IBM Corporation - additional debug information
 *    Netflix (Jason Koch) - refactors for increased performance and concurrency
 *******************************************************************************/
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
	MultiplePathsFromGCRootsRecord[] getPathsByGCRoot(IProgressListener progressListener) throws SnapshotException;

	/**
	 * Calculates (if not yet calculated) and returns all the paths. Each element in the Object[] is an int[]
	 * representing the path. The first element in the int[] is the specified object, and the last is the GC root object
	 *
	 * @param progressListener
	 *            - used to track the progress of the computation
	 * @return Object[] - each element in the array is an int[] representing a path
	 * @throws SnapshotException
	 */
	Object[] getAllPaths(IProgressListener progressListener) throws SnapshotException;

}
