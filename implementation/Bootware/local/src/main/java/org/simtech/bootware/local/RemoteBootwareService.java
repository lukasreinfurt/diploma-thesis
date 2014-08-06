package org.simtech.bootware.local;

import java.net.URL;
import java.util.concurrent.ExecutionException;

import javax.xml.namespace.QName;
import javax.xml.ws.Response;
import javax.xml.ws.Service;

import org.simtech.bootware.core.InformationListWrapper;
import org.simtech.bootware.core.UserContext;
import org.simtech.bootware.core.exceptions.DeployException;
import org.simtech.bootware.core.exceptions.ShutdownException;
import org.simtech.bootware.remote.DeployResponse;
import org.simtech.bootware.remote.ShutdownResponse;

import async.client.RemoteBootware;

public class RemoteBootwareService {

	private URL url;
	private Boolean available = false;
	private RemoteBootware remoteBootware;

	public RemoteBootwareService(final URL url) {
		this.url = url;
		final QName qname = new QName("http://remote.bootware.simtech.org/", "RemoteBootwareImplService");
		final Service service = Service.create(url, qname);
		remoteBootware = service.getPort(RemoteBootware.class);
		available = true;
	}

	public final Boolean isAvailable() {
		return available;
	}

	public final InformationListWrapper deploy(final UserContext context) throws DeployException {
		try {
			final Response<DeployResponse> response = remoteBootware.deployAsync(context);

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

	public final void shutdown() throws ShutdownException {
		try {
			final Response<ShutdownResponse> response = remoteBootware.shutdownAsync();

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
