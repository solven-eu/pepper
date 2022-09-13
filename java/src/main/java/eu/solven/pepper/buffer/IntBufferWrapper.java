package eu.solven.pepper.buffer;

import java.nio.IntBuffer;

/**
 * Wraps an {@link IntBuffer}
 *
 * @author Benoit Lacelle
 *
 */
public class IntBufferWrapper implements IIntBufferWrapper {

	private final IntBuffer asIntBuffer;

	public IntBufferWrapper(IntBuffer asIntBuffer) {
		this.asIntBuffer = asIntBuffer;
	}

	@Override
	public void put(int index, int value) {
		asIntBuffer.put(index, value);
	}

	@Override
	public int capacity() {
		return asIntBuffer.capacity();
	}

	@Override
	public IntBuffer getFirstRawBuffer() {
		return asIntBuffer;
	}

	@Override
	public int get(int index) {
		return asIntBuffer.get(index);
	}

}
