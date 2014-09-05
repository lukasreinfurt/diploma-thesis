package org.simtech.bootware.eclipse;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.xml.namespace.QName;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import org.simtech.bootware.core.ConfigurationListWrapper;
import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.UserContext;
import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.SetConfigurationException;
import org.simtech.bootware.core.exceptions.ShutdownException;
import org.simtech.bootware.local.DeployResponse;
import org.simtech.bootware.local.IsReadyResponse;
import org.simtech.bootware.local.SetConfigurationResponse;
import org.simtech.bootware.local.ShutdownResponse;

import async.client.LocalBootware;

/**
 * A wrapper class around the web service calls to the local bootware.
 */
public class LocalBootwareService {

	private URL url;
	private Boolean available = false;
	private LocalBootware localBootware;

	/**
	 * Connect to the local bootware at the given URL.
	 *
	 * @param url The URL of the local bootware.
	 */
	public LocalBootwareService(final URL url) {
		this.url = url;
		final QName qname = new QName("http://local.bootware.simtech.org/", "LocalBootwareImplService");
		final Service service = Service.create(url, qname);
		localBootware = service.getPort(LocalBootware.class);
		available = true;
	}

	/**
	 * Return the availability of the local bootware service.
	 *
	 * @return True if the local bootware is available, false otherwise.
	 */
	public final Boolean isAvailable() {
		return available;
	}

	/**
	 * Calls the isReady operation of the local bootware.
	 * <p>
	 * This implementation uses asynchronous polling for the call.
	 */
	public final Boolean isReady() {
		try {
			final Response<IsReadyResponse> response = localBootware.isReadyAsync();

			while (!response.isDone()) {
				final Integer time = 1000;
				Thread.sleep(time);
			}

			return response.get().isReturn();
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		catch (ExecutionException e) {
			return false;
		}
	}

	/**
	 * Calls the deploy operation of the local bootware.
	 * <p>
	 * This implementation uses asynchronous polling for the call.
	 *
	 * @param context The user context that describes the application and resource that should be deployed.
	 *
	 * @return A wrapper object that contains a map of strings of information about the deployed application.
	 *
	 * @throws DeployException If there was an error during the deploy process.
	 */
	public final InformationListWrapper deploy(final UserContext context) throws DeployException {
		try {
			final Response<DeployResponse> response = localBootware.deployAsync(context);

			while (!response.isDone()) {
				final Integer time = 1000;
				Thread.sleep(time);
			}

			return response.get().getReturn();
		}
		catch (InterruptedException e) {
			throw new DeployException(e);
		}
		catch (ExecutionException e) {
			throw new DeployException(e);
		}
	}

	/**
	 * Calls the setConfiguration operation of the local bootware.
	 * <p>
	 * This implementation uses asynchronous polling for the call.
	 *
	 * @param configurationListWrapper A wrapper object containing the configuration list that should be set as new default Configuration.
	 *
	 * @throws SetConfigurationException If there was an error while setting the configuration.
	 */
	public final void setConfiguration(final ConfigurationListWrapper configurationListWrapper) throws SetConfigurationException {
		try {
			final Response<SetConfigurationResponse> response = localBootware.setConfigurationAsync(configurationListWrapper);

			while (!response.isDone()) {
				final Integer time = 1000;
				Thread.sleep(time);
			}
		}
		catch (InterruptedException e) {
			throw new SetConfigurationException(e);
		}
	}

	/**
	 * Calls the shutdown operation of the local bootware.
	 * <p>
	 * This implementation uses asynchronous polling for the call.
	 *
	 * @throws ShutdownException If there was an error during the shutdown process.
	 */
	public final void shutdown() throws ShutdownException {
		try {
			final Response<ShutdownResponse> response = localBootware.shutdownAsync();

			while (!response.isDone()) {
				final Integer time = 1000;
				Thread.sleep(time);
			}
		}
		catch (InterruptedException e) {
			throw new ShutdownException(e);
		}
	}

}
