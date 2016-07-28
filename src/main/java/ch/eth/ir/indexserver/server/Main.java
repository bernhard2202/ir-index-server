package ch.eth.ir.indexserver.server;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.utilities.IndexReaderBinder;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;

/**
 * Main class.
 *
 */
public class Main {
	
	// Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:7777/myapp/";
	
    private static Logger log = Logger.getLogger(IndexAPI.class);
   

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in com.example package
        final ResourceConfig rc = new ResourceConfig().packages("ch.eth.ir.indexserver.resource");
        rc.register(new IndexReaderBinder());
        
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }


    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        
        log.info(String.format("Jersey app started with WADL available at "
                + "%s\nHit enter to stop it...", BASE_URI));
        System.in.read();
        
        server.shutdownNow();
    }
}

