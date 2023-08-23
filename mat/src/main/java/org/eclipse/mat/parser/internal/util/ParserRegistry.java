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
package org.eclipse.mat.parser.internal.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.mat.parser.IIndexBuilder;
import org.eclipse.mat.parser.IObjectReader;
import org.eclipse.mat.snapshot.SnapshotFormat;

public class ParserRegistry {
	public static final String INDEX_BUILDER = "indexBuilder";
	public static final String OBJECT_READER = "objectReader";

	private static final List<Parser> parsers = Arrays.asList(new Parser("hprof",
			new SnapshotFormat("hprof", new String[] { "hprof", "bin" }),
			() -> new org.eclipse.mat.hprof.HprofHeapObjectReader(),
			() -> new org.eclipse.mat.hprof.HprofIndexBuilder()));

	public static class Parser {
		private final String id;
		private final SnapshotFormat snapshotFormat;

		private final Supplier<IObjectReader> objectReader;
		private final Supplier<IIndexBuilder> indexBuilder;

		public Parser(String id,
				SnapshotFormat snapshotFormat,
				Supplier<IObjectReader> objectReader,
				Supplier<IIndexBuilder> indexBuilder) {
			this.id = id;
			this.snapshotFormat = snapshotFormat;

			this.objectReader = objectReader;
			this.indexBuilder = indexBuilder;
		}

		public String getId() {
			return id;
		}

		public String getUniqueIdentifier() {
			return "mat" + "." + id;
		}

		public SnapshotFormat getSnapshotFormat() {
			return snapshotFormat;
		}

		public IObjectReader createObjectReader() {
			return objectReader.get();
		}

		public IIndexBuilder createIndexBuider() {
			return indexBuilder.get();
		}
	}

	public static Parser lookupParser(String uniqueIdentifier) {
		for (Parser p : parsers)
			if (uniqueIdentifier.equals(p.getUniqueIdentifier()))
				return p;
		return null;
	}

	public static List<Parser> matchParser(String name) {
		return parsers.stream()
				.filter(p -> Arrays.stream(p.snapshotFormat.getFileExtensions()).anyMatch(name::endsWith))
				.collect(Collectors.toList());
	}

	public static List<SnapshotFormat> getSupportedFormats() {
		return parsers.stream().map(parser -> parser.snapshotFormat).collect(Collectors.toList());
	}

}
