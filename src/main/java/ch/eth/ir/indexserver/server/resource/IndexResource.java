package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
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
import ch.eth.ir.indexserver.server.resource.beans.QueryResultBean;
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
	public QueryResultBean findIdsForQuery(@QueryParam("minOverlap") int nOverlap,
			@QueryParam("term") List<String> query) throws IOException {
		return indexAPI.findNOverlappingDocuments(nOverlap, query);
	}
	
	@GET
	public void asyncGetWithTimeout(@Suspended final AsyncResponse asyncResponse,
			@QueryParam("minOverlap") final int nOverlap,
			@QueryParam("term") final List<String> query) throws IOException{
	    asyncResponse.setTimeoutHandler(new TimeoutHandler() {
	    	
	        public void handleTimeout(AsyncResponse asyncResponse) {
	            asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
	                    .entity("Operation time out.").build());
	        }
	    });
	    asyncResponse.setTimeout(20, TimeUnit.SECONDS);
	 
	    new Thread(new Runnable() {
	 
	        public void run() {
				try {
					QueryResultBean result = indexAPI.findNOverlappingDocuments(nOverlap, query);
		            asyncResponse.resume(result);
				} catch (IOException e) {
					asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
							.entity("Internal error, please repeat").build());
				}
	        }
	    }).start();
	}
}
