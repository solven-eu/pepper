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
package org.eclipse.mat.util;

/**
 * Empty implementation of {@link IProgressListener} which is frequently used throughout the snapshot API in ISnapshot
 * to get feedback for long running operations. This implementation does nothing.
 *
 * @see IProgressListener
 */
public class VoidProgressListener implements IProgressListener {
	private boolean cancelled = false;

	/**
	 * Does nothing.
	 *
	 * @see IProgressListener#beginTask(String, int)
	 */
	@Override
	public void beginTask(String name, int totalWork) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProgressListener#done()
	 */
	@Override
	public void done() {
	}

	/**
	 * Gets the cancel state.
	 *
	 * @see IProgressListener#isCanceled()
	 */
	@Override
	public boolean isCanceled() {
		return cancelled;
	}

	/**
	 * Sets the cancel state.
	 *
	 * @see IProgressListener#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(boolean value) {
		cancelled = value;
	}

	/**
	 * Does nothing.
	 *
	 * @see IProgressListener#subTask(String)
	 */
	@Override
	public void subTask(String name) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProgressListener#worked(int)
	 */
	@Override
	public void worked(int work) {
	}

	/**
	 * Does nothing
	 *
	 * @see IProgressListener#sendUserMessage(Severity, String, Throwable)
	 */
	@Override
	public void sendUserMessage(Severity severity, String message, Throwable exception) {
	}

}
