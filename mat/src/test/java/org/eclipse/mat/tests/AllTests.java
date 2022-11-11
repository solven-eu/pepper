/*******************************************************************************
 * Copyright (c) 2008, 2021 SAP AG, IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import junit.framework.JUnit4TestAdapter;

@RunWith(Suite.class)
@SuiteClasses({ org.eclipse.mat.tests.collect.CompressedArraysTest.class, //
		org.eclipse.mat.tests.collect.SetIntTest.class, //
		org.eclipse.mat.tests.collect.PrimitiveArrayTests.class, //
		org.eclipse.mat.tests.collect.PrimitiveMapTests.class, //
		org.eclipse.mat.tests.collect.CommandTests.class, //
		org.eclipse.mat.tests.collect.SortTest.class, //
		org.eclipse.mat.tests.collect.ExtractCollectionEntriesTest.class, //
		org.eclipse.mat.tests.parser.TestIndex.class, //
		org.eclipse.mat.tests.parser.TestIndex1to1.class, //
		org.eclipse.mat.tests.snapshot.DominatorTreeTest.class, //
		org.eclipse.mat.tests.snapshot.TestUnreachableObjects.class, //
		org.eclipse.mat.tests.snapshot.GeneralSnapshotTests.class, //
		org.eclipse.mat.tests.snapshot.TestInstanceSizes.class, //
		org.eclipse.mat.tests.snapshot.QueryLookupTest.class, //
		org.eclipse.mat.tests.snapshot.QueriesTest.class, //
		org.eclipse.mat.tests.snapshot.OQLTest.class, //
		org.eclipse.mat.tests.snapshot.MultipleSnapshots.class, //
		org.eclipse.mat.tests.acquire.AcquireDumpTest.class,
		org.eclipse.mat.tests.collect.ExtractCollectionEntriesTest3.class, //
		org.eclipse.mat.tests.collect.ExtractCollectionEntriesTest4.class, //
})
public class AllTests {

	/**
	 * Use for Athena Builds
	 *
	 * @return the test suite
	 */
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(AllTests.class);
	}

}
