package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.server.request.QueryDocumentsRequest;
import ch.eth.ir.indexserver.server.security.Secured;

/** 
 * Resource for direct retrieval requests from the index
 */
@Secured
@Path("index")
public class IndexResource extends AbstractResource {
	
	@Inject
	private IndexAPI indexAPI;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("query")
	public void findDocIdsForQuery(@Suspended final AsyncResponse asyncResponse,
			@DefaultValue("0") @QueryParam("minOverlap") final int nOverlap,
			@QueryParam("term") final Set<String> query,
			@Context SecurityContext securityContext) throws IOException {
				
		/* check for ill formed queries */
		if (query.size() < nOverlap) {
			asyncResponse.resume(Response.status(Response.Status.BAD_REQUEST)
					.entity("minOverlap is bigger than the number of terms provided").build());
		} 		
		
		this.performAsyncRequest(
				asyncResponse,
				new QueryDocumentsRequest(indexAPI.getSearcher(), query, nOverlap),
				securityContext);
	}
}
