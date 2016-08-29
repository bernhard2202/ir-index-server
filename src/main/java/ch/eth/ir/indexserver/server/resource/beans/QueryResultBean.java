package ch.eth.ir.indexserver.server.resource.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 
 * Result of a query against the index, contains the id's of matching documents. 
 */
public class QueryResultBean {
	
	private List<Integer> documentIds;
	
	public QueryResultBean() {
		documentIds = new ArrayList<Integer>();
	}
	
	public void addDocument(Integer id) {
		documentIds.add(id);
	}

	public void addAllDocuments(Collection<? extends Integer> ids) {
		documentIds.addAll(ids);
	}
	
	@JsonProperty(value="results")
	public List<Integer> getIds() {
		return documentIds;
	}
	
	@JsonProperty(value="totalHits")
	public int getTotalHits() {
		return documentIds.size();
	}
}
