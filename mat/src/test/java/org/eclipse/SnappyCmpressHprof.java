/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle
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
