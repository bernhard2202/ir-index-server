package ch.eth.ir.indexserver.server.response;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class QueryTermResponse extends AbstractResponse {
	private List<String> queryTerms;
	
	public QueryTermResponse() {
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
