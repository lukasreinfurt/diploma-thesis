package org.simtech.bootware.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Implements a simple REST server
 */
@Path("/repository")
public class RestServer {

	public RestServer() {}

	/**
	 * Returns the plugin of the requested type and name if it exists.
	 *
	 * @param pluginType The type of the requested plugin. Maps to subfolders in the plugin folder.
	 * @param pluginName The name of the requested plugin. Maps to files in subfolder.
	 *
	 * @return A response with the file attached.
	 *
	 */
	@GET
	@Path("/plugins/{pluginType}/{pluginName}")
	@Produces("application/octet-stream")
	public final Response getPlugin(@PathParam("pluginType") final String pluginType,
	                                @PathParam("pluginName") final String pluginName) {

		System.out.println("GET /repository/plugins/" + pluginType + "/" + pluginName);

		// Generate path to plugin file.
		final File file = new File("plugins/" + pluginType + "/" + pluginName);

		// If plugin file doesn't exist, return error.
		if (file == null || !file.exists()) {
			System.out.println("Plugin not found. Returning 404.");
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		// Return plugin file.
		return Response.ok(file).header("Content-Disposition", "attachment; filename=" + file.getName()).build();
	}

	/**
	 * Returns the request context that maps to the given user context if it exists.
	 *
	 * @param application The application parameter.
	 * @param resource The resource parameter.
	 *
	 * @return A string that contains the request context.
	 */
	@GET
	@Path("context")
	@Produces("application/xml")
	public final String getContext(@QueryParam("application") final String application,
	                               @QueryParam("resource") final String resource) {

		System.out.println("GET /repository/context?application=" + application + "&resource=" + resource);

		// Build path to potential mapping file from the resource and application provided.
		final File mappingFile   = new File("mappings/" + application + "/" + resource + "/context.xml");

		// Load and return mapping file if it exists
		try {
			final Scanner scanner = new Scanner(mappingFile);
			final String response = scanner.useDelimiter("\\Z").next();
			scanner.close();
			return response;
		}
		catch (FileNotFoundException e) {
			System.out.println("Mapping not found. Returning 404.");
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}
}
