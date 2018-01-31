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

/**
 * 
 * Typical JVM arguments
 * 
 * Class memory parameterization
 * 
 * -XX:+UseG1GC -XX:+ExplicitGCInvokesConcurrent -Xmx3G -Xms3G -XX:MaxDirectMemorySize=7G -XX:MaxPermSize=512M
 * 
 * java -XX:+PrintFlagsFinal -version > flags.log
 * 
 * 
 * -XX:+PrintGCApplicationStoppedTime - it prints all STW pauses not only related to GC
 * 
 * -XX:+PrintSafepointStatistics - prints safe points details
 * 
 * -XX:PrintSafepointStatisticsCount=1 - make JVM report every safe point
 *
 * 
 * 
 * https://bugs.openjdk.java.net/browse/JDK-6950794
 * 
 * Minimum logs in sysout
 * 
 * -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps
 * 
 * For -Xloggc, %p.%t are very important else gc logs will be overridden on each restart
 *
 * -Xloggc:../log/jvm_gc.%p.%t.log -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCDetails
 * -XX:+PrintClassHistogramBeforeFullGC -XX:+PrintClassHistogramAfterFullGC -XX:+PrintGCApplicationStoppedTime
 * -XX:+PrintSafepointStatistics –XX:PrintSafepointStatisticsCount=1
 * 
 * GC rolling
 * 
 * -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=10 XX:GCLogFileSize=10M
 * 
 * In Prod -XX:-OmitStackTraceInFastThrow will prevent cutting stacks, even if at least the first stack occurrence has
 * been complete http://stackoverflow.com/questions/2411487/nullpointerexception-in-java-with-no-stacktrace
 * 
 * 
 * # CheckIP with: hostname -i
 * 
 * -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1088
 * -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false
 * -Djava.rmi.server.hostname=<OUTPUT OF "hostname -i">
 * 
 * Typical monitoring commands
 * 
 * jstat -gclog <pid>
 * 
 * jstack <pid>
 * 
 * jstack -F <pid>
 * 
 * Add debug in tomcat: "-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
 * 
 * 
 * OS Environment variable: JAVA_TOOL_OPTIONS
 * 
 * http://www.oracle.com/technetwork/java/javase/tech/vmoptions-jsp-140102.html
 * 
 * @author Benoit Lacelle
 *
 */
public interface IPepperJVMConstants {
	/**
	 * 
	 * Enable HeapDump on OutOfMemoryError
	 * 
	 * -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/disk2/dumps
	 * 
	 * jmap <pid>
	 * 
	 * jmap -histo <pid>
	 * 
	 * jmap -histo -F <pid> > some.file
	 * 
	 * jmap -dump:format=b,file=<filename> <pid> -J-Dsun.tools.attach.attachTimeout=<milliseconds>
	 * 
	 * @author Benoit Lacelle
	 *
	 */
	interface IPepperHeapAnalysis {

	}

	/**
	 * 
	 * Enable Java Mission Control http://docs.oracle.com/cd/E15289_01/doc.40/e15070/usingjfr.htm
	 * -XX:+UnlockCommercialFeatures -XX:+FlightRecorder
	 * 
	 * One may want additional Stack-traces out of safe points: -XX:+UnlockDiagnosticVMOptions -XX:+DebugNonSafepoints
	 * 
	 * https://docs.oracle.com/cd/E15289_01/doc.40/e15070/config_rec_data.htm
	 * 
	 * Start from startup. Default conf is in <java_home>\jre\lib\jfr
	 * 
	 * -XX:FlightRecorderOptions=defaultrecording=true -XX:FlightRecorderOptions=defaultrecording=true,settings=default
	 * 
	 * Record for a single unit-test:
	 * -XX:FlightRecorderOptions=defaultrecording=true,disk=true,repository=/tmp,maxage=6h,settings=default
	 * 
	 * @author Benoit Lacelle
	 *
	 */
	interface IPepperJMCConstants {

	}

	/**
	 * 
	 * JIT Class Compilation audit
	 * 
	 * -XX:+UnlockDiagnosticVMOptions -XX:+LogCompilation -XX:+TraceClassLoading
	 * 
	 * Optionally add -XX:+PrintAssembly
	 * 
	 * https://github.com/AdoptOpenJDK/jitwatch/
	 * 
	 * https://github.com/AdoptOpenJDK/jitwatch/wiki/Instructions
	 * 
	 * Profiling based on ThreadDumps http://techblog.netflix.com/2015/07/java-in-flames.html
	 * 
	 * @author Benoit Lacelle
	 *
	 */
	interface IPepperJITAnalysis {

	}

	/**
	 * -DdefaultChunkSize=131072
	 * 
	 * -DbufferAllocatorClass=com.qfs.buf.impl.HeapBufferAllocator
	 * -DchunkAllocatorClass=com.qfs.chunk.buffer.impl.HeapBufferChunkAllocator
	 * -DchunkAllocatorClass=com.qfs.chunk.direct.impl.DirectChunkAllocator
	 * -DchunkAllocatorClass=com.qfs.chunk.direct.impl.MmapDirectChunkAllocator
	 * -DchunkAllocatorClass=com.qfs.chunk.direct.allocator.impl.SlabMemoryAllocator
	 * 
	 * JGroups does not work with IPv6 -Djava.net.preferIPv4Stack=true
	 * 
	 * @author Benoit Lacelle
	 *
	 */
	interface IPepperActivePivot {

	}
}
