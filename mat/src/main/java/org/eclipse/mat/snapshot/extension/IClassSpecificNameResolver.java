/**
 * The MIT License
 * Copyright (c) 2008-2009 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.snapshot.extension;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IObject;

/**
 * Interface describing a name resolver for objects of specific classes (found in an snapshot), e.g. String (where the
 * char[] is evaluated) or as specific class loader (where the appropriate field holding its name and thereby deployment
 * unit is evaluated). Objects of this interface need to be registered using the
 * <code>org.eclipse.mat.api.nameResolver</code> extension point. Implementations of this interface should be tagged
 * with the {@link Subject} or {@link Subjects} annotation to specify the types of objects in the dump they describe.
 */
public interface IClassSpecificNameResolver {
	/**
	 * Resolve the name for snapshot object.
	 *
	 * @param object
	 *            object for which the name should be resolved
	 * @return name for snapshot object
	 * @throws SnapshotException
	 */
	public String resolve(IObject object) throws SnapshotException;
}
