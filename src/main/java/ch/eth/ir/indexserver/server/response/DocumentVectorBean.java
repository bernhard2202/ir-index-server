package ch.eth.ir.indexserver.server.response;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 
 * Encapsulates a term vector for a single document. 
 */
public class DocumentVectorBean {
	
	/* the term vector for this document represented as a map
	 * 		key = term
	 * 		value = number of occurrences of the term in this document
	 */
	private Map<String, Long> termFrequencies;
	
	/* 
	 * the document id
	 */
	private int id;
	
	public DocumentVectorBean() {
		termFrequencies = new HashMap<String, Long>();
	}
	
	public DocumentVectorBean(int docId, Map<String, Long> termFrequencies) { 
		this.termFrequencies = termFrequencies;
		this.id = docId;
	} 
	
	@JsonProperty(value="doc-id")
	public int getId() {
		return id;
	}
	
	@JsonProperty(value="tf-vector")
    public Map<String, Long> getTermFrequencies() {
        return termFrequencies;
    }

	public void setTermFrequencies(Map<String, Long> termFrequencies) {
		this.termFrequencies = termFrequencies;
	}

	public void setId(int id) {
		this.id = id;
	}
}
