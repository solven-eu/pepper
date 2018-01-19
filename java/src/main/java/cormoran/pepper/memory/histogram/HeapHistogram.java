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
package cormoran.pepper.memory.histogram;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cormoran.pepper.agent.VirtualMachineWithoutToolsJar;
import cormoran.pepper.io.PepperSerializationHelper;

/**
 * Histogramme mémoire.
 * 
 * @author Emeric Vernat
 */
// TODO: restrict to 95% of the Heap
public class HeapHistogram implements IHeapHistogram, Serializable {
	private static final long serialVersionUID = 2163916067335213382L;

	protected static final Logger LOGGER = LoggerFactory.getLogger(HeapHistogram.class);

	private final List<ClassInfo> classes;
	private final List<ClassInfo> permGenClasses;
	private final Date time;
	private long totalHeapBytes;
	private long totalHeapInstances;
	private long totalPermGenBytes;
	private long totalPermgenInstances;
	private boolean sourceDisplayed;

	HeapHistogram(InputStream in, boolean jrockit) {
		time = new Date();
		final Scanner sc = new Scanner(in, JMAP_CHARSET.toString());
		final List<ClassInfo> classInfos = scan(sc, jrockit);

		classes = new ArrayList<ClassInfo>();
		permGenClasses = new ArrayList<ClassInfo>();

		for (final ClassInfo classInfo : classInfos) {
			if (classInfo.isPermGen()) {
				permGenClasses.add(classInfo);
				totalPermGenBytes += classInfo.getBytes();
				totalPermgenInstances += classInfo.getInstancesCount();
			} else {
				classes.add(classInfo);
				totalHeapBytes += classInfo.getBytes();
				totalHeapInstances += classInfo.getInstancesCount();
			}
			if (!sourceDisplayed && classInfo.getSource() != null) {
				sourceDisplayed = true;
			}
		}
		if (!jrockit) {
			sc.next("Total");
			final long totalInstances = sc.nextLong();
			final long totalBytes = sc.nextLong();
			assert totalInstances == totalPermgenInstances + totalHeapInstances;
			assert totalBytes == totalPermGenBytes + totalHeapBytes;
		}
		sort();
	}

	private void addClassInfo(ClassInfo newClInfo, Map<String, ClassInfo> map) {
		final ClassInfo oldClInfo = map.get(newClInfo.getName());
		if (oldClInfo == null) {
			map.put(newClInfo.getName(), newClInfo);
		} else {
			oldClInfo.add(newClInfo);
		}
	}

	protected Date getTime() {
		return time;
	}

	protected List<ClassInfo> getHeapHistogram() {
		return Collections.unmodifiableList(classes);
	}

	protected long getTotalHeapInstances() {
		return totalHeapInstances;
	}

	@Override
	public long getTotalHeapBytes() {
		return totalHeapBytes;
	}

	List<ClassInfo> getPermGenHistogram() {
		return Collections.unmodifiableList(permGenClasses);
	}

	long getTotalPermGenInstances() {
		return totalPermgenInstances;
	}

	long getTotalPermGenBytes() {
		return totalPermGenBytes;
	}

	boolean isSourceDisplayed() {
		return sourceDisplayed;
	}

	private void sort() {
		final Comparator<ClassInfo> classInfoReversedComparator = Collections.reverseOrder(new ClassInfoComparator());
		Collections.sort(permGenClasses, classInfoReversedComparator);
		Collections.sort(classes, classInfoReversedComparator);
	}

	protected void skipHeader(Scanner sc, boolean jrockit) {
		// num #instances #bytes class name
		// --------------------------------------
		sc.nextLine();
		sc.nextLine();
		if (!jrockit) {
			sc.skip("-+");
			sc.nextLine();
		}
	}

	private static final int DECIMAL_RADIX = 10;

	protected List<ClassInfo> scan(Scanner sc, boolean jrockit) {
		final Map<String, ClassInfo> classInfoMap = new HashMap<String, ClassInfo>();
		sc.useRadix(DECIMAL_RADIX);

		skipHeader(sc, jrockit);

		final String nextLine;
		if (jrockit) {
			nextLine = "[0-9.]+%";
		} else {
			// 1: 1414 6013016 [I
			nextLine = "[0-9]+:";
		}
		while (sc.hasNext(nextLine)) {
			final ClassInfo newClInfo = new ClassInfo(sc, jrockit);
			addClassInfo(newClInfo, classInfoMap);
		}
		return new ArrayList<>(classInfoMap.values());
	}

	/**
	 * @return l'histogramme mémoire
	 * @throws IOException
	 */
	public static HeapHistogram createHeapHistogram() throws IOException {
		return VirtualMachineWithoutToolsJar.heapHisto().transform(is -> {
			try (InputStream input = is) {
				return new HeapHistogram(input, VirtualMachineWithoutToolsJar.isJRockit());
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}).orNull();
	}

	public static String createHeapHistogramAsString() throws IOException {
		return VirtualMachineWithoutToolsJar.heapHisto().transform(is -> {
			try (InputStream input = is) {
				return PepperSerializationHelper.toString(input, JMAP_CHARSET);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}).or("Heap Histogram is not available");
	}

	/**
	 * 
	 * @param file
	 * @param gzipped
	 * @return the number of written bytes
	 * @throws Exception
	 */
	public static String saveHeapDump(File file) throws IOException {
		final String output = VirtualMachineWithoutToolsJar.heapDump(file, true).transform(is -> {
			try (InputStream input = is) {
				return PepperSerializationHelper.toString(input, JMAP_CHARSET);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}).or("Heap Histogram is not available");

		if (output.startsWith("Heap dump file created")) {
			LOGGER.info("Heap-Dump seems to have been successfully generated in {}", file);
		}

		return output;
	}
}