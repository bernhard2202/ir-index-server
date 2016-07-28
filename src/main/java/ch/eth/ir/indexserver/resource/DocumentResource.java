package ch.eth.ir.indexserver.resource;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.resource.beans.DocumentVectorBatch;
import ch.eth.ir.indexserver.server.RequestProperties;
import ch.eth.ir.indexserver.server.exception.BatchLimitExceededException;

/**
 * Resource for requests on indexed documents
 */
@Path("document")
public class DocumentResource {
	private static Logger log = Logger.getLogger(IndexAPI.class);

	@Inject
	private IndexAPI indexAPI;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("vector")
	public DocumentVectorBatch getDocumentVectors(@QueryParam("id") List<Integer> ids) throws IOException {
		DocumentVectorBatch batchResponse = new DocumentVectorBatch();
		if (ids.size() > RequestProperties.MAX_BATCH_REQ_ALLOWED) {
			log.error(String.format("% exceeds the maximum number of requests allowed in one batch",
					ids.size()));
			throw new BatchLimitExceededException();
		}
		for (int id : ids) {
			batchResponse.addDocumentVector(indexAPI.getDocumentVector(id));
		}
		return batchResponse;
	}
}