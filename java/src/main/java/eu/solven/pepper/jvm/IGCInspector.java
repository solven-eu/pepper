/**
 * The MIT License
 * Copyright (c) 2014 Benoit Lacelle - SOLVEN
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
package eu.solven.pepper.jvm;

import java.util.Map;

import eu.solven.pepper.memory.IPepperMemoryConstants;

/**
 * Interface for GC activity monitoring
 *
 * @author Benoit Lacelle
 *
 */
public interface IGCInspector extends IPepperMemoryConstants {

	/**
	 * We want to see at least 2 digits: after printing 9999B, we print 10KB
	 */
	long BARRIER_FOR_SIZE_IN_LOG = 10;

	String getAndLogCurrentMemoryStatus();

	/**
	 *
	 * @param withoutMonitors
	 *            by default withoutMonitors=true in JConsole MBean for faster access to fasfter method
	 * @return a formatted thread-dump
	 */
	String getAllThreads(boolean withoutMonitors);

	String getAllThreadsSmart(boolean withoutMonitors);

	Map<String, String> getThreadGroupsToAllocatedHeapNiceString();

	Map<String, String> getThreadNameToAllocatedHeapNiceString();

	void clearAllocatedHeapReference();

	void markNowAsAllocatedHeapReference();

	long getMaxHeapGbForHeapHistogram();

	void setMaxHeapGbForHeapHistogram(long maxHeapGbForHeapHistogram);

	long getMarksweepDurationMillisForHeapHistogram();

	void setMarksweepDurationMillisForHeapHistogram(long marksweepDurationMillisForHeapHistogram);

	long getMarksweepDurationMillisForThreadDump();

	void setMarksweepDurationMillisForThreadDump(long marksweepDurationMillisForThreadDump);

}
