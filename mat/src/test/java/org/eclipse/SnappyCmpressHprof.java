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
package org.eclipse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.SnappyOutputStream;

import com.google.common.io.ByteStreams;
import com.google.common.io.CountingOutputStream;

public class SnappyCmpressHprof {

	protected static final Logger LOGGER = LoggerFactory.getLogger(SnappyCmpressHprof.class);

	// 2017-08-30 10:10:50,145 [main] INFO org.eclipse.SnappyCmpressHprof.main(46) - Input=44962441763
	// Snappy=11595050694 -> 25%
	public static void main(String[] args) throws FileNotFoundException, IOException {
		// Files.copy(in, target, options)

		String folder = "D:\\blacelle112212\\HeapDUmp\\20170811 Grommet Equity\\";

		String file = "grommet.77831.hprof";
		// file = "grommet.77831.idx.index";

		OutputStream out = new FileOutputStream(new File(folder, file + ".snappy"));

		try (OutputStream nullStream = new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				// do nothing like '/dev/null'
			}
		}) {
			CountingOutputStream cos = new CountingOutputStream(out);

			File inputFile = new File(folder, file);
			ByteStreams.copy(new FileInputStream(inputFile), new SnappyOutputStream(cos));

			LOGGER.info("Input={} Snappy={} -> {}%",
					inputFile.length(),
					cos.getCount(),
					cos.getCount() * 100L / inputFile.length());
		}
	}
}
