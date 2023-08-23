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
			if (counter++ % 5000 == 0) {
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
