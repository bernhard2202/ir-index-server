package ch.eth.ir.indexserver.server.request;

import java.util.concurrent.Callable;

import ch.eth.ir.indexserver.server.response.AbstractResponse;

/**
 * Abstract class for asynchronous requests
 * <T> the response type for the request
 */
public abstract class AbstractAsynchronousRequest<T extends AbstractResponse> implements Callable<T> { 
	
	/* 
	 * THE LOGIC HAS TO BE IMPLEMENTED IN THE CHILD's CALL() METHOD!
	 * The call method will be executed by the thread pool
	 */
}
