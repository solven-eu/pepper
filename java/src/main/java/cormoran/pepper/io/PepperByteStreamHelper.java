package cormoran.pepper.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities related to InputStreaml and {@link OutputStream}
 * 
 * @author Benoit Lacelle
 *
 */
public class PepperByteStreamHelper {

	protected static final Logger LOGGER = LoggerFactory.getLogger(PepperByteStreamHelper.class);

	protected PepperByteStreamHelper() {
		// hidden
	}

	/**
	 * This is useful for tests, as it enable retrieving the avro binary through an InputStream
	 * 
	 * @param rowsToWrite
	 *            the stream of {@link GenericRecord} to write
	 * @param executor
	 *            an Async {@link Executor} supplier. It MUST not be synchronous else the underlying PipedInputStream
	 *            may dead-lock waiting for the PipedInputStream to be filled
	 * @return
	 * @throws IOException
	 */
	public static InputStream toInputStream(OutputStreamConsumer outputStreamConsumer, Supplier<Executor> executor)
			throws IOException {
		AtomicReference<Throwable> throwable = new AtomicReference<>();

		// https://stackoverflow.com/questions/5778658/how-to-convert-outputstream-to-inputstream
		PipedInputStream pis = new PipedInputStream() {
			@Override
			public void close() throws IOException {
				super.close();

				// As the user is expected to call .close on success, we ensure to report the issue
				if (throwable.get() != null) {
					throw new IOException(throwable.get());
				}
			}
		};
		PipedOutputStream pos = new PipedOutputStream(pis);

		executor.get().execute(() -> {
			try {
				outputStreamConsumer.accept(pos);
			} catch (RuntimeException | Error | IOException e) {
				registerAsyncException(throwable, e);
			} finally {
				try {
					pos.close();
				} catch (IOException e) {
					registerAsyncException(throwable, e);
				}
			}
		});

		return pis;
	}

	/**
	 * On asynchronous exceptions, we prefer not to log the whole stack as we should report the exception later: we then
	 * expect not to duplicate stack in logs
	 * 
	 * @param throwable
	 * @param e
	 */
	private static void registerAsyncException(AtomicReference<Throwable> throwable, Throwable e) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.warn("Async exception", e);
		} else {
			// We will re-throw the exception: do not produce a full stack by default else it will be duplicated
			// in the logs
			LOGGER.warn("Async exception: {} ({})", e.getMessage(), e.getClass());
		}
		// Register the issue so that it is re-thrown when the returned InputStream is closed
		throwable.compareAndSet(null, e);
	}
}
