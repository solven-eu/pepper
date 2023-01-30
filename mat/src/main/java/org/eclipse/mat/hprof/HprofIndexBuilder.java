/**
 * The MIT License
 * Copyright (c) 2008-2013 Benoit Lacelle - SOLVEN
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
package org.eclipse.mat.hprof;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.collect.IteratorLong;
import org.eclipse.mat.hprof.extension.IParsingEnhancer;
import org.eclipse.mat.hprof.ui.HprofPreferences;
import org.eclipse.mat.parser.IIndexBuilder;
import org.eclipse.mat.parser.IPreliminaryIndex;
import org.eclipse.mat.parser.index.IIndexReader.IOne2LongIndex;
import org.eclipse.mat.parser.index.IndexWriter;
import org.eclipse.mat.util.IProgressListener;
import org.eclipse.mat.util.MessageUtil;
import org.eclipse.mat.util.SimpleMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HprofIndexBuilder implements IIndexBuilder {

	protected static final Logger LOGGER = LoggerFactory.getLogger(HprofIndexBuilder.class);

	private File file;
	private String prefix;
	private IOne2LongIndex id2position;
	private List<IParsingEnhancer> enhancers;

	@Override
	public void init(File file, String prefix) {
		this.file = file;
		this.prefix = prefix;

		this.enhancers = new ArrayList<IParsingEnhancer>();
		// for (EnhancerRegistry.Enhancer enhancer : EnhancerRegistry.instance().delegates())
		// {
		// IParsingEnhancer parsingEnhancer = enhancer.parser();
		// if (parsingEnhancer != null)
		// this.enhancers.add(parsingEnhancer);
		// }

	}

	@Override
	public void fill(IPreliminaryIndex preliminary, IProgressListener listener) throws SnapshotException, IOException {
		HprofPreferences.HprofStrictness strictnessPreference = HprofPreferences.getCurrentStrictness();

		SimpleMonitor monitor = new SimpleMonitor(MessageUtil.format(Messages.HprofIndexBuilder_Parsing,
				new Object[] { file.getAbsolutePath() }), listener, new int[] { 500, 1500 });

		listener.beginTask(MessageUtil.format(Messages.HprofIndexBuilder_Parsing, file.getName()), 3000);

		IHprofParserHandler handler = new HprofParserHandlerImpl();
		handler.beforePass1(preliminary.getSnapshotInfo());

		SimpleMonitor.Listener mon = (SimpleMonitor.Listener) monitor.nextMonitor();
		mon.beginTask(MessageUtil.format(Messages.HprofIndexBuilder_Scanning, new Object[] { file.getAbsolutePath() }),
				(int) (file.length() / 1000));
		Pass1Parser pass1 = new Pass1Parser(handler, mon, strictnessPreference);
		Serializable id = preliminary.getSnapshotInfo().getProperty("$runtimeId");
		String dumpNrToRead;
		if (id instanceof String) {
			dumpNrToRead = (String) id;
		} else {
			dumpNrToRead = pass1.determineDumpNumber();
		}
		pass1.read(file, dumpNrToRead);

		if (listener.isCanceled())
			throw new IProgressListener.OperationCanceledException();

		mon.done();

		handler.beforePass2(listener);

		mon = (SimpleMonitor.Listener) monitor.nextMonitor();
		mon.beginTask(
				MessageUtil.format(Messages.HprofIndexBuilder_ExtractingObjects,
						new Object[] { file.getAbsolutePath() }),
				(int) (file.length() / 1000));

		Pass2Parser pass2 = new Pass2Parser(handler, mon, strictnessPreference);
		pass2.read(file, dumpNrToRead);

		if (listener.isCanceled())
			throw new IProgressListener.OperationCanceledException();

		mon.done();

		if (listener.isCanceled())
			throw new IProgressListener.OperationCanceledException();

		for (IParsingEnhancer enhancer : enhancers)
			enhancer.onParsingCompleted(handler.getSnapshotInfo());

		id2position = handler.fillIn(preliminary);
	}

	@Override
	public void clean(final int[] purgedMapping, IProgressListener listener) throws IOException {

		// //////////////////////////////////////////////////////////////
		// object 2 hprof position
		// //////////////////////////////////////////////////////////////

		File indexFile = new File(prefix + "o2hprof.index");
		listener.subTask(
				MessageUtil.format(Messages.HprofIndexBuilder_Writing, new Object[] { indexFile.getAbsolutePath() }));
		IOne2LongIndex newIndex =
				new IndexWriter.LongIndexStreamer().writeTo(indexFile, new IndexIterator(id2position, purgedMapping));

		try {
			newIndex.close();
		} catch (IOException ignore) {
			LOGGER.trace("Ouch", ignore);
		}

		try {
			id2position.close();
		} catch (IOException ignore) {
			LOGGER.trace("Ouch", ignore);
		}

		id2position.delete();
		id2position = null;
	}

	@Override
	public void cancel() {
		if (id2position != null) {
			try {
				id2position.close();
			} catch (IOException ignore) {
				// $JL-EXC$
			}
			id2position.delete();
		}
	}

	private static final class IndexIterator implements IteratorLong {
		private final IOne2LongIndex id2position;
		private final int[] purgedMapping;
		private int nextIndex = -1;

		private IndexIterator(IOne2LongIndex id2position, int[] purgedMapping) {
			this.id2position = id2position;
			this.purgedMapping = purgedMapping;
			findNext();
		}

		@Override
		public boolean hasNext() {
			return nextIndex < purgedMapping.length;
		}

		@Override
		public long next() {
			long answer = id2position.get(nextIndex);
			findNext();
			return answer;
		}

		protected void findNext() {
			nextIndex++;
			while (nextIndex < purgedMapping.length && purgedMapping[nextIndex] < 0)
				nextIndex++;
		}
	}
}
