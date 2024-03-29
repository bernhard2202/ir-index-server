package ch.eth.ir.indexserver.server;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import ch.eth.ir.indexserver.index.IndexAPI;
import ch.eth.ir.indexserver.server.config.ApplicationResourceConfig;
import ch.eth.ir.indexserver.server.config.ServerProperties;
import ch.eth.ir.indexserver.server.security.UserProperties;
import ch.eth.ir.indexserver.server.threadpool.RequestHandlerPool;
import ch.eth.ir.indexserver.server.threadpool.ResetUserPrioritiesJob;
import ch.eth.ir.indexserver.server.threadpool.ThreadPoolMonitor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Main class for starting the server
 */
public class Server {

	private static Logger log = Logger.getLogger(IndexAPI.class);

	public static final String BASE_URI;
	public static final String protocol;
	public static final String host;
	public static final String path;
	public static final String port;

	/*
	 * CONFIGURE SERVER HOSTNAME, PORT, ETC HERE:
	 */
	static {
		protocol = "http://";
		host = System.getenv("HOSTNAME");
		port = System.getenv("PORT");
		path = "irserver";
		StringBuffer uri = new StringBuffer(protocol);
		uri.append(host == null ? "localhost" : host);
		uri.append(":");
		uri.append(port == null ? "8080" : port);
		uri.append("/");
		uri.append(path);
		uri.append("/");
		BASE_URI = uri.toString();
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
		/* load properties and do some sanity checks */
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
			System.err.println(args[0] + " - user properties");
			System.exit(-1);
		}

		UserProperties.load(args[0], args[1]);
		
		/* activate cron to reset user priorities regularly */
		
		Timer t = new Timer();
		ResetUserPrioritiesJob resetUserPriority = new ResetUserPrioritiesJob();
	    t.scheduleAtFixedRate(resetUserPriority, 0, ServerProperties.DELAY_BETWEEN_RESET);
	    log.info("Reset user priorities every (ms): "+ServerProperties.DELAY_BETWEEN_RESET);
	    
		/* start server and monitoring tool */

		final HttpServer server = startServer();

		final ThreadPoolMonitor monitor = new ThreadPoolMonitor(RequestHandlerPool.getInstance(), 5);
		Thread monitorThread = new Thread(monitor);
		monitorThread.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				log.warn("SHUTDOWN SIGNAL DETECTED! - shutting down server!");
				monitor.shutdown();

				RequestHandlerPool.getInstance().shutdownNow();
				try {
					RequestHandlerPool.getInstance().awaitTermination(105, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					log.error("Error on shutting down server",e);
				}
				server.shutdownNow();		
				log.info("Shutdown successfull.");
			}
		});

	}
}
