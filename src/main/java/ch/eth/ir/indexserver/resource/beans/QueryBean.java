package ch.eth.ir.indexserver.resource.beans;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryBean {
	private List<String> queryTerms;
	
	public QueryBean() {
		this.queryTerms = new ArrayList<String>();
	}
	
	public void addQueryTerm(String term) {
		this.queryTerms.add(term);
	}
	
	@JsonProperty(value="terms")
	public List<String> getQueryTerms() {
		return queryTerms;
	}
}
