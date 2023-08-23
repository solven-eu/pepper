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
package org.eclipse.mat.snapshot.model;

/**
 * May be subject of change
 *
 * @noimplement
 * @since 0.8
 */
public interface IStackFrame {

	/**
	 * Returns the object IDs of all objects referenced from this stack frame - both Java and JNI local objects
	 *
	 * @return int[] an array containing the object Ids. If there are no local objects to the frame, an empty array will
	 *         be returned
	 */
	int[] getLocalObjectsIds();

	/**
	 * Get the text representation of the stack frame
	 *
	 * @return java.lang.String the text representation of the stack frame - class and method
	 */
	String getText();

}
