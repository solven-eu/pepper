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
package org.eclipse.mat.snapshot.extension;

import java.util.EnumSet;

/**
 * Enumeration of known JDK collection versions
 *
 * @since 1.6
 */
public enum JdkVersion {
	SUN, IBM14, IBM15, IBM16, // Harmony based collections
	IBM17, // Oracle based collections
	IBM18, JAVA18;

	// helpers
	public static EnumSet<JdkVersion> ALL = EnumSet.allOf(JdkVersion.class);

	public static EnumSet<JdkVersion> of(JdkVersion first, JdkVersion... rest) {
		return EnumSet.of(first, rest);
	}

	public static EnumSet<JdkVersion> except(JdkVersion first, JdkVersion... rest) {
		return EnumSet.complementOf(EnumSet.of(first, rest));
	}
}