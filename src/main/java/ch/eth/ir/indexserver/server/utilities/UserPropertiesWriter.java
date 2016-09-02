package ch.eth.ir.indexserver.server.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;


/**
 * Command line tool to add and remove existing users
 * or refresh their tokens
 */
public class UserPropertiesWriter {
	
	/* 
	 * map storing user credentials 
	 * name -> token
	 */
	private static Map<String, String> credentials = new HashMap<String,String>();

	// used to issue random tokens
	private static Random random = new SecureRandom();

	/*
	 * Generates and returns a new random token 
	 */
	private static String getNewToken() {
		return new BigInteger(130, random).toString(32);
	}
	
	
	/*
	 * loads an existing properties file and returns the number of loaded users
	 */
	private static int loadUsers(String filename, StringEncryptor encryptor) throws IOException {
		Properties props = new EncryptableProperties(encryptor); 
		try {
			FileInputStream stream = new FileInputStream(filename);
			props.load(stream); 
			stream.close();
		} catch (FileNotFoundException e) {
			// ignore if no properties file exists no user will be 
			// loaded but this is not an error
			return 0;
		}
		
		int i = 1;
		while ((props.getProperty("user."+i+".name")) != null) {
		  String user = props.getProperty("user."+i+".name");
		  String token = props.getProperty("user."+i+".token");
		  if (user != null && token != null) {
			  credentials.put(user,token);
		  }
		  i++;
		}
		return i-1;
	}
	
	/* persists users to properties file and returns number of persisted users*/
	private static int persistProperties(String filename, StringEncryptor encryptor) throws IOException {
		OutputStream stream = new FileOutputStream(new File(filename));
		Properties props = new Properties();
		int i=0;
		for (Map.Entry<String,String> entry : credentials.entrySet()) {
			i++;
			props.setProperty("user."+i+".name", entry.getKey());
			props.setProperty("user."+i+".token", "ENC("+encryptor.encrypt(entry.getValue())+")");
		} 
		props.store(stream, "User credentials for Authentification - do not change manually!");
		stream.close();	
		return i;
	}
	
	/* adds or updates user and returns the new issued token */
	private static String addOrUpdateUser(String name) {
		String token = getNewToken();
		credentials.put(name, token);
		return token;
	}
	
	/*
	 * run UserPropertiesWriter <path to properties file> <password>
	 */
	public static void main(String[] args) throws IOException {				
		if (args == null || args.length != 2) {
			System.err.println("provide filename and password");
			System.exit(-1);
		}	
		
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();     
		encryptor.setPassword(args[1]);

		// Load existing configuration
		System.out.println("Reading current configuration ('"+args[0]+"')....");
		int users = loadUsers(args[0], encryptor);
		System.out.println(users+" users loaded!");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String nextCommand = "";
		while (!nextCommand.equals("exit")){
			System.out.println("\nEnter next command (type 'exit' to stop, 'help' for help):");
			nextCommand = br.readLine().trim();
			if (nextCommand.equals("exit")) {
				break;
			} else if (nextCommand.equals("print")) {
				for (Map.Entry<String,String> entry : credentials.entrySet()) {
					System.out.println(entry.getKey()+": "+entry.getValue());
				}		
			} else if (nextCommand.equals("persist")) {
				int persisted = persistProperties(args[0], encryptor);
				System.out.println(persisted+ " Users written to ./users.properties");
			} else if (nextCommand.startsWith("add ")) {
				String username = nextCommand.substring(3).trim();
				if (credentials.containsKey(username)) {
					System.out.println("user already exists, use 'remove' or 'refresh'");
				} else {
					String token = addOrUpdateUser(username);
					System.out.println("Added user: "+username+"("+token+")");
				}
			} else if (nextCommand.startsWith("remove ")) {
				String username = nextCommand.substring(6).trim();
				if (username.equals("*")) {
					credentials.clear();
				} else {
					credentials.remove(username);
				}
			} else if (nextCommand.startsWith("refresh ")) {
				String username = nextCommand.substring(6).trim();
				if (username.equals("*")) {
					for (String user : credentials.keySet()) {
						addOrUpdateUser(user);
					}
				} else if (credentials.containsKey(username)){
					String token = addOrUpdateUser(username);
					System.out.println("Updated user: "+username+"("+token+")");
				}		
			} else if (nextCommand.equals("help")) {
				System.out.println("persist\t\t\tpersists the user config");
				System.out.println("print\t\t\tprints all users and keys");
				System.out.println("add <username>\t\tadds a new user with the given name");
				System.out.println("refresh <*|username>\trefreshes token for given user name or for all users (*)");
				System.out.println("remove <*|username>\ttremoves the given user or all users (*)");
				System.out.println("help\t\t\tshows this message");
			} else {
				System.out.println("Invalid command!");
			}
		}
	}
}
