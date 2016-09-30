package ch.eth.ir.indexserver.server.request;

import java.util.concurrent.Callable;

import ch.eth.ir.indexserver.server.response.AbstractResponse;

public abstract class AbstractRequest<T extends AbstractResponse> implements Callable<T> {
	
}
