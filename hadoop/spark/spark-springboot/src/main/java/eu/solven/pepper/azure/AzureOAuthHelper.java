package eu.solven.pepper.azure;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.DeviceCode;

/**
 * Helps authenticating in Azure
 *
 * @author Benoit Lacelle
 *
 */
public class AzureOAuthHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(AzureOAuthHelper.class);

	private static final String AUTHORITY = "https://login.microsoftonline.com/common/";
	private static final String RESOURCE = "https://storage.azure.com/";

	protected AzureOAuthHelper() {
		// hidden
	}

	// https://cloudarchitected.com/2019/04/accessing-azure-data-lake-storage-gen2-from-clients/
	public static AuthenticationResult getAccessTokenUsingDeviceCodeFlow(String clientId) {
		final ExecutorService service = Executors.newFixedThreadPool(1);

		AuthenticationResult result = null;
		ExecutionException exception;
		try {

			AuthenticationContext context = new AuthenticationContext(AUTHORITY, true, service);

			Future<DeviceCode> future = context.acquireDeviceCode(clientId, RESOURCE, null);
			DeviceCode deviceCode = future.get();
			long expiration = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(deviceCode.getExpiresIn());
			LOGGER.info("DevideCode: {}", deviceCode.getMessage());
			do {
				try {
					Future<AuthenticationResult> futureResult = context.acquireTokenByDeviceCode(deviceCode, null);
					return futureResult.get();
				} catch (ExecutionException ee) {
					exception = ee;
					TimeUnit.SECONDS.sleep(1);
				}
			} while (result == null && System.currentTimeMillis() < expiration);
		} catch (InterruptedException | ExecutionException | MalformedURLException e) {
			throw new RuntimeException(e);
		} finally {
			service.shutdown();
		}
		throw new RuntimeException("Authentication result not received", exception);
	}
}
