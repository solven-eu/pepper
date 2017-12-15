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
package cormoran.pepper.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.CharStreams;
import com.google.common.io.LineProcessor;

import cormoran.pepper.memory.PepperMemoryHelper;

/**
 * Various helpers for Process launched through a ProcessBuilder
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperProcessHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperProcessHelper.class);

	protected static final int OS_MARKER_LINUX = 0;
	protected static final int OS_MARKER_WINDOWS = 1;
	protected static final int OS_MARKER_MAC = 2;

	// UNIXProcess class is loaded only udner a linux environment
	private static final String CLASS_PROCESS_UNIX = "java.lang.UNIXProcess";

	// Deprecated in Java9
	public static synchronized long getPidOfProcess(Process p) {
		long pid = -1;

		try {
			if (isUnixProcess(p.getClass().getName())) {
				Field f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				pid = f.getLong(p);
				f.setAccessible(false);
			}
		} catch (Exception e) {
			pid = -1;
		}
		return pid;
	}

	// Used to prevent Sonar complaining about not using instanceof
	private static boolean isUnixProcess(String className) {
		return className.equals(CLASS_PROCESS_UNIX);
	}

	/**
	 * 
	 * @param pid
	 *            the PID of the process to consider
	 * @return the number of bytes in the resident memory of given process
	 * @throws IOException
	 */
	public static OptionalLong getProcessResidentMemory(long pid) throws IOException {
		ProcessBuilder memoryBuilder;

		// https://stackoverflow.com/questions/131303/how-to-measure-actual-memory-usage-of-an-application-or-process
		String pidAsString = Long.toString(pid);
		if (SystemUtils.IS_OS_MAC) {
			memoryBuilder = new ProcessBuilder("/usr/bin/vmmap", "-summary", pidAsString);
		} else if (SystemUtils.IS_OS_WINDOWS) {
			// https://stackoverflow.com/questions/54686/how-to-get-a-list-of-current-open-windows-process-with-java
			// /nh: no header
			memoryBuilder = new ProcessBuilder("tasklist.exe", "/fi", "PID eq " + pidAsString, "/fo", "csv", "/nh");
		} else {
			// In heroku, no need for sudo
			// We need -X to be able to find the resident memory
			// https://stackoverflow.com/questions/131303/how-to-measure-actual-memory-usage-of-an-application-or-process
			// https://linux.die.net/man/1/pmap
			memoryBuilder = new ProcessBuilder("pmap", "-x", pidAsString);
		}

		// SImplify output by redirecting all stream in the same place
		memoryBuilder.redirectErrorStream(true);

		LOGGER.debug("About to execute {}", PepperProcessHelper.getCommandAsString(memoryBuilder.command()));

		Process memoryProcess = memoryBuilder.start();

		InputStream inputStream = memoryProcess.getInputStream();

		int osFlag;
		if (SystemUtils.IS_OS_LINUX) {
			osFlag = OS_MARKER_LINUX;
		} else if (SystemUtils.IS_OS_WINDOWS) {
			osFlag = OS_MARKER_WINDOWS;
		} else if (SystemUtils.IS_OS_MAC) {
			osFlag = OS_MARKER_MAC;
		} else {
			LOGGER.trace("Unknown OS: {}", SystemUtils.OS_NAME);
			return OptionalLong.empty();
		}

		return extractMemory(osFlag, inputStream);
	}

	public static String getCommandAsString(List<String> command) {
		return command.stream().map(s -> {
			if (s.contains(" ")) {
				return "\"" + s + "\"";
			} else {
				return s;
			}
		}).collect(Collectors.joining(" "));
	}

	@VisibleForTesting
	protected static OptionalLong extractMemory(int osFlag, InputStream inputStream) throws IOException {
		LineProcessor<String> processor = new LineProcessor<String>() {
			AtomicReference<String> lastLine = new AtomicReference<>("");

			@Override
			public boolean processLine(String line) throws IOException {
				if (!line.isEmpty()) {
					lastLine.set(line);
				}

				if (osFlag == OS_MARKER_MAC) {
					// MacOS
					if (line.trim().startsWith("TOTAL")) {
						// Stop at the first total
						return false;
					} else {
						return true;
					}
				} else {
					// Move to last row
					return true;
				}
			}

			@Override
			public String getResult() {
				return lastLine.get();
			}
		};

		String lastLine = CharStreams.readLines(new InputStreamReader(inputStream), processor).trim();

		if (lastLine.isEmpty()) {
			LOGGER.trace("Unexpected row: {}", lastLine);
			return OptionalLong.empty();
		} else {
			if (osFlag == OS_MARKER_MAC) {
				// MacOS
				// In mac:
				// mapped file 32.9M 10.7M 32K 0K 0K 0K 0K 139
				// shared memory 44K 44K 44K 0K 0K 0K 0K 6
				// =========== ======= ======== ===== ======= ======== ====== ===== =======
				// TOTAL 2.2G 538.2M 377.3M 0K 0K 16K 0K 845
				// TOTAL, minus reserved VM space 2.2G 538.2M 377.3M 0K 0K 16K 0K 845
				//
				// VIRTUAL RESIDENT DIRTY SWAPPED ALLOCATION BYTES DIRTY+SWAP REGION
				// MALLOC ZONE SIZE SIZE SIZE SIZE COUNT ALLOCATED FRAG SIZE % FRAG COUNT
				// =========== ======= ========= ========= ========= ========= ========= ========= ======
				// ======
				// DefaultMallocZone_0x10b7b6000 203.0M 148.4M 87.4M 0K 167902 64.5M 22.9M 27% 19
				// GFXMallocZone_0x10b7e7000 0K 0K 0K 0K 0 0K 0K 0% 0
				// =========== ======= ========= ========= ========= ========= ========= ========= ======
				// ======
				// TOTAL 203.0M 148.4M 87.4M 0K 167902 64.5M 22.9M 27% 19
				lastLine = lastLine.trim();
				if (lastLine.startsWith("TOTAL")) {
					lastLine = lastLine.substring("TOTAL".length()).trim();

					// Skip the space between total and virtual size
					int firstSpace = lastLine.indexOf(' ');

					if (firstSpace >= 0) {
						lastLine = lastLine.substring(firstSpace + 1).trim();
					} else {
						return OptionalLong.empty();
					}

					// Skip virtual size to move to resident size
					int secondSpace = lastLine.indexOf(' ');
					lastLine = lastLine.substring(0, secondSpace + 1).trim();

					long memory = PepperMemoryHelper.memoryAsLong(lastLine);
					return OptionalLong.of(memory);
				} else {
					LOGGER.trace("Unexpected row: {}", lastLine);
					return OptionalLong.empty();
				}
			} else if (osFlag == OS_MARKER_WINDOWS) {
				// If no process matching pid: INFO: No tasks are running which match the specified criteria.
				// If matching with "/fo csv": "chrome.exe","6740","Console","1","107,940 K"
				// If matching with "/fo table": "chrome.exe\t6740\tConsole\t1\t108,760 K"
				String WINDOWS_MEMORY_PATTERN = "\",\"";

				try {
					int indexLastComa = lastLine.lastIndexOf(WINDOWS_MEMORY_PATTERN);
					if (indexLastComa < 0) {
						return OptionalLong.empty();
					} else {
						String memoryString = lastLine.substring(indexLastComa + WINDOWS_MEMORY_PATTERN.length(),
								lastLine.length() - "\"".length());
						if (memoryString.length() >= 1 && memoryString.charAt(0) == '\"') {
							memoryString = memoryString.substring(1, memoryString.length() - 1);
						}

						long memory = PepperMemoryHelper.memoryAsLong(memoryString);
						return OptionalLong.of(memory);
					}
				} catch (RuntimeException e) {
					throw new RuntimeException("Issue when extracting memory from '" + lastLine + "'", e);
				}
			} else {
				// With 'pmap -x':
				// Address Kbytes RSS Dirty Mode Mapping
				// ...
				// ---------------- ------- ------- -------
				//
				// total kB 4824728 390624 377220

				// With 'pmap', without '-x', Last line is like ' total 65512K'
				lastLine = lastLine.trim();
				if (lastLine.startsWith("total")) {
					lastLine = lastLine.substring("total".length()).replaceAll("\\s+", " ").trim();

					int betweenTotalAndKbytes = lastLine.indexOf(' ');

					long memory;
					if (betweenTotalAndKbytes < 0) {
						memory = PepperMemoryHelper.memoryAsLong(lastLine);
					} else {
						String unit = lastLine.substring(0, betweenTotalAndKbytes);

						int betweenKBytesAndRSS = lastLine.indexOf(' ', betweenTotalAndKbytes + 1);
						// String kBytes = lastLine.substring(betweenKBytesAndRSS + 1, betweenKBytesAndRSS);

						int betweenRSSAndDirty = lastLine.indexOf(' ', betweenKBytesAndRSS + 1);
						String rss = lastLine.substring(betweenKBytesAndRSS + 1, betweenRSSAndDirty);

						memory = PepperMemoryHelper.memoryAsLong(rss + unit);
					}

					return OptionalLong.of(memory);
				} else {
					LOGGER.trace("Unexpected row: {}", lastLine);
					return OptionalLong.empty();
				}
			}
		}
	}

	/**
	 * @return current process PID
	 */
	public static Object getPID() {
		return ManagementFactory.getRuntimeMXBean().getName();
	}

}
