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
package org.eclipse.mat.tests.collect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.eclipse.mat.collect.IteratorLong;
import org.eclipse.mat.collect.SetLong;
import org.junit.Test;

public class SetLongTest {
	private static final int KEYS = 3000;
	private static final int INITIAL_SIZE = 30;
	private static final int COUNT = 10_000;

	/**
	 * Basic test - no unexpected ArrayIndexOutOfBoundsException
	 */
	@Test
	public void testSetLong0() {
		Random r = new Random(1);
		for (int i = 0; i < COUNT; ++i) {
			int t = 0;
			SetLong ss = new SetLong(r.nextInt(INITIAL_SIZE));
			for (int j = 0; j < KEYS; ++j) {
				int v = r.nextInt(KEYS);
				t += ss.add(v) ? 1 : 0;
			}
			assertTrue("At least one item should have been added", t > 0);
			assertEquals("Added items should equal size", t, ss.size());
		}
	}

	/**
	 * Check that set contains everything it says it has
	 */
	@Test
	public void testSetLong1() {
		Random r = new Random(1);
		for (int i = 0; i < COUNT; ++i) {
			testOneSetLong(r);
		}
	}

	private void testOneSetLong(Random r) {
		SetLong ss = new SetLong(r.nextInt(INITIAL_SIZE));
		for (int j = 0; j < KEYS; ++j) {
			ss.add(r.nextInt(KEYS));
		}
		for (IteratorLong ii = ss.iterator(); ii.hasNext();) {
			assertTrue("every key should be contained", ss.contains(ii.next()));
		}
	}

	/**
	 * Check the number of contained items is the size
	 */
	@Test
	public void testSetLong2() {
		Random r = new Random(1);
		for (int i = 0; i < COUNT; ++i) {
			testTwoSetLong(r);
		}
	}

	private void testTwoSetLong(Random r) {
		SetLong ss = new SetLong(r.nextInt(INITIAL_SIZE));
		for (int j = 0; j < KEYS; ++j) {
			ss.add(r.nextInt(KEYS));
		}
		int t = 0;
		for (int k = 0; k < KEYS; ++k) {
			t += ss.contains(k) ? 1 : 0;
		}
		assertEquals("contained items should equals the size", ss.size(), t);
	}

	/**
	 * Check remove works as expected
	 */
	@Test
	public void testSetLong3() {
		Random r = new Random(1);
		for (int i = 0; i < COUNT; ++i) {
			testThreeSetLong(r);
		}
	}

	private void testThreeSetLong(Random r) {
		SetLong ss = new SetLong(r.nextInt(INITIAL_SIZE));
		for (int j = 0; j < KEYS; ++j) {
			ss.add(r.nextInt(KEYS));
		}
		for (int k = 0; k < KEYS; ++k) {
			boolean b1 = ss.contains(k);
			boolean b2 = ss.remove(k);
			assertEquals("remove should only succeed if key is contained", b1, b2);
			assertFalse("after a remove the key should not be contained", ss.contains(k));
		}
	}

	/**
	 * Check add works as expected
	 */
	@Test
	public void testSetLong4() {
		Random r = new Random(1);
		for (int i = 0; i < COUNT; ++i) {
			testFourSetLong(r);
		}
	}

	private void testFourSetLong(Random r) {
		SetLong ss = new SetLong(r.nextInt(INITIAL_SIZE));
		for (int j = 0; j < KEYS; ++j) {
			ss.add(r.nextInt(KEYS));
		}
		for (int k = 0; k < KEYS; ++k) {
			boolean b1 = ss.contains(k);
			boolean b2 = ss.add(k);
			assertEquals("add should not succeed if the key is already there", b1, !b2);
			assertTrue("key should be contained after an add", ss.contains(k));
		}
	}

}
