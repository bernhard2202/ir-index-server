package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
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
import ch.eth.ir.indexserver.server.request.QueryDocumentsRequest;
import ch.eth.ir.indexserver.server.response.QueryResultResponse;
import ch.eth.ir.indexserver.server.security.Secured;

/** 
 * Resource for direct retrieval requests from the index
 */
@Secured
@Path("index")
public class IndexResource {
	
	@Inject
	private IndexAPI indexAPI;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("query")
	public void findIdsForQuery(@Suspended final AsyncResponse asyncResponse,
			@DefaultValue("0") @QueryParam("minOverlap") final int nOverlap,
			@QueryParam("term") final Set<String> query) throws IOException {
				
		/* check for ill formed queries */
		if (query.size() < nOverlap) {
			asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST)
					.entity("minOverlap is bigger than the number of terms provided").build());
		} 		
		
		Future<QueryResultResponse> futureResponse = IndexRequestHandlerPool.getInstance()
				.submit(new QueryDocumentsRequest(indexAPI.getSearcher(), query, nOverlap),1);
		try {
			QueryResultResponse response = futureResponse.get(20, TimeUnit.SECONDS);
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
}
