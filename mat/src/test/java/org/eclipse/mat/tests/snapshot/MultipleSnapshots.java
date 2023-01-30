/**
 * The MIT License
 * Copyright (c) 2013 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.tests.snapshot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.MultipleSnapshotsException;
import org.eclipse.mat.tests.TestSnapshots;
import org.junit.Test;

public class MultipleSnapshots {

	/**
	 * Simple test that without an option multiple snapshots cause an exception
	 */
	@Test
	public void testException() {
		try {
			ISnapshot fail = TestSnapshots.getSnapshot(TestSnapshots.ORACLE_JDK7_21_64BIT_HPROFAGENT, true);
			fail("Expected a MultipleSnapshotsException");
		} catch (RuntimeException e) {
			Throwable f = e.getCause();
			assertTrue(f instanceof MultipleSnapshotsException);
			MultipleSnapshotsException s = (MultipleSnapshotsException) f;
			assertEquals(2, s.getRuntimes().size());
			List<MultipleSnapshotsException.Context> ctxs = s.getRuntimes();
			assertEquals("#1", ctxs.get(0).getRuntimeId());
			assertEquals("#2", ctxs.get(1).getRuntimeId());
		}
	}

	/**
	 * Simple test that the two snapshots are different
	 */
	@Test
	public void testDump1() {
		Map<String, String> options = new HashMap<String, String>();
		options.put("snapshot_identifier", "#1");
		ISnapshot snapshot1 = TestSnapshots.getSnapshot(TestSnapshots.ORACLE_JDK7_21_64BIT_HPROFAGENT, options, true);
		options.put("snapshot_identifier", "#2");
		ISnapshot snapshot2 = TestSnapshots.getSnapshot(TestSnapshots.ORACLE_JDK7_21_64BIT_HPROFAGENT, options, true);
		assertTrue(
				snapshot1.getSnapshotInfo().getNumberOfObjects() != snapshot2.getSnapshotInfo().getNumberOfObjects());

	}
}
