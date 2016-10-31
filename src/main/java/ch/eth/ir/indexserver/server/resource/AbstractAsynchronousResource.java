package ch.eth.ir.indexserver.server.resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import ch.eth.ir.indexserver.server.config.ServerProperties;
import ch.eth.ir.indexserver.server.request.AbstractAsynchronousRequest;
import ch.eth.ir.indexserver.server.response.AbstractResponse;
import ch.eth.ir.indexserver.server.security.UserProperties;
import ch.eth.ir.indexserver.server.threadpool.RequestHandlerPool;

/**
 * Abstract class for asynchronous resources
 */
public abstract class AbstractAsynchronousResource {
	
	/**
	 * 1) retrieve priority for request
	 * 2) put request with right priority on the queue
	 * 3) wait for response
	 * 4) write back response / handle errors
	 */
	protected <T extends AbstractResponse> void performAsyncRequest(
			final AsyncResponse asyncResponse,
			AbstractAsynchronousRequest<T> request,
			SecurityContext securityContext) {
		/* get request priority */
		int priority = Integer.MAX_VALUE - UserProperties
				.getRequestsForUser(securityContext.getUserPrincipal().getName());
		
		
		/* process request asynchron */
		Future<T> futureResponse = RequestHandlerPool.getInstance()
				.submit(request,priority);
		try {
			T response = futureResponse.get(ServerProperties.TIMEOUT, TimeUnit.SECONDS);
			asyncResponse.resume(response); /* OK */
	    } catch (InterruptedException | ExecutionException e) {
	    	/* internal error */
	    	asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	    			.entity(ServerProperties.INTERNAL_ERROR_MSG).build());
	    } catch (TimeoutException e) {
	    	/* reached timeout limit */
	    	futureResponse.cancel(true);
	    	asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
	    			.entity(ServerProperties.TIMEOUT_ERROR_MSG).build());
	    }	
	}

}
