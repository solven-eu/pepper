package eu.solven.pepper.buffer;

import java.nio.IntBuffer;

/**
 * Wrap an {@link IntBuffer}
 * 
 * @author Benoit Lacelle
 *
 */
public interface IIntBufferWrapper {

	IntBuffer getFirstRawBuffer();

	int get(int index);

	int capacity();

	void put(int index, int val);

}
