/**
 * The MIT License
 * Copyright (c) 2012-2013 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.snapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.SnapshotException;

/**
 * Multiple snapshots found in a dump when no particular dump has been requested. Experimental - the form and name of
 * this class is subject to change Not an API.
 *
 * @since 1.3
 */
public class MultipleSnapshotsException extends SnapshotException {
	private static final long serialVersionUID = 1L;

	/**
	 * Experimental - the form and name of this class is subject to change
	 */
	public static class Context implements Serializable {
		private static final long serialVersionUID = 1L;
		private String runtimeId;
		private String description;
		private String version;
		private List<String> options = new ArrayList<String>();

		public Context(String runtimeId) {
			setRuntimeId(runtimeId);
		}

		public String getRuntimeId() {
			return runtimeId;
		}

		public void setRuntimeId(String runtimeId) {
			this.runtimeId = runtimeId;
		}

		public String getVersion() {
			return version;
		}

		public void setVersion(String version) {
			this.version = version;
		}

		public void addOption(String optionString) {
			options.add(optionString);
		}

		public List<String> getOptions() {
			return options;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	List<Context> contexts = new ArrayList<Context>();

	public List<Context> getRuntimes() {
		return contexts;
	}

	public void addContext(Context runtime) {
		contexts.add(runtime);
	}

	public MultipleSnapshotsException() {
		super();
	}

	public MultipleSnapshotsException(String msg) {
		super(msg);
	}
}