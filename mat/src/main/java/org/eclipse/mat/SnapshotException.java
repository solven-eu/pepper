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
package org.eclipse.mat;

/**
 * Exception used to indicate a problem different from the standard Java exceptions while performing an operation on an
 * snapshot.
 */
public class SnapshotException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Create snapshot exception - should not be used except during deserialization.
	 */
	public SnapshotException() {
	}

	/**
	 * Create snapshot exception with message and root cause.
	 *
	 * @param message
	 * @param cause
	 */
	public SnapshotException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create snapshot exception with message only.
	 *
	 * @param message
	 */
	public SnapshotException(String message) {
		super(message);
	}

	/**
	 * Create snapshot exception with root cause only.
	 *
	 * @param cause
	 */
	public SnapshotException(Throwable cause) {
		super(cause);
	}

	/**
	 * Wrap, if necessary, and return a SnapshotException.
	 */
	public static final SnapshotException rethrow(Throwable e) {
		if (e instanceof RuntimeException) {
			// if we wrap an SnapshotException, pass the snapshot exception on
			if (((RuntimeException) e).getCause() instanceof SnapshotException)
				return (SnapshotException) ((RuntimeException) e).getCause();
			throw (RuntimeException) e;
		} else if (e instanceof SnapshotException)
			return (SnapshotException) e;
		else
			return new SnapshotException(e);
	}
}
