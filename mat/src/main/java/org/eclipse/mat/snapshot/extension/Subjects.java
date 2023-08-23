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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Used to tag resolvers with the names of classes that they handle. Can be used as follows: <code>
 * <pre>
 * &#64;Subjects({"com.example.class1", "com.example.Class2"})
 * </pre>
 * </code> or <code>
 * <pre>
 * &#64;Subjects("com.example.class1")
 * </pre>
 * </code> See {@link Subject} for a single class name, though this annotation can also be used for a single class name.
 * <p>
 * Experimental: can also be used to tag queries which only make sense when at least one the classes is present in the
 * snapshot.
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface Subjects {
	String[] value();
}
