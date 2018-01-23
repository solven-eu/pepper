package cormoran.pepper.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * A typicallt {@link OutputStream} {@link Consumer} may throw {@link IOException}
 * 
 * @author Benoit Lacelle
 *
 */
public interface OutputStreamConsumer {
	void accept(OutputStream t) throws IOException;
}
