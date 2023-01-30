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
package org.eclipse.mat.snapshot;

/**
 * Summary of a parser for the snapshot
 *
 * @noinstantiate
 */
public class SnapshotFormat {
	private String name;
	private String[] fileExtensions;

	/**
	 * Create summary information about a parser
	 *
	 * @param name
	 *            name of the parser type
	 * @param fileExtensions
	 *            file extensions it handles
	 */
	public SnapshotFormat(String name, String[] fileExtensions) {
		this.fileExtensions = fileExtensions;
		this.name = name;
	}

	/**
	 * Get the parser name
	 *
	 * @return the parser name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the file extensions. Used for filtering files in a file dialog when choosing a snapshot to open
	 *
	 * @return an array of file extensions
	 */
	public String[] getFileExtensions() {
		return fileExtensions;
	}
}
