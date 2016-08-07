package ch.eth.ir.indexserver.server.exception;

import java.io.IOException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import ch.eth.ir.indexserver.index.IndexAPI;

@Provider
public class IOExceptionMapper implements ExceptionMapper<IOException> {
	private static Logger log = Logger.getLogger(IndexAPI.class);

	@Context
	private UriInfo uriInfo;

	public Response toResponse(IOException e) {
		String errorMessage = buildErrorMessage(uriInfo);
		log.error(errorMessage, e);
		return Response.serverError().entity("Internal Server Error - Please try again!").build();
	}

	private String buildErrorMessage(UriInfo uriInfo) {
		StringBuilder message = new StringBuilder();
		message.append("IO Exception:\n");
		if (uriInfo != null) {
			message.append("URL: ").append(uriInfo.getAbsolutePath()).append("\n");
			message.append("PARAM: ").append(uriInfo.getQueryParameters().toString()).append("\n");
		}
		return message.toString();
	}
}