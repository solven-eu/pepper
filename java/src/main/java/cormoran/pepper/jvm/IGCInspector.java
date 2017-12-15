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
package cormoran.pepper.jvm;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import cormoran.pepper.memory.IPepperMemoryConstants;

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

	String getHeapHistogram() throws IOException;

	/**
	 * Save a heap-dump in given map. It is comparable to jmap
	 * (http://docs.oracle.com/javase/7/docs/technotes/tools/share/jmap.html)
	 * 
	 * @param path
	 * @return the output of the command
	 * @throws IOException
	 */
	String saveHeapDump(Path path) throws IOException;

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
