package ch.eth.ir.indexserver.server;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.server.config.ApplicationResourceConfig;
import ch.eth.ir.indexserver.server.security.UserProperties;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

/**
 * Main class for starting server
 */
public class Main {


	private static Logger log = Logger.getLogger(IndexAPI.class);
	
	public static final String BASE_URI;
    public static final String protocol;
    public static final Optional<String> host;
    public static final String path;
    public static final Optional<String> port;
   
    static{
        protocol = "http://";
        host = Optional.ofNullable(System.getenv("HOSTNAME"));
        port = Optional.ofNullable(System.getenv("PORT"));
        path = "irserver";
        BASE_URI = protocol + host.orElse("localhost") + ":" + port.orElse("8080") + "/" + path + "/";
    }
	
	/**
	 * Starts Grizzly HTTP server exposing JAX-RS resources defined in this
	 * application.
	 * 
	 * @return Grizzly HTTP server.
	 */
	public static HttpServer startServer() {
		// create a resource config that scans for JAX-RS resources and
		// providers
		// in com.example package
		final ResourceConfig rc = new ApplicationResourceConfig();

		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
	}

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.err.println("ussage: Main <user.properties> <password>");
			System.exit(-1);
		}
		
		File index = new File("./index");
		File userProperties = new File(args[0]);
		File indexProperties = new File("./index/index.properties");
		
		if (!index.exists() || !userProperties.exists() || !indexProperties.exists()) {
			System.err.println("make suere the following files/directories exist: ");
			System.err.println("./index/ - index folder");
			System.err.println("./index/index.properties - index properties");
			System.err.println(args[0]+" - user properties");
			System.exit(-1);
		}
		
		UserProperties.load(args[0], args[1]); 
		final HttpServer server = startServer();

		log.info(String.format("Jersey app started with WADL available at %s\nHit enter to stop it...", BASE_URI));
		System.in.read();

		server.shutdownNow();
	}
}
