/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation/Andrew Johnson - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.internal.collectionextract;

import java.util.Map.Entry;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.collect.ArrayInt;
import org.eclipse.mat.inspections.collectionextract.ExtractedMap;
import org.eclipse.mat.inspections.collectionextract.ICollectionExtractor;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.model.IObjectArray;

public class ValuesCollectionExtractor extends WrapperCollectionExtractor {
	public ValuesCollectionExtractor(String field) {
		this(field, null);
	}

	public ValuesCollectionExtractor(String field, ICollectionExtractor extractor) {
		super(field, extractor);
	}

	public int[] extractEntryIds(IObject coll) throws SnapshotException {
		ExtractedMap em = extractMap(coll);
		ArrayInt a = new ArrayInt();
		if (em != null) {
			for (Entry<IObject, IObject> en : em) {
				IObject value = en.getValue();
				if (value != null)
					a.add(value.getObjectId());
			}
		}
		return a.toArray();
	}

	public boolean hasExtractableArray() {
		return false;
	}

	public IObjectArray extractEntries(IObject coll) throws SnapshotException {
		throw new UnsupportedOperationException();
	}
}
