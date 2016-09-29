package ch.eth.ir.indexserver.server.response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 
 * Result of a query against the index, contains the id's of matching documents. 
 */
public class QueryResultResponse extends AbstractResponse{
	
	private List<Integer> documentIds;
	private int hits;
	
	public QueryResultResponse() {
		documentIds = new ArrayList<Integer>();
		hits=0;
	}
	
	public void addDocument(Integer id) {
		documentIds.add(id);
		hits++;
	}

	public void addAllDocuments(Collection<? extends Integer> ids) {
		documentIds.addAll(ids);
		hits = hits + ids.size();
	}
	
	@JsonProperty(value="totalHits")
	public int getTotalHits() {
		return hits;
	}
	
	@JsonProperty(value="result")
	public List<Integer> getDocumentIds() {
		return documentIds;
	}

	public void setDocumentIds(List<Integer> documentIds) {
		this.documentIds = documentIds;
	}
	
	public void setTotalHits(int hits) {
		this.hits = hits;
	}
}
