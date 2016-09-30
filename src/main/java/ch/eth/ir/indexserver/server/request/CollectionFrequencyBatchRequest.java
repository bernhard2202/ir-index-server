package ch.eth.ir.indexserver.server.request;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;

import ch.eth.ir.indexserver.index.IndexConstants;
import ch.eth.ir.indexserver.server.response.FrequencyBatchResponse;
import ch.eth.ir.indexserver.server.response.FrequencyBean;

public class CollectionFrequencyBatchRequest extends AbstractRequest<FrequencyBatchResponse>{

	private Set<String> terms;
	private IndexReader reader;
	
	public CollectionFrequencyBatchRequest(Set<String> terms, IndexReader reader) {
		super();
		this.terms = terms;
		this.reader = reader;
	}
	
	/** 
	 * Returns the collection frequency for a given term
	 * @param term
	 * @return Collection frequency of the term
	 */
	public long getCollectionFrequency(String term) throws IOException {
		Term luceneTerm = new Term(IndexConstants.CONTENT, new BytesRef(term));
		return reader.totalTermFreq(luceneTerm);
	}
	
	@Override
	public FrequencyBatchResponse call() throws Exception {
		FrequencyBatchResponse frequencyBatch = new FrequencyBatchResponse();
		for (String term : terms) {
			frequencyBatch.addFrequency(new FrequencyBean(term, getCollectionFrequency(term)));
		}
		return frequencyBatch;
	}

}
