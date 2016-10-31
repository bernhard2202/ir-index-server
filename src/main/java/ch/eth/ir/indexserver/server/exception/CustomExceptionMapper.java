package ch.eth.ir.indexserver.server.exception;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import ch.eth.ir.indexserver.index.IndexAPI;

/**
 * Catches general exceptions, logs them, and returns a 501 Internal Server Error
 */
@Provider
public class CustomExceptionMapper implements ExceptionMapper<Exception> {
	private static Logger log = Logger.getLogger(IndexAPI.class);

	@Context
	private UriInfo uriInfo;

	public Response toResponse(Exception e) {
		String errorMessage = buildErrorMessage(uriInfo);
		log.error(errorMessage, e);
		return Response.serverError().entity("Internal Server Error - Please try again!").build();
	}

	private String buildErrorMessage(UriInfo uriInfo) {
		StringBuilder message = new StringBuilder();
		message.append("Exception:\n");
		if (uriInfo != null) {
			message.append("URL: ").append(uriInfo.getAbsolutePath()).append("\n");
			message.append("PARAM: ").append(uriInfo.getQueryParameters().toString()).append("\n");
		}
		return message.toString();
	}
}