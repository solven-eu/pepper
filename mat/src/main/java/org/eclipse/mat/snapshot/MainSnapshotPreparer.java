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
package org.eclipse.mat.snapshot;

import java.io.File;

import javax.management.InstanceNotFoundException;
import javax.management.MalformedObjectNameException;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.util.ConsoleProgressListener;

import eu.solven.pepper.jvm.GCInspector;

/**
 * Typically used to parse a heap-dump. While trying to diminish the heap of mat: -XX:+HeapDumpOnOutOfMemoryError
 * -XX:HeapDumpPath=/disk2/dumps
 *
 * @author Benoit Lacelle
 *
 */
public class MainSnapshotPreparer {
	public static void main(String[] args)
			throws SnapshotException, MalformedObjectNameException, InstanceNotFoundException {
		new GCInspector().afterPropertiesSet();

		if (args.length < 1) {
			throw new IllegalArgumentException("We miss a first argument being the path to the file to analyze");
		}
		String path = args[0];

		SnapshotFactory.openSnapshot(new File(path), new ConsoleProgressListener(System.out));

	}
}
