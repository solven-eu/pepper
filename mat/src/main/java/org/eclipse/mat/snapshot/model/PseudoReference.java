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

import org.eclipse.mat.snapshot.ISnapshot;

/**
 * Value of a field if it is a pseudo references. Some references do not actually exist in the heap dump but are
 * automatically generated because they are maintained by the JVM. Examples are the link from an instance to the class
 * and from the class to the class loader.
 */
public class PseudoReference extends NamedReference {
	private static final long serialVersionUID = 1L;

	/**
	 * Create a PseudoReference
	 *
	 * @param snapshot
	 *            the dump
	 * @param address
	 *            the address of the object
	 * @param name
	 *            the description of the reference e.g. &lt;class&gt;, &lt;classloader&gt;
	 */
	public PseudoReference(ISnapshot snapshot, long address, String name) {
		super(snapshot, address, name);
	}
}
