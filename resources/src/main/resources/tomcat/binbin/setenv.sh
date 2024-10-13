#! /bin/sh

# discourage address map swapping by setting Xms and Xmx to the same value
# http://confluence.atlassian.com/display/DOC/Garbage+Collector+Performance+Issues
export CATALINA_OPTS="$CATALINA_OPTS -Xms200g"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx200g"
export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxDirectMemorySize=500g"
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UseNUMA"

# Increase maximum perm size for web base applications to 4x the default amount
# http://wiki.apache.org/tomcat/FAQ/Memoryhttp://wiki.apache.org/tomcat/FAQ/Memory
export CATALINA_OPTS="$CATALINA_OPTS -XX:MaxPermSize=256m"

# Reset the default stack size for threads to a lower value (by 1/10th original)
# By default this can be anywhere between 512k -> 1024k depending on x32 or x64
# bit Java version.
# http://www.springsource.com/files/uploads/tomcat/tomcatx-large-scale-deployments.pdf
# http://www.oracle.com/technetwork/java/hotspotfaq-138619.html
export CATALINA_OPTS="$CATALINA_OPTS -Xss228k"

# Disable remote (distributed) garbage collection by Java clients
# and remove ability for applications to call explicit GC collection
export CATALINA_OPTS="$CATALINA_OPTS -XX:+DisableExplicitGC"

# Enable Java Mission Control http://docs.oracle.com/cd/E15289_01/doc.40/e15070/usingjfr.htm
export CATALINA_OPTS="$CATALINA_OPTS -XX:+UnlockCommercialFeatures -XX:+FlightRecorder"

# Prevent stack-traces to be cut-down by the JVM
export CATALINA_OPTS="$CATALINA_OPTS -XX:-OmitStackTraceInFastThrow"

# Enable tomcat debug
export CATALINA_OPTS="$CATALINA_OPTS -Xdebug -Xrunjdwp:transport=dt_socket,address=8001,server=y,suspend=n"

# Enable JMX
export CATALINA_OPTS="$CATALINA_OPTS -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=1089 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"

# Add GC logs
# <%p> will add the PID in the gcLogFile
# <%t> will add the startup date in the gcLogFile
# '../logs/' to write in tomcat default logs folder
export CATALINA_OPTS="$CATALINA_OPTS -Xloggc:../logs/jvm_gc.%p.%t.log -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintGCDetails"

export CATALINA_OPTS="$CATALINA_OPTS -XX:FlightRecorderOptions=defaultrecording=true,disk=true,repository=../logs/jfr,maxage=6h,settings=default"

# Ensure a .pid file is created
export CATALINA_PID="$CATALINA_BASE/bin/catalina.pid"

# Check for application specific parameters at startup
if [ -r "$CATALINA_BASE/bin/appenv.sh" ]; then
  . "$CATALINA_BASE/bin/appenv.sh"
fi

echo "Using CATALINA_OPTS:"
for arg in $CATALINA_OPTS
do
    echo ">> " $arg
done
echo ""

echo "Using JAVA_OPTS:"
for arg in $JAVA_OPTS
do
    echo ">> " $arg
done
echo "_______________________________________________"
echo ""
