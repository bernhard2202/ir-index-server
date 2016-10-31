package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;
import java.util.Set;

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
import ch.eth.ir.indexserver.server.config.ServerProperties;
import ch.eth.ir.indexserver.server.exception.BatchLimitExceededException;
import ch.eth.ir.indexserver.server.request.CollectionFrequencyBatchRequest;
import ch.eth.ir.indexserver.server.request.DocumentFrequencyBatchRequest;
import ch.eth.ir.indexserver.server.security.Secured;

/**
 * Resource for term specific requests
 */
@Secured
@Path("term")
public class TermResource extends AbstractAsynchronousResource {
	@Inject
	private IndexAPI indexAPI;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("df")
	public void getDocumentFrequency(
			@Suspended final AsyncResponse asyncResponse,
			@QueryParam("term") Set<String> terms,
			@Context SecurityContext securityContext) throws IOException {
		
		/* check for ill formed request */
		if (terms.size() > ServerProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		
		this.performAsyncRequest(
				asyncResponse,
				new DocumentFrequencyBatchRequest(terms, indexAPI.getReader()),
				securityContext);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("cf")
	public void getCollectionFrequency(
			@Suspended final AsyncResponse asyncResponse,
			@QueryParam("term") Set<String> terms,
			@Context SecurityContext securityContext) throws IOException {
		
		/* check for ill formed request */
		if (terms.size() > ServerProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		
		this.performAsyncRequest(
				asyncResponse,
				new CollectionFrequencyBatchRequest(terms, indexAPI.getReader()),
				securityContext);
	}
	
	@GET
	@Path("unique")
	public long getUniqueTokensInIndex() throws IOException {
		return indexAPI.getNumberOfUniqueTerms();
	}
	
	@GET
	@Path("total")
	public long getTotalTokensInIndex() throws IOException {
		return indexAPI.getTotalNumberOfTerms();
	}
}
