package eu.solven.pepper.buffer;

import java.nio.IntBuffer;

/**
 * Wraps at most 2 {@link IntBuffer}
 * 
 * @author Benoit Lacelle
 *
 */
public class IntBufferCompositeWrapper implements IIntBufferWrapper {

	private final IntBuffer asIntBuffer;
	private final IntBuffer second;

	public IntBufferCompositeWrapper(IntBuffer... buffers) {
		if (buffers.length == 0 || buffers.length > 2) {
			throw new UnsupportedOperationException("TODO");
		} else if (buffers.length == 1) {
			asIntBuffer = buffers[0];
			second = IntBuffer.allocate(0);
		} else if (buffers.length == 2) {
			asIntBuffer = buffers[0];
			second = buffers[1];
		} else {
			throw new UnsupportedOperationException("TODO");
		}
	}

	@Override
	public void put(int index, int value) {
		int capacity = asIntBuffer.capacity();
		if (index > capacity) {
			second.put(index - capacity, value);
		} else {
			asIntBuffer.put(index, value);
		}
	}

	@Override
	public int capacity() {
		return asIntBuffer.capacity() + second.capacity();
	}

	@Override
	public IntBuffer getFirstRawBuffer() {
		return asIntBuffer;
	}

	@Override
	public int get(int index) {
		int capacity = asIntBuffer.capacity();
		if (index > capacity) {
			return asIntBuffer.get(index - capacity);
		} else {
			return asIntBuffer.get(index);
		}
	}

}
