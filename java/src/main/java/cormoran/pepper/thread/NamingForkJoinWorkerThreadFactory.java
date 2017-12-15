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
package cormoran.pepper.thread;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;

/**
 * Enable naming of ForkJoinPool threads
 * 
 * @author Benoit Lacelle
 *
 * @see <a
 *      href="http://stackoverflow.com/questions/34303094/is-it-not-possible-to-supply-a-thread-facory-or-name-pattern-to-forkjoinpool>StackOverFlow</a>
 */
public class NamingForkJoinWorkerThreadFactory implements ForkJoinWorkerThreadFactory {

	protected final String threadPrefix;

	public NamingForkJoinWorkerThreadFactory(String threadPrefix) {
		this.threadPrefix = threadPrefix;
	}

	@Override
	public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
		final ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
		worker.setName(threadPrefix + "-" + worker.getPoolIndex());
		return worker;
	}
}
