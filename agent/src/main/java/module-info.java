// https://www.baeldung.com/java-modularity
// `open` as we do not know what we're doing while setting-up this module
open module eu.solven.pepper.agent {
	// https://stackoverflow.com/questions/62971569/using-the-attach-api-in-java
	requires transitive jdk.attach;

	requires org.slf4j;
	requires com.google.common;
	requires java.instrument;
	requires net.bytebuddy.agent;
	requires java.management;
	//requires org.ehcache.sizeof;

	exports eu.solven.pepper.agent;
}