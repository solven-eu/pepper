// https://www.baeldung.com/java-modularity
// `open` as we do not know what we're doing while setting-up this module
open module eu.solven.pepper.agent {
	// https://stackoverflow.com/questions/62971569/using-the-attach-api-in-java
	requires transitive jdk.attach;
	
	requires org.slf4j;
	// eu.solven.pepper.agent.InstrumentationAgent.getInstrumentation()
	requires transitive java.instrument;
	requires net.bytebuddy.agent;
	requires java.management;
	// jar --file=/Users/blacelle/.m2/repository/org/ehcache/sizeof/0.4.4/sizeof-0.4.4.jar --describe-module
	// https://stackoverflow.com/questions/53246066/compile-module-that-depends-on-an-external-jar
//	requires org.ehcache.sizeof;

	exports eu.solven.pepper.agent;
}