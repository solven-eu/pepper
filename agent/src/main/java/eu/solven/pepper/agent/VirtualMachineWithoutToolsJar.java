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
package eu.solven.pepper.agent;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Sets;

import net.bytebuddy.agent.ByteBuddyAgent.AttachmentProvider.Accessor;

/**
 * Gives access to the VirtualMachine object. It may not be available if tools.jar is not made available. Tools.jar is
 * made available by ensuring JAVA_HOME targets a jdk
 *
 * @see <a href=
 *      "http://java.sun.com/javase/6/docs/jdk/api/attach/spec/com/sun/tools/attach/VirtualMachine.html#attach(java.lang.String)"
 *      >VirtualMachine</a>
 *
 * @author Benoit Lacelle
 */
// https://github.com/javamelody/javamelody/blob/master/javamelody-core/src/main/java/net/bull/javamelody/VirtualMachine.java
@SuppressWarnings({ "PMD.AvoidSynchronizedAtMethodLevel", "PMD.GodClass" })
public class VirtualMachineWithoutToolsJar {
	private static final int JDK_9_VERSION = 9;

	private static final String ENV_JAVA_SPECIFICATION_VERSION = "java.specification.version";

	protected static final Logger LOGGER = LoggerFactory.getLogger(VirtualMachineWithoutToolsJar.class);

	// http://cr.openjdk.java.net/~malenkov/8022746.8.1/jdk/src/share/classes/sun/tools/jmap/JMap.java.html
	private static final String LIVE_OBJECTS_OPTION = "-live";
	private static final String ALL_OBJECTS_OPTION = "-all";

	private static final Set<Class<?>> REPORTED_ERRORS_FOR_VM = Sets.newConcurrentHashSet();

	// Switched to true if incompatible JVM, or attach failed
	private static final AtomicBoolean WILL_NOT_WORK = new AtomicBoolean(false);

	private static final AtomicReference<Class<?>> JVM_VIRTUAL_MACHINE_CLASS = new AtomicReference<Class<?>>();
	private static final AtomicReference<Object> JVM_VIRTUAL_MACHINE = new AtomicReference<Object>();

	// https://stackoverflow.com/questions/2591083/getting-java-version-at-runtime
	// https://stackoverflow.com/questions/5103121/how-to-find-the-jvm-version-from-a-program
	public static final boolean IS_JDK_9 = "9".equals(System.getProperty(ENV_JAVA_SPECIFICATION_VERSION));
	public static final boolean IS_JDK_11 = "11".equals(System.getProperty(ENV_JAVA_SPECIFICATION_VERSION));

	public static final boolean IS_JDK_9_OR_LATER = isJdk9OrLater();
	public static final boolean IS_VIRTUAL_MACHINE_ELIGIBLE = !isJdk9OrLater() || isAllowAttachSelf();

	protected VirtualMachineWithoutToolsJar() {
		// hidden
	}

	private static boolean isJdk9OrLater() {
		String specificationVersion = System.getProperty(ENV_JAVA_SPECIFICATION_VERSION);
		try {
			if (specificationVersion.indexOf('.') >= 0) {
				// Before jdk8, versions are '1.8'...
				return false;
			} else {
				// After jdk9, versions are '9', '10', ...
				int asInt = Integer.parseInt(specificationVersion);

				if (asInt >= JDK_9_VERSION) {
					LOGGER.debug("{} is jdk9 or later", specificationVersion);
					return true;
				} else {
					return false;
				}
			}
		} catch (RuntimeException e) {
			LOGGER.trace("Not 9 or later", e);
			return false;
		}
	}

	// https://stackoverflow.com/questions/56787777/virtualmachine-attachpid-fails-with-java-io-ioexception-can-not-attach-to-cur
	private static boolean isAllowAttachSelf() {
		String allowAttachSelf = System.getProperty("jdk.attach.allowAttachSelf");

		return "true".equals(allowAttachSelf);
	}

	/**
	 * @return true if heap histogram is supported
	 */
	static boolean isJmapSupported() {
		// pour nodes Hudson/Jenkins, on réévalue sans utiliser de constante
		final String javaVendor = getJavaVendor();
		// http://www.oracle.com/technetwork/middleware/jrockit/overview/index.html
		// https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/make/tools/manifest.mf
		return javaVendor.contains("Sun") || javaVendor.contains("Oracle")
				|| javaVendor.contains("AdoptOpenJDK")
				|| javaVendor.contains("Apple")
				|| javaVendor.contains("Eclipse Foundation")
				|| isJRockit();
	}

	/**
	 * @return true if current JVM is a JRockIt JVM
	 */
	public static boolean isJRockit() {
		return getJavaVendor().contains("BEA");
	}

