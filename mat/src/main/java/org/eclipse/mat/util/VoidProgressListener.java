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
