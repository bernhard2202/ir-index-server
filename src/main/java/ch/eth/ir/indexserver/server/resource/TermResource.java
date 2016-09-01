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
import ch.eth.ir.indexserver.server.resource.beans.DocumentVectorBatch;
import ch.eth.ir.indexserver.server.resource.beans.FrequencyBatch;
import ch.eth.ir.indexserver.server.resource.beans.FrequencyBean;
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
	public FrequencyBatch getDocumentFrequency(@QueryParam("term") List<String> terms) throws IOException {
		FrequencyBatch frequencyBatch = new FrequencyBatch();
		if (terms.size() > RequestProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		for (String term : terms) {
			frequencyBatch.addFrequency(new FrequencyBean(term,indexAPI.getDocumentFrequency(term)));
		}
		return frequencyBatch;
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("cf")
	public FrequencyBatch getCollectionFrequency(@QueryParam("term") List<String> terms) throws IOException {
		FrequencyBatch frequencyBatch = new FrequencyBatch();
		if (terms.size() > RequestProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		for (String term : terms) {
			frequencyBatch.addFrequency(new FrequencyBean(term,indexAPI.getCollectionFrequency(term)));
		}
		return frequencyBatch;
	}
}
