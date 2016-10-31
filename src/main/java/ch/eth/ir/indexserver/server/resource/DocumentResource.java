package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.server.config.RequestProperties;
import ch.eth.ir.indexserver.server.exception.BatchLimitExceededException;
import ch.eth.ir.indexserver.server.exception.IllegalDocumentIdentifierException;
import ch.eth.ir.indexserver.server.request.TermVectorsBatchRequest;
import ch.eth.ir.indexserver.server.security.Secured;

/**
 * Resource for requests on indexed documents
 */
@Secured
@Path("document")
public class DocumentResource extends AbstractAsynchronousResource {

	@Inject
	private IndexAPI indexAPI;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("vector")
	public void getDocumentVectors(
			@Suspended final AsyncResponse asyncResponse,
			@QueryParam("id") List<Integer> ids,
			@Context SecurityContext securityContext) throws IOException {
		
		int maxDocId = indexAPI.getMaxDocId();
		
		/* check for ill formed requests */
		if (ids.size() > RequestProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		/* check for wrong doc-ids in the request */
		for (int id : ids) {
			if (id < 0 || id >= maxDocId) {
				throw new IllegalDocumentIdentifierException();
			}
		}
		
		this.performAsyncRequest(
				asyncResponse,
				new TermVectorsBatchRequest(indexAPI.getReader(), ids),
				securityContext);
	}
	
	@GET
	@Path("average-length")
	public int getAverageDocumentLength() {
		return indexAPI.getAverageDocumentLength();
	}
	
	@GET
	@Path("count")
	public int getDocumentCount() throws IOException {
		return indexAPI.getMaxDocId();
	}
}
