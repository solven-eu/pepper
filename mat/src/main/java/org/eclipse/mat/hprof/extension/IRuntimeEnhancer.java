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
package org.eclipse.mat.hprof.extension;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;

/**
 * Using this interface an implementor can provide additional information to a HPROF snapshot
 *
 * See the documentation on the org.eclipse.mat.hprof.enhancer extension point
 *
 */
public interface IRuntimeEnhancer {

	/**
	 * Returns addon information of certain type if such information is attached to the HPROF snapshot
	 *
	 * @param <A>
	 *            The type of the additional information
	 * @param snapshot
	 * @param addon
	 *            the class of the required extra information
	 * @return the extra information, or null
	 * @throws SnapshotException
	 */
	<A> A getAddon(ISnapshot snapshot, Class<A> addon) throws SnapshotException;
}
