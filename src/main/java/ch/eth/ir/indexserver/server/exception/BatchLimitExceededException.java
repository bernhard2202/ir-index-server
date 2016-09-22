package ch.eth.ir.indexserver.server.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.eth.ir.indexserver.server.config.RequestProperties;

/**
 * Exception gets thrown whenever a user tries to request more resources in
 * a single batch request than {@link RequestProperties#MAX_BATCH_REQ_ALLOWED} 
 * allows
 */
public class BatchLimitExceededException extends WebApplicationException {
	public BatchLimitExceededException() {
		super(Response.status(Response.Status.BAD_REQUEST).entity(String
				.format("Maximum batch size is %d within a single request!", RequestProperties.MAX_BATCH_REQ_ALLOWED))
				.type(MediaType.TEXT_PLAIN).build());
	}
}
