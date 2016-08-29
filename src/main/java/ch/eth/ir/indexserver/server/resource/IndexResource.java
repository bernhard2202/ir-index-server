package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.server.resource.beans.QueryResultBean;

/** 
 * Resource for direct retrieval requests from the index
 */
@Path("index")
public class IndexResource {
	
	@Inject
	private IndexAPI indexAPI;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("query")
	public QueryResultBean findIdsForQuery(@QueryParam("minOverlap") int nOverlap, @QueryParam("term") List<String> query) throws IOException {
		return indexAPI.findNOverlappingDocuments(nOverlap, query);
	}

}
