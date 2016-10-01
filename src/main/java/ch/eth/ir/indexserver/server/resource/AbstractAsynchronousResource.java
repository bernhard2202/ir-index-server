package ch.eth.ir.indexserver.server.resource;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import ch.eth.ir.indexserver.server.config.RequestProperties;
import ch.eth.ir.indexserver.server.request.AbstractRequest;
import ch.eth.ir.indexserver.server.response.AbstractResponse;
import ch.eth.ir.indexserver.server.security.UserProperties;
import ch.eth.ir.indexserver.server.threadpool.RequestHandlerPool;

public abstract class AbstractAsynchronousResource {
	
	protected <T extends AbstractResponse> void performAsyncRequest(
			final AsyncResponse asyncResponse,
			AbstractRequest<T> request,
			SecurityContext securityContext) {
		/* get request priority */
		int priority = Integer.MAX_VALUE - UserProperties
				.getRequestsForUser(securityContext.getUserPrincipal().getName());
		
		
		/* process request asynchron */
		Future<T> futureResponse = RequestHandlerPool.getInstance()
				.submit(request,priority);
		try {
			T response = futureResponse.get(RequestProperties.TIMEOUT, TimeUnit.SECONDS);
			asyncResponse.resume(response); /* OK */
	    } catch (InterruptedException | ExecutionException e) {
	    	/* internal error */
	    	asyncResponse.resume(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
	    			.entity(RequestProperties.INTERNAL_ERROR_MSG).build());
	    } catch (TimeoutException e) {
	    	/* reached timeout limit */
	    	futureResponse.cancel(true);
	    	asyncResponse.resume(Response.status(Response.Status.SERVICE_UNAVAILABLE)
	    			.entity(RequestProperties.TIMEOUT_ERROR_MSG).build());
	    }	
	}

}
