package org.simtech.bootware.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.simtech.bootware.core.UserContext;

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
	@Path("/getPlugin/{pluginType}/{pluginName}")
	@Produces("application/octet-stream")
	public final Response getPlugin(@PathParam("pluginType") final String pluginType,
	                                @PathParam("pluginName") final String pluginName) {

		// Generate path to plugin file.
		final File file = new File("plugins/" + pluginType + "/" + pluginName);

		// If plugin file doesn't exist, return error.
		if (file == null || !file.exists()) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}

		// Return plugin file.
		return Response.ok(file).header("Content-Disposition", "attachment; filename=" + file.getName()).build();
	}

	/**
	 * Returns the request context that maps to the given user context if it exists.
	 *
	 * @param userContext The user context that should be mapped to a request context.
	 *
	 * @return A string that contains the request context.
	 */
	@POST
	@Path("/mapContext")
	@Consumes("application/xml")
	@Produces("application/xml")
	public final String getPlugin(final UserContext userContext) {

		// Build path to potential mapping file from the resource and application provided.
		final String resource    = userContext.getResource();
		final String application = userContext.getApplication();
		final File mappingFile   = new File("mappings/" + application + "/" + resource + "/context.xml");

		// Load and return mapping file if it exists
		try {
			return new Scanner(mappingFile).useDelimiter("\\Z").next();
		}
		catch (FileNotFoundException e) {
			throw new WebApplicationException(Response.Status.NOT_FOUND);
		}
	}

}
