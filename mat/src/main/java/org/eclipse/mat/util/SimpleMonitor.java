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
 * A way of generating several progress monitors.
 */
public class SimpleMonitor {
	String task;
	IProgressListener delegate;
	int currentMonitor;
	int[] percentages;

	public SimpleMonitor(String task, IProgressListener monitor, int[] percentages) {
		this.task = task;
		this.delegate = monitor;
		this.percentages = percentages;
	}

	public IProgressListener nextMonitor() {
		if (currentMonitor == 0) {
			int total = 0;
			for (int ii : percentages)
				total += ii;
			delegate.beginTask(task, total);
		}

		return new Listener(percentages[currentMonitor++]);
	}

	public class Listener implements IProgressListener {
		long counter;

		int majorUnits;
		int unitsReported;
		long workDone;
		long workPerUnit;

		boolean isSmaller;

		public Listener(int majorUnits) {
			this.majorUnits = majorUnits;
		}

		@Override
		public void beginTask(String name, int totalWork) {
			if (name != null)
				delegate.subTask(name);

			if (totalWork == 0)
				return;

			isSmaller = totalWork < majorUnits;
			workPerUnit = isSmaller ? majorUnits / totalWork : totalWork / majorUnits;
			unitsReported = 0;
		}

		@Override
		public void subTask(String name) {
			delegate.subTask(name);
		}

		@Override
		public void done() {
			if (majorUnits - unitsReported > 0)
				delegate.worked(majorUnits - unitsReported);
		}

		@Override
		public boolean isCanceled() {
			return delegate.isCanceled();
		}

		public boolean isProbablyCanceled() {
			if (counter++ % 5_000 == 0) {
				return isCanceled();
			} else {
				return false;
			}
		}

		public void totalWorkDone(long work) {
			if (workDone == work)
				return;

			if (workPerUnit == 0)
				return;

			workDone = work;
			int unitsWorked;
			if (isSmaller) {
				unitsWorked = (int) (work * workPerUnit);
			} else {
				unitsWorked = (int) (work / workPerUnit);
			}
			int unitsToReport = unitsWorked - unitsReported;

			if (unitsToReport > 0) {
				delegate.worked(unitsToReport);
				unitsReported += unitsToReport;
			}
		}

		@Override
		public void worked(int work) {
			totalWorkDone(workDone + work);
		}

		@Override
		public void setCanceled(boolean value) {
			delegate.setCanceled(value);
		}

		@Override
		public void sendUserMessage(Severity severity, String message, Throwable exception) {
			delegate.sendUserMessage(severity, message, exception);
		}

		public long getWorkDone() {
			return workDone;
		}

	}

}
