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
