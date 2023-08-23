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