	public static synchronized Optional<Object> getJvmVirtualMachine() {
		try {
			return getUnsafeJvmVirtualMachine();
		} catch (Throwable e) {
			if (REPORTED_ERRORS_FOR_VM.add(e.getClass())) {
				// First encounter of this error
				LOGGER.warn("Issue while loading VirtualMachine. java.vendor={} java.spec={}",
						getJavaVendor(),
						getJavaSpecification(),
						e);
			} else {
				// This error has already been reported
				LOGGER.trace("Issue while loading VirtualMachine. java.vendor={} java.spec={}",
						getJavaVendor(),
						getJavaSpecification(),
						e);
			}
			return Optional.absent();
		}
	}

	@Beta
	public static synchronized List<?> getJvmVirtualMachines() {
		final Optional<? extends Class<?>> virtualMachineClass = findVirtualMachineClass();

		if (!virtualMachineClass.isPresent()) {
			return Collections.emptyList();
		}

		try {
			// http://hg.openjdk.java.net/jdk8/jdk8/jdk/file/687fd7c7986d/src/windows/classes/sun/tools/attach/WindowsAttachProvider.java
			Method m = virtualMachineClass.get().getDeclaredMethod("list");

			// List of VirtualMachineDescriptor
			Object list = m.invoke(null);

			if (list == null) {
				return Collections.emptyList();
			} else {
				List<?> vmDescriptions = (List<?>) list;

				return Collections.unmodifiableList(vmDescriptions);
			}
		} catch (Throwable e) {
			LOGGER.trace("Ouch", e);
			return Collections.emptyList();
		}
	}

	public static synchronized Optional<Object> getUnsafeJvmVirtualMachine() throws ClassNotFoundException,
			MalformedURLException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		if (WILL_NOT_WORK.get()) {
			return Optional.absent();
		} else if (!IS_VIRTUAL_MACHINE_ELIGIBLE) {
			// We do not even try loading the VirtualMachine if it is not eligible (jdk9+ and lack of explicit property)
			LOGGER.warn(
					"VirtualMachine is not eligible. java.vendor={} java.spec={} '-Djdk.attach.allowAttachSelf=true'={}",
					getJavaVendor(),
					getJavaSpecification(),
					isAllowAttachSelf());
			WILL_NOT_WORK.set(true);

			return Optional.absent();
		}

