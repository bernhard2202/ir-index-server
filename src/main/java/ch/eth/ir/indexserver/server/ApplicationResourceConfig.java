package ch.eth.ir.indexserver.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

import ch.eth.ir.indexserver.server.exception.CustomExceptionMapper;
import ch.eth.ir.indexserver.server.security.AuthenticationFilter;
import ch.eth.ir.indexserver.server.utilities.IndexReaderBinder;

public class ApplicationResourceConfig extends ResourceConfig {

	public ApplicationResourceConfig() {
		packages("ch.eth.ir.indexserver.server.resource");
		// Set this property so that the 400 will still send the entity
		// correctly.
		property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
		// register for singleton injection
        register(new IndexReaderBinder());
        register(new CustomExceptionMapper());
        register(AuthenticationFilter.class);
	}
}
