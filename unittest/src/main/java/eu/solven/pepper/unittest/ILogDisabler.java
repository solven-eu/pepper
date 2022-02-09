package eu.solven.pepper.unittest;

/**
 * Enable disabling a log in a try-with-resources block
 * 
 * @author Benoit Lacelle
 *
 */
public interface ILogDisabler extends AutoCloseable {
	@Override
	void close();
}
