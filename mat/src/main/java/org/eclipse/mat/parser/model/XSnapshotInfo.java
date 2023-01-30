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
package org.eclipse.mat.parser.model;

import java.util.Date;

import org.eclipse.mat.snapshot.SnapshotInfo;

/**
 * Basic information about the snapshot which can be updated as data is read from the dump.
 */
public final class XSnapshotInfo extends SnapshotInfo {
	private static final long serialVersionUID = 3L;

	/**
	 * Simple constructor, with real data added later using setters.
	 */
	public XSnapshotInfo() {
		super(null, null, null, 0, null, 0, 0, 0, 0, 0);
	}

	/**
	 *
	 * @param prefix
	 * @see #getPrefix()
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	/**
	 *
	 * @param path
	 * @see #getPath()
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 *
	 * @param creationDate
	 * @see #getCreationDate()
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = new Date(creationDate.getTime());
	}

	/**
	 *
	 * @param identifierSize
	 * @see #getIdentifierSize()
	 */
	public void setIdentifierSize(int identifierSize) {
		this.identifierSize = identifierSize;
	}

	/**
	 *
	 * @param jvmInfo
	 * @see #getJvmInfo()
	 */
	public void setJvmInfo(String jvmInfo) {
		this.jvmInfo = jvmInfo;
	}

	/**
	 *
	 * @param numberOfClasses
	 * @see #getNumberOfClasses()
	 */
	public void setNumberOfClasses(int numberOfClasses) {
		this.numberOfClasses = numberOfClasses;
	}

	/**
	 *
	 * @param numberOfClassLoaders
	 * @see #getNumberOfClassLoaders()
	 */
	public void setNumberOfClassLoaders(int numberOfClassLoaders) {
		this.numberOfClassLoaders = numberOfClassLoaders;
	}

	/**
	 *
	 * @param numberOfGCRoots
	 * @see #getNumberOfGCRoots()
	 */
	public void setNumberOfGCRoots(int numberOfGCRoots) {
		this.numberOfGCRoots = numberOfGCRoots;
	}

	/**
	 *
	 * @param numberOfObjects
	 * @see #getNumberOfObjects()
	 */
	public void setNumberOfObjects(int numberOfObjects) {
		this.numberOfObjects = numberOfObjects;
	}

	/**
	 *
	 * @param usedHeapSize
	 * @see #getUsedHeapSize()
	 */
	public void setUsedHeapSize(long usedHeapSize) {
		this.usedHeapSize = usedHeapSize;
	}
}
