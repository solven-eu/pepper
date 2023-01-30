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
package org.eclipse.mat.parser;

import java.io.IOException;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.parser.model.ObjectArrayImpl;
import org.eclipse.mat.parser.model.PrimitiveArrayImpl;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IObject;

/**
 * Part of the parser which retrieves detailed information about an object
 */
public interface IObjectReader {
	/**
	 * Open the dump file associated with the snapshot
	 *
	 * @param snapshot
	 * @throws SnapshotException
	 * @throws IOException
	 */
	void open(ISnapshot snapshot) //
			throws SnapshotException, IOException;

	/**
	 * Get detailed information about an object
	 *
	 * @param objectId
	 *            the object id
	 * @param snapshot
	 *            the snapshot
	 * @return an IObject such as {@link org.eclipse.mat.parser.model.InstanceImpl},
	 *         {@link org.eclipse.mat.parser.model.ObjectArrayImpl},
	 *         {@link org.eclipse.mat.parser.model.PrimitiveArrayImpl},
	 *         {@link org.eclipse.mat.parser.model.ClassLoaderImpl}
	 * @throws SnapshotException
	 * @throws IOException
	 */
	IObject read(int objectId, ISnapshot snapshot) //
			throws SnapshotException, IOException;

	/**
	 * Get detailed information about a primitive array
	 *
	 * @param array
	 *            the array
	 * @param offset
	 *            where in the array to start
	 * @param length
	 *            how much to read
	 * @return a byte[], short[], int[], long[], boolean[], char[], float[], double[]
	 * @throws IOException
	 * @throws SnapshotException
	 */
	Object readPrimitiveArrayContent(PrimitiveArrayImpl array, int offset, int length) //
			throws IOException, SnapshotException;

	/**
	 * Get detailed information about a object array
	 *
	 * @param array
	 * @param offset
	 *            where in the array to start
	 * @param length
	 *            how much to read
	 * @return an array of object addresses, with 0 for nulls
	 * @throws IOException
	 * @throws SnapshotException
	 */
	long[] readObjectArrayContent(ObjectArrayImpl array, int offset, int length) //
			throws IOException, SnapshotException;

	/**
	 * Get additional information about the snapshot
	 *
	 * @param addon
	 *            type of the additional information
	 * @return the additional information
	 * @throws SnapshotException
	 */
	<A> A getAddon(Class<A> addon) //
			throws SnapshotException;

	/**
	 * tidy up when snapshot no longer required
	 *
	 * @throws IOException
	 */
	void close() throws IOException;

}
