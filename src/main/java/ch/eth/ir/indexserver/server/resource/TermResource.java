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
import ch.eth.ir.indexserver.server.response.FrequencyBatchResponse;
import ch.eth.ir.indexserver.server.response.FrequencyBean;
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
	public FrequencyBatchResponse getDocumentFrequency(@QueryParam("term") List<String> terms) throws IOException {
		FrequencyBatchResponse frequencyBatch = new FrequencyBatchResponse();
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
	public FrequencyBatchResponse getCollectionFrequency(@QueryParam("term") List<String> terms) throws IOException {
		FrequencyBatchResponse frequencyBatch = new FrequencyBatchResponse();
		if (terms.size() > RequestProperties.MAX_BATCH_REQ_ALLOWED) {
			throw new BatchLimitExceededException();
		}
		for (String term : terms) {
			frequencyBatch.addFrequency(new FrequencyBean(term,indexAPI.getCollectionFrequency(term)));
		}
		return frequencyBatch;
	}
	
	@GET
	@Path("unique")
	public long getUniqueTokensInIndex() throws IOException {
		return indexAPI.getNumberOfUniqueTerms();
	}
	
	@GET
	@Path("total")
	public long getTotalTokensInIndex() throws IOException {
		return indexAPI.getTotalNumberOfTerms();
	}
}
