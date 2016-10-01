package ch.eth.ir.indexserver.server.config;

public class RequestProperties {
	/**
	 * How many single homogeneous requests are allowed to be bundled
	 * into to a single batch request.
	 */
	public final static Integer MAX_BATCH_REQ_ALLOWED = 100;
	
	/**
	 * upper bound on the number of document ID's returned when issuing a query
	 */
	public final static int MAX_SEARCH_RESULTS = 10000;
	
	/**
	 * number of seconds until any request on the queue times out. 
	 */
	public final static int TIMEOUT = 20;
	
	/**
	 * Error message shown on a timeout
	 */
	public final static String TIMEOUT_ERROR_MSG = 
			"The request timed out, this could be due to too low priority, please try again later.";
	
	/**
	 * Error message shown on a internal error during computation
	 */
	public final static String INTERNAL_ERROR_MSG = 
			"Internal Error, please check request and repeat. Please report this error, if it keeps repeating.";

}
