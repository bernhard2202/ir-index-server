package ch.eth.ir.indexserver.server.resource.beans;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Bean for document or collection frequency of a term
 */
public class FrequencyBean {
	
	private String term;
	private long frequency;
	
	public FrequencyBean(String term, long frequency) {
		super();
		this.term = term;
		this.frequency = frequency;
	}
	
	@JsonProperty(value="term")
	public String getTerm() {
		return term;
	}
	
	@JsonProperty(value="frequency")	
	public long getFrequency() {
		return frequency;
	}
	
	
	
}
