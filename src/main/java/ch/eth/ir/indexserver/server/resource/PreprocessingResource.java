package ch.eth.ir.indexserver.server.resource;

import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.server.resource.beans.QueryBean;

@Path("preprocess")
public class PreprocessingResource {
	
	@Inject
	IndexAPI indexApi;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public QueryBean preprocessQuery(@QueryParam("query")String query) throws IOException {
		return indexApi.preprocess(query);
	}
}
