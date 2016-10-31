package ch.eth.ir.indexserver.server.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import ch.eth.ir.indexserver.server.config.ServerProperties;

public class UnauthorizedAccessException extends WebApplicationException {
	public UnauthorizedAccessException() {
		super(Response.status(Response.Status.UNAUTHORIZED).entity(String
				.format("Illegal authorization - This will be reported.", ServerProperties.MAX_BATCH_REQ_ALLOWED))
				.build());
	}
}