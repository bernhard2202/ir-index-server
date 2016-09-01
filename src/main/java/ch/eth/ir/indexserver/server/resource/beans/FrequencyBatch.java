package ch.eth.ir.indexserver.server.resource.beans;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encapsulates multiple document frequency beans for batch requests
 */
public class FrequencyBatch {
	private List<FrequencyBean> frequencies;
	
	public FrequencyBatch() {
		frequencies = new ArrayList<FrequencyBean>();
	}
	
	public void addFrequency(FrequencyBean frequency) {
		frequencies.add(frequency);
	}
	
	@JsonProperty(value="frequencies")
	public List<FrequencyBean> getFrequencies() {
		return frequencies;
	}
}
