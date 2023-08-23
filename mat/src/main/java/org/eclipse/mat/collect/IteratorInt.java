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
package org.eclipse.mat.collect;

/**
 * Simple iterator to go through ints
 */
public interface IteratorInt {
	/**
	 * Is there a next entry?
	 *
	 * @return true if next entry available
	 */
	boolean hasNext();

	/**
	 * Get the next entry.
	 *
	 * @return the entry
	 */
	int next();
}
