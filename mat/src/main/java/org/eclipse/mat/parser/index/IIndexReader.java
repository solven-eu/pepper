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
package org.eclipse.mat.parser.index;

import java.io.IOException;
import java.io.Serializable;

import org.eclipse.mat.SnapshotException;

/**
 * Interfaces for reading various indexes into the snapshot.
 */
public interface IIndexReader {
	/**
	 * Index from object id to another int. For example, object id to type id.
	 */
	interface IOne2OneIndex extends IIndexReader {
		int get(int index);

		int[] getNext(int index, int length);
	}

	/**
	 * Index from object id to size, stored compressed as an int.
	 *
	 * @since 1.0
	 */
	interface IOne2SizeIndex extends IOne2OneIndex {
		long getSize(int index);
	}

	/**
	 * Index from object id to a long. For example, object id to object address or object id to retained size.
	 */
	interface IOne2LongIndex extends IIndexReader {
		long get(int index);

		int reverse(long value);

		long[] getNext(int index, int length);
	}

	/**
	 * Index from object id to several object ids. For example outbound references from an object or outbound dominated
	 * ids.
	 */
	interface IOne2ManyIndex extends IIndexReader {
		int[] get(int index);
	}

	/**
	 * Index from object id to several object ids. For example inbound references from an object.
	 */
	interface IOne2ManyObjectsIndex extends IOne2ManyIndex {
		int[] getObjectsOf(Serializable key) throws SnapshotException, IOException;
	}

	/**
	 * Size of the index
	 *
	 * @return number of entries
	 */
	int size();

	/**
	 * Clear the caches. Used when the indexes are not current in use and the memory needs to be reclaimed such as when
	 * building the dominator tree.
	 *
	 * @throws IOException
	 */
	void unload() throws IOException;

	/**
	 * Close the backing file.
	 *
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * Delete the backing file.
	 */
	void delete();
}
