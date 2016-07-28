package ch.eth.ir.indexserver.server;

public class RequestProperties {
	/**
	 * How many single homogeneous requests are allowed to be bundled
	 * together to a single batch request.
	 */
	public final static Integer MAX_BATCH_REQ_ALLOWED = 100;

}