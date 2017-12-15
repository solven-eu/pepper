/*******************************************************************************
 * Copyright (c) 2009, 2010 SAP AG.
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
 * A progress listener that does very little, but passes on the essentials to the delegate.
 * 
 * @since 0.8
 */
public class SilentProgressListener implements IProgressListener {
	IProgressListener delegate;

	public SilentProgressListener(IProgressListener delegate) {
		this.delegate = delegate;
	}

	@Override
	public void beginTask(String name, int totalWork) {
		delegate.subTask(name);
	}

	@Override
	public void done() {
	}

	@Override
	public boolean isCanceled() {
		return delegate.isCanceled();
	}

	@Override
	public void sendUserMessage(Severity severity, String message, Throwable exception) {
		delegate.sendUserMessage(severity, message, exception);
	}

	@Override
	public void setCanceled(boolean value) {
		delegate.setCanceled(value);
	}

	@Override
	public void subTask(String name) {
		delegate.subTask(name);
	}

	@Override
	public void worked(int work) {
	}

}
