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

import java.io.OutputStream;
import java.io.PrintWriter;

import org.eclipse.mat.report.internal.Messages;

/**
 * Class used as progress listener for the console. You can obtain one instance via the
 * {@link org.eclipse.mat.query.IQuery#execute} method if the query is run from Memory Analyzer run in batch mode.
 */
public class ConsoleProgressListener implements IProgressListener {
	private PrintWriter out;
	private boolean isDone = false;
	private int workPerDot;
	private int workAccumulated;
	private int dotsPrinted;

	public ConsoleProgressListener(OutputStream out) {
		this(new PrintWriter(out));
	}

	public ConsoleProgressListener(PrintWriter out) {
		this.out = out;
	}

	@Override
	public void beginTask(String name, int totalWork) {
		out.write(Messages.ConsoleProgressListener_Label_Task + " " + name + "\n");
		out.write("[");
		if (totalWork > 80) {
			workPerDot = totalWork / 80;
		} else {
			workPerDot = 1;
		}
		workAccumulated = 0;
		dotsPrinted = 0;
		out.flush();
	}

	@Override
	public void done() {
		if (!isDone) {
			out.write("]\n");
			out.flush();
			isDone = true;
		}

	}

	@Override
	public boolean isCanceled() {
		return false;
	}

	@Override
	public void setCanceled(boolean value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void subTask(String name) {
		out.write("\n" + Messages.ConsoleProgressListener_Label_Subtask + " " + name + "\n[");
		for (int ii = 0; ii < dotsPrinted; ii++)
			out.write(".");
		out.flush();
	}

	@Override
	public void worked(int work) {
		workAccumulated += work;

		int dotsToPrint = workAccumulated / workPerDot;
		if (dotsToPrint > 0) {
			dotsPrinted += dotsToPrint;
			for (int ii = 0; ii < dotsToPrint; ii++) {
				out.write(".");
			}
			workAccumulated -= (dotsToPrint * workPerDot);
			out.flush();
		}
	}

	@Override
	public void sendUserMessage(Severity severity, String message, Throwable exception) {
		out.write("\n");

		switch (severity) {
		case INFO:
			out.write("[INFO] ");
			break;
		case WARNING:
			out.write("[WARNING] ");
			break;
		case ERROR:
			out.write("[ERROR] ");
			break;
		default:
			out.write("[UNKNOWN] ");
		}

		out.write(message);

		if (exception != null) {
			out.write("\n");
			exception.printStackTrace(out);
		}

		out.write("\n[");
		for (int ii = 0; ii < dotsPrinted; ii++) {
			out.write(".");
		}
	}

}
