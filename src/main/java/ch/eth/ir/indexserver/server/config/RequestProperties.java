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
	public final static int MAX_SEARCH_RESULTS = 100000;

}
