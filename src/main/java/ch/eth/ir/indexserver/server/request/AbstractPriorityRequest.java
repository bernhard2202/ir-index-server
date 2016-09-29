package ch.eth.ir.indexserver.server.request;

import ch.eth.ir.indexserver.server.response.AbstractResponse;

public abstract class AbstractPriorityRequest implements Comparable<AbstractPriorityRequest>{
	private int priority;
	private AbstractResponse response;
	
	public AbstractPriorityRequest(int priority) {
		this.priority = priority;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public AbstractResponse getResponse() {
		return response;
	}

	public void setResponse(AbstractResponse response) {
		this.response = response;
	}

	public int compareTo(AbstractPriorityRequest o) {
		return Integer.compare(priority, o.getPriority());
	}
}
