package cormoran.pepper.buffer;

import java.nio.IntBuffer;

public interface IIntBufferWrapper {

	IntBuffer getFirstRawBuffer();

	int get(int index);

	int capacity();

	void put(int index, int val);

}
