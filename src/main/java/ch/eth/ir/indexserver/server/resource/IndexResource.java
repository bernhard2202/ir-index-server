package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;
import java.util.List;
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
import javax.ws.rs.container.TimeoutHandler;
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
			@QueryParam("term") final List<String> query) throws IOException {
//	    asyncResponse.setTimeoutHandler(new TimeoutHandler() {
//	    	
//	        public void handleTimeout(AsyncResponse asyncResponse) {
//	            asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
//	                    .entity("Operation time out.").build());
//	        }
//	    });
//	    asyncResponse.setTimeout(20, TimeUnit.SECONDS);
//	 
//	    new Thread(new Runnable() {
//	 
//	        public void run() {
//	        	int minimumOverlap = nOverlap;
//	        	if (nOverlap > query.size()) {
//	        		asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST)
//	        				.entity("minOverlap has to be between one and the query size").build());
//	        	}
//	        	// by default set it to query.size
//	        	if (nOverlap == 0) {
//	        		minimumOverlap = query.size();
//	        	}
//				try {
//					QueryResultResponse result = indexAPI.findNOverlappingDocuments(minimumOverlap, query);
//		            asyncResponse.resume(result);
//				} catch (IOException e) {
//					asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//							.entity("Internal error, please repeat").build());
//				}
//	        }
//	    }).start();
		
		Future<QueryResultResponse> futureResponse = IndexRequestHandlerPool.getInstance().submit(
				new QueryDocumentsRequest(1, indexAPI.getSearcher(), query, nOverlap));
		try {
			QueryResultResponse response = futureResponse.get(20, TimeUnit.SECONDS);
			asyncResponse.resume(response);
	    } catch (InterruptedException | ExecutionException e) {
	    	asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Internal error, please repeat").build());
	    } catch (TimeoutException e) {
	    	futureResponse.cancel(true);
	    	asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
	    			.entity("Operation time out.").build());
	    }
	}
}
