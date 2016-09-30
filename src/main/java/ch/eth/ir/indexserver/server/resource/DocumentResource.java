package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.index.IndexRequestHandlerPool;
import ch.eth.ir.indexserver.server.config.RequestProperties;
import ch.eth.ir.indexserver.server.exception.BatchLimitExceededException;
import ch.eth.ir.indexserver.server.exception.IllegalDocumentIdentifierException;
import ch.eth.ir.indexserver.server.request.TermVectorsBatchRequest;
import ch.eth.ir.indexserver.server.response.DocumentVectorBatchResponse;
import ch.eth.ir.indexserver.server.security.Secured;

/**
 * Resource for requests on indexed documents
 */
@Secured
@Path("document")
public class DocumentResource {

	@Inject
	private IndexAPI indexAPI;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("vector")
	public void getDocumentVectors(
			@Suspended final AsyncResponse asyncResponse,
			@QueryParam("id") List<Integer> ids) throws IOException {
		int maxDocId = indexAPI.getNumberOfDocuments();
		
		/* check for ill formed requests */
		if (ids.size() > RequestProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		/* check for wrong doc-ids in the request */
		for (int id : ids) {
			if (id < 0 || id > maxDocId) {
				throw new IllegalDocumentIdentifierException();
			}
		}
		
		/* process request asynchron */
		Future<DocumentVectorBatchResponse> futureResponse = IndexRequestHandlerPool.getInstance()
				.submit(new TermVectorsBatchRequest(indexAPI.getReader(), ids),/*TODO*/1);
		try {
			DocumentVectorBatchResponse response = futureResponse.get(RequestProperties.TIMEOUT, TimeUnit.SECONDS);
			// everything went well:
			asyncResponse.resume(response); 
	    } catch (InterruptedException | ExecutionException e) {
	    	// Internal error
	    	asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	    			.entity("Internal error, please repeat").build());
	    } catch (TimeoutException e) {
	    	// Reached timeoutlimit
	    	futureResponse.cancel(true);
	    	asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
	    			.entity("Operation time out.").build());
	    }		
	}
	
	@GET
	@Path("average-length")
	public int getAverageDocumentLength() {
		return indexAPI.getAverageDocumentLength();
	}
	
	@GET
	@Path("count")
	public int getDocumentCount() throws IOException {
		return indexAPI.getNumberOfDocuments();
	}
}
