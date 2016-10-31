package ch.eth.ir.indexserver.server.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


/**
 * Exception thrown when user tries to request a document with an ID not in range
 */
public class IllegalDocumentIdentifierException  extends WebApplicationException {
	public IllegalDocumentIdentifierException() {
		super(Response.status(Response.Status.BAD_REQUEST).entity(String
				.format("Document id out of range!"))
				.type(MediaType.TEXT_PLAIN).build());
	}
}
