package ch.eth.ir.indexserver.server.security;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;

import ch.eth.ir.indexserver.server.exception.UnauthorizedAccessException;

import javax.ws.rs.Priorities;


@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
	
	@Inject
	private UserProperties userProperties;

	public void filter(ContainerRequestContext requestContext) {
        String authorizationHeader = 
            requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedAccessException();
        }
        
        String token = authorizationHeader.substring(6).trim();

        try {
        	if (!userProperties.validateToken(token)) {
        		throw new UnauthorizedAccessException();
        	}
        } catch (Exception e) {
            throw new UnauthorizedAccessException();
        }
	}
}