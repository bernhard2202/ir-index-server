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
import ch.eth.ir.indexserver.server.config.RequestProperties;
import ch.eth.ir.indexserver.server.exception.BatchLimitExceededException;
import ch.eth.ir.indexserver.server.exception.IllegalDocumentIdentifierException;
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
	public DocumentVectorBatchResponse getDocumentVectors(@QueryParam("id") List<Integer> ids) throws IOException {
		DocumentVectorBatchResponse batchResponse = new DocumentVectorBatchResponse();
		int maxDocId = indexAPI.getNumberOfDocuments();
		
		if (ids.size() > RequestProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		
		for (int id : ids) {
			if (id < 0 || id > maxDocId) {
				throw new IllegalDocumentIdentifierException();
			}
			batchResponse.addDocumentVector(indexAPI.getDocumentVector(id));
		}
		return batchResponse;
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