		// https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/src/share/classes/sun/tools/attach/HotSpotVirtualMachine.java
		// https://github.com/openjdk-mirror/jdk7u-jdk/blob/master/src/windows/classes/sun/tools/attach/WindowsVirtualMachine.java
		// http://hg.openjdk.java.net/jdk8/jdk8/jdk/file/687fd7c7986d/src/windows/classes/sun/tools/attach/WindowsVirtualMachine.java
		if (JVM_VIRTUAL_MACHINE.get() == null) {
			final Optional<? extends Class<?>> virtualMachineClass = findVirtualMachineClass();

			if (virtualMachineClass.isPresent()) {
				Class<?> vmClass = virtualMachineClass.get();
				final Method attachMethod = vmClass.getMethod("attach", String.class);
				final String pid = PepperAgentHelper.getPIDForAgent();
				try {
					JVM_VIRTUAL_MACHINE.set(attachMethod.invoke(null, pid));
				} finally {
					if (JVM_VIRTUAL_MACHINE.get() == null) {
						LOGGER.warn(
								"Issue while loading VirtualMachine. java.vendor={} java.spec={} '-Djdk.attach.allowAttachSelf=true'={}",
								getJavaVendor(),
								getJavaSpecification(),
								isAllowAttachSelf());
						WILL_NOT_WORK.set(true);
					} else {
						LOGGER.trace("VirtualMachine has been loaded: {}. Available methods: {}",
								vmClass.getName(),
								Arrays.asList(vmClass.getMethods()));
					}
				}
			}
		}
		return Optional.fromNullable(JVM_VIRTUAL_MACHINE.get());
	}

	/**
	 * Soft access to com.sun.tools.attach.VirtualMachine, as it may not be available in the classpath
	 *
	 * @return if available, the Class of the VirtualMachine object
	 */
	public static synchronized Optional<? extends Class<?>> findVirtualMachineClass() {
		if (JVM_VIRTUAL_MACHINE_CLASS.get() == null) {
			try {
				Accessor attempt = InstrumentationAgent.safeGetDefaultAttempt();
				if (attempt.isAvailable()) {
					JVM_VIRTUAL_MACHINE_CLASS.set(attempt.getVirtualMachineType());
				}
			} catch (Throwable e) {
				// We log in trace to prevent showing this alarming stack too often
				LOGGER.trace("Issue while getting VirtualMachine class", e);
				return Optional.absent();
			}
		}

		return Optional.fromNullable(JVM_VIRTUAL_MACHINE_CLASS.get());
	}

	/**
	 * Force detaching the VirtualMachine object
	 *
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static synchronized void detach()
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		// Ensure VirtualMachine reference will not be used anymore
		Object localRef = JVM_VIRTUAL_MACHINE.getAndSet(null);
		if (localRef != null) {
			// We have an attached VirtualMachine : detach it
			final Method detachMethod = localRef.getClass().getMethod("detach");
			detachMethod.invoke(localRef);
		}
	}

	/**
	 * Dump an histogram of the objects in the heap. This could refers Object which are electable for GC, but not GCed
	 * yet
	 *
	 * @return The output histogram as produced by 'jmap -histo'
	 */
	public static Optional<InputStream> heapHisto() {
		// We follow the default behavior: all objects in heap (even those dead)
		boolean allObjectsElseLive = true;
		return heapHisto(allObjectsElseLive);
	}

	/**
	 *
	 * @param allObjectsElseLive
	 *            if true, we return all objects in the heap, else only the live objects (which requires the equivalent
	 *            of a full-GC)
	 * @return An {@link java.util.Optional} {@link InputStream} of the heapHistogram
	 */
	public static Optional<InputStream> heapHisto(final boolean allObjectsElseLive) {
		Optional<InputStream> asInputStream = getJvmVirtualMachine().transform(new Function<Object, InputStream>() {

			@Override
			public InputStream apply(Object vm) {
				if (!allObjectsElseLive) {
					LOGGER.warn(".heapHisto with allObjectsElseLive=false will trigger a full-GC");
				}
				try {
					return invokeForInputStream(vm, "heapHisto", ALL_OBJECTS_OPTION);
				} catch (Throwable e) {
					throw new RuntimeException("Issue on invoking 'heapHisto -all'", e);
				}
			}

		});

		return asInputStream;
	}

	/**
	 *
	 * @param allObjectsElseLive
	 * @return if true, use "-all" option, else use "-live" but beware it will trigger a full GC
	 */
	public static Optional<InputStream> heapDump(final File targetFile, final boolean allObjectsElseLive) {
		final File absoluteFile = targetFile.getAbsoluteFile();
		if (absoluteFile.exists()) {
			throw new IllegalArgumentException("Can not write heap-dump as file already exists: " + absoluteFile);
		}

		Optional<InputStream> asInputStream = getJvmVirtualMachine().transform(new Function<Object, InputStream>() {

			@Override
			public InputStream apply(Object vm) {
				if (!allObjectsElseLive) {
					LOGGER.warn(".heapDump with allObjectsElseLive=false will trigger a full-GC");
				}
				String option = getAllOrLiveOption(allObjectsElseLive);
				try {
					return invokeForInputStream(vm, "dumpHeap", absoluteFile.getPath(), option);
				} catch (Throwable e) {
					throw new RuntimeException("Issue on invoking 'dumpHeap " + option + "'", e);
				}
			}

		});

		return asInputStream;
	}

	/**
	 *
	 * @param allObjectsElseLive
	 *            if true, one want jmap to export all objects available in the JVM. If false, jmap shall keep only live
	 *            object, which will require a full GC
	 * @return the jmap String option associated to the expected behavior
	 */
	protected static String getAllOrLiveOption(boolean allObjectsElseLive) {
		if (allObjectsElseLive) {
			return ALL_OBJECTS_OPTION;
		} else {
			return LIVE_OBJECTS_OPTION;
		}
	}

	/**
	 * @param methodName
	 *            the methodName
	 * @param argument
	 * @return
	 */
	protected static InputStream invokeForInputStream(String methodName, String... argument) {
		try {
			final Class<?> virtualMachineClass = getJvmVirtualMachine().getClass();

			return invokeForInputStream(virtualMachineClass, methodName, argument);
		} catch (final ClassNotFoundException e) {
			throw new UnsupportedOperationException("You should use a JDK instead of a JRE", e);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param virtualMachine
	 * @param methodName
	 * @param argument
	 * @return
	 * @throws NoSuchMethodException
	 * @throws MalformedURLException
	 * @throws ClassNotFoundException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	protected static InputStream invokeForInputStream(Object virtualMachine, String methodName, String... argument)
			throws IllegalAccessException, InvocationTargetException, ClassNotFoundException, MalformedURLException,
			NoSuchMethodException {
		if (virtualMachine == null) {
			throw new IllegalArgumentException("VirtualMachine is null");
		}

		Class<?> vmClass = virtualMachine.getClass();

		// https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/tooldescr014.html#BABJIIHH
		// http://docs.oracle.com/javase/7/docs/technotes/tools/share/jmap.html
		final Method methodForInputStream = vmClass.getMethod(methodName, Object[].class);

		LOGGER.info("About to invoke {} on {}", methodName, vmClass);
		return (InputStream) methodForInputStream.invoke(virtualMachine, new Object[] { argument });
	}

	private static String getJavaVendor() {
		return System.getProperty("java.vendor");
	}

	private static String getJavaSpecification() {
		return System.getProperty(ENV_JAVA_SPECIFICATION_VERSION);
	}

	public static boolean isVirtualMachineAvailable() {
		try {
			if (getJvmVirtualMachine().isPresent()) {
				return true;
			} else {
				LOGGER.trace("VirtualMachine is null");
				return false;
			}
		} catch (Throwable e) {
			// Whatever the reason is, the VirtualMachine is not available. It
			// could be an error if we load from an incompatible java version
			LOGGER.trace("VirtualMachine is not available", e);
			return false;
		}
	}
}
