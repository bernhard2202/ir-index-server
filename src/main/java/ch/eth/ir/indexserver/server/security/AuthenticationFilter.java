package ch.eth.ir.indexserver.server.security;

import java.security.Principal;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

import ch.eth.ir.indexserver.server.exception.UnauthorizedAccessException;

import javax.ws.rs.Priorities;

/**
 * Authentication filter is used to filter unauthorized requests
 * Valid request provide a HTTP authorization header of the following 
 * form: 'Barer <token>', where <token> is a valid authorization
 * token belonging to an user
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
        
        final String username = UserProperties.getNameForToken(token);
        
        /* Override security context to make requests traceable */ 
        requestContext.setSecurityContext(new SecurityContext() {
        	
        	@Override
            public boolean isUserInRole(String role) {
                return true;
            }

            @Override
            public boolean isSecure() {
                return false;
            }

            @Override
            public String getAuthenticationScheme() {
                return null;
            }
			
			@Override
	        public Principal getUserPrincipal() {

	            return new Principal() {

	                @Override
	                public String getName() {
	                    return username;
	                }
	            };
	        }
		});
        
        // HINT
        // uncomment when every user request should be logged in the system
        //log.info("user request with token: "+username);
	}
}