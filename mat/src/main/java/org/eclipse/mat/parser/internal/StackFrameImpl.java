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
package org.eclipse.mat.parser.internal;

import org.eclipse.mat.snapshot.model.IStackFrame;

/**
 *
 * @noextend This class is not intended to be subclassed by clients. May still be subject of change
 *
 */
class StackFrameImpl implements IStackFrame {
	private String text;

	private int[] localObjectIds;

	public StackFrameImpl(String text, int[] localObjectIds) {
		this.text = text;
		this.localObjectIds = localObjectIds;
	}

	@Override
	public int[] getLocalObjectsIds() {
		if (localObjectIds == null) {
			return new int[0];
		} else {
			return localObjectIds;
		}
	}

	@Override
	public String getText() {
		return text;
	}

}
