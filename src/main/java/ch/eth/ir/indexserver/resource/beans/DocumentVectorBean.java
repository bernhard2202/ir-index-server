package ch.eth.ir.indexserver.resource.beans;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentVectorBean {
	
	private Map<String, Long> termFrequencies;
	private int id;
	
	public DocumentVectorBean(int docId, Map<String, Long> termFrequencies) { 
		this.termFrequencies = termFrequencies;
		this.id = docId;
	} 
	
	@JsonProperty(value="id")
	public int getId() {
		return id;
	}
	
	@JsonProperty(value="tf-vector")
    public Map<String, Long> getTermFrequencies() {
        return termFrequencies;
    }
}
