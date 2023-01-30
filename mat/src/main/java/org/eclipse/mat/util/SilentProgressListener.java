/**
 * The MIT License
 * Copyright (c) 2009-2010 Benoit Lacelle - SOLVEN
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
