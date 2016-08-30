package ch.eth.ir.indexserver.server.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

import ch.eth.ir.indexserver.index.IndexAPI;

/**
 * Static class to load and decrypt user properties in memory during 
 * runtime.
 */
public class UserProperties {
	
	private static StandardPBEStringEncryptor encryptor = null;
	// maps decrypted token to user name 
	private static HashMap<String, String> credentials = null;
	
	private static Logger log = Logger.getLogger(IndexAPI.class);

	/**
	 * read and load the encrypted properties file using the provided password
	 */
	public static void load(String password) throws IOException {
		encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(password);
		credentials = new HashMap<String, String>();

		Properties props = new EncryptableProperties(encryptor);
		props.load(new FileInputStream("./users.properties"));

		int i = 1;
		while ((props.getProperty("user." + i + ".name")) != null) {
			String user = props.getProperty("user." + i + ".name");
			String token = props.getProperty("user." + i + ".token");
			if (user != null && token != null) {
				log.info("loaded user config for: " + user);
				credentials.put(token, user);
			}
			i++;
		}
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

}
