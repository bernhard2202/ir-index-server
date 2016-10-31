package ch.eth.ir.indexserver.server.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

/**
 * Static class to load and decrypt user properties in memory during 
 * runtime.
 */
public class UserProperties {
	
	private static StandardPBEStringEncryptor encryptor = null;
	// maps user tokens to user names 
	private static HashMap<String, String> credentials = null;
	// keeps track of usage statistics
	// mapping user names to the number of requests performed by them
	private static ConcurrentHashMap<String, Integer> requestCount = null;
	
	private static Logger log = Logger.getLogger(UserProperties.class);
	
	/**
	 * read and load the encrypted properties file using the provided password
	 */
	public static void load(String filename, String password) throws IOException {
		encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(password);
		credentials = new HashMap<String, String>();
		requestCount = new ConcurrentHashMap<String, Integer>();
		
		Properties props = new EncryptableProperties(encryptor);
		FileInputStream stream = new FileInputStream(filename);
		props.load(stream);

		int i = 1;
		while ((props.getProperty("user." + i + ".name")) != null) {
			String user = props.getProperty("user." + i + ".name");
			String token = props.getProperty("user." + i + ".token");
			if (user != null && token != null) {
				log.info("loaded user config for: " + user);
				credentials.put(token, user);
				requestCount.put(user, 0);
			}
			i++;
		}
		stream.close();
	}

	/**
	 * Returns true if the given token belongs to a valid user 
	 */
	public static boolean validateToken(String token) {
		if (credentials == null) {
			log.warn("token validation failed due to empty user config file," +
					" make sure the file was loaded correctly");
			return false;
		}
		return credentials.containsKey(token);
	}
	
	public static void resetRequestCounts() {
		log.warn("Reset user priorities");
		for (String username : credentials.values()) {
			requestCount.put(username, 0);
		}
	}
	
	/**
	 * Increase the request count for the given user by one
	 */
	public static void increaseRequestCount(String userName) {
		if (requestCount.containsKey(userName))
			requestCount.put(userName, requestCount.get(userName)+1);
	}

	/**
	 * Return the username for a given token 
	 */
	public static String getNameForToken(String token) {
		return credentials.get(token);
	}

	/** 
	 * Returns the request count for a given username
	 */
	public static int getRequestsForUser(String name) {
		if (requestCount.containsKey(name)) {
			return requestCount.get(name);
		}
		return 0;
	}
}
