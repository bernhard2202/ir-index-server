package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;
import java.util.List;
import java.util.Set;
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
import ch.eth.ir.indexserver.server.request.CollectionFrequencyBatchRequest;
import ch.eth.ir.indexserver.server.request.DocumentFrequencyBatchRequest;
import ch.eth.ir.indexserver.server.request.QueryDocumentsRequest;
import ch.eth.ir.indexserver.server.response.FrequencyBatchResponse;
import ch.eth.ir.indexserver.server.response.FrequencyBean;
import ch.eth.ir.indexserver.server.response.QueryResultResponse;
import ch.eth.ir.indexserver.server.security.Secured;

/**
 * Resource for term specific requests
 */
@Secured
@Path("term")
public class TermResource {
	@Inject
	private IndexAPI indexAPI;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("df")
	public void getDocumentFrequency(@Suspended final AsyncResponse asyncResponse,
			@QueryParam("term") Set<String> terms) throws IOException {
		
		/* check for ill formed request */
		if (terms.size() > RequestProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		
		Future<FrequencyBatchResponse> futureResponse = IndexRequestHandlerPool.getInstance()
				.submit(new DocumentFrequencyBatchRequest(terms, indexAPI.getReader()),1);
		try {
			FrequencyBatchResponse response = futureResponse.get(RequestProperties.TIMEOUT, TimeUnit.SECONDS);
			asyncResponse.resume(response);
	    } catch (InterruptedException | ExecutionException e) {
	    	asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	    			.entity("Internal error, please repeat").build());
	    } catch (TimeoutException e) {
	    	futureResponse.cancel(true);
	    	asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
	    			.entity("Operation time out.").build());
	    }
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("cf")
	public void getCollectionFrequency(@Suspended final AsyncResponse asyncResponse,
			@QueryParam("term") Set<String> terms) throws IOException {
		
		/* check for ill formed request */
		if (terms.size() > RequestProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		
		Future<FrequencyBatchResponse> futureResponse = IndexRequestHandlerPool.getInstance()
				.submit(new CollectionFrequencyBatchRequest(terms, indexAPI.getReader()),1);
		try {
			FrequencyBatchResponse response = futureResponse.get(RequestProperties.TIMEOUT, TimeUnit.SECONDS);
			asyncResponse.resume(response);
	    } catch (InterruptedException | ExecutionException e) {
	    	asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	    			.entity("Internal error, please repeat").build());
	    } catch (TimeoutException e) {
	    	futureResponse.cancel(true);
	    	asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
	    			.entity("Operation time out.").build());
	    }
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
