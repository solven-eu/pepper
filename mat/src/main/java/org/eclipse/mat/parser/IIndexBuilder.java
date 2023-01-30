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

import java.io.File;
import java.io.IOException;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.util.IProgressListener;

/**
 * Part of the parser which builds the indexes
 */
public interface IIndexBuilder {
	/**
	 * initialize with file and prefix (needed for naming conventions)
	 *
	 * @param file
	 *            the dump file
	 * @param prefix
	 *            used to build index files
	 * @throws SnapshotException
	 * @throws IOException
	 */
	void init(File file, String prefix) throws SnapshotException, IOException;

	/**
	 * pass1 and pass2 parsing
	 *
	 * @param index
	 * @param listener
	 *            for progress and error reporting
	 * @throws SnapshotException
	 * @throws IOException
	 */
	void fill(IPreliminaryIndex index, IProgressListener listener) throws SnapshotException, IOException;

	/**
	 * Memory Analyzer has discard unreachable objects, so the parser may need to known the discarded objects
	 *
	 * @param purgedMapping
	 *            mapping from old id to new id, -1 indicates object has been discarded
	 * @param listener
	 *            for progress and error reporting
	 * @throws IOException
	 */
	void clean(final int[] purgedMapping, IProgressListener listener) throws IOException;

	/**
	 * called in case of error to delete any files / close any file handles
	 */
	void cancel();
}
