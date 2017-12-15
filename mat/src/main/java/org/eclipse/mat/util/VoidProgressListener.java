/*******************************************************************************
 * Copyright (c) 2008, 2010 SAP AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    SAP AG - initial API and implementation
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
