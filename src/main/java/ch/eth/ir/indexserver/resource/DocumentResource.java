package ch.eth.ir.indexserver.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ch.eth.ir.indexserver.index.IndexAPI;

/**
 * Resource reflects a Lucene document in the index 
 */
@Path("document")
public class DocumentResource {
	
	@Inject
    private IndexAPI reader;
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
    	return reader.getReader().toString();
    }
}
