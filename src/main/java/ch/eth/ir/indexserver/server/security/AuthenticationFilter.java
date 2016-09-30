package ch.eth.ir.indexserver.server.security;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import ch.eth.ir.indexserver.server.exception.UnauthorizedAccessException;

import javax.ws.rs.Priorities;

/**
 * Authentication filter is used to filter unauthorized requests
 * Valid request provide a HTTP authorization head of the following 
 * form: Barer <token>, where token is valid token belonging to an 
 * user
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
	private static Logger log = Logger.getLogger(AuthenticationFilter.class);
	
	public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader = 
            requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            log.warn("unauthorized attempt: Header:"+authorizationHeader);
            throw new UnauthorizedAccessException();
        }
        
        String token = authorizationHeader.substring(6).trim();

        try {
        	if (!UserProperties.validateToken(token)) {
        		throw new UnauthorizedAccessException();
        	}
        } catch (Exception e) {
            log.warn("unauthorized attempt: Header:"+authorizationHeader);
            throw new UnauthorizedAccessException();
        }
        UserProperties.increaseRequestCount(token);
        //TODO: uncomment when every user request should be logged
        //log.info("user request with token: "+token);
	}
}