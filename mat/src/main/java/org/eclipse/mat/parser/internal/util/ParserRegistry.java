/**
 * The MIT License
 * Copyright (c) 2008-2010 Benoit Lacelle - SOLVEN
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
				.filter(p -> Arrays.stream(p.snapshotFormat.getFileExtensions())
						.filter(ext -> name.endsWith(ext))
						.findAny()
						.isPresent())
				.collect(Collectors.toList());
	}

	public static List<SnapshotFormat> getSupportedFormats() {
		return parsers.stream().map(parser -> parser.snapshotFormat).collect(Collectors.toList());
	}

}
