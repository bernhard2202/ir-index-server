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

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;


public class UserPropertiesWriter {
	
	private static Map<String, String> credentials = new HashMap<String,String>();
	
	// make main loop with input:
	// exit - leave
	// add 
		// gimme user-name: (if already exists then replace, otherwise add new)
		// token issued:
	// write
	// list
		// prints all users and decrypted tokens

	public static void main(String[] args) throws IOException {				
		Random random = new SecureRandom();

		if (args == null || args.length != 1) {
			System.err.println("provide a password!");
			System.exit(-1);
		}		
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();     
		encryptor.setPassword(args[0]);

		Properties props = new EncryptableProperties(encryptor); 
		try {
			props.load(new FileInputStream("./users.properties")); 
		} catch (FileNotFoundException e) {
			// ignore if no properties file exists no user will be 
			// loaded but this is not an error
		}
		System.out.println("Reading current configuration....");
		
		int i = 1;
		while ((props.getProperty("user."+i+".name")) != null) {
		  String user = props.getProperty("user."+i+".name");
		  String token = props.getProperty("user."+i+".token");
		  if (user != null && token != null) {
			  System.out.println("loaded: "+user);
			  credentials.put(user,token);
		  }
		  i++;
		}
		
		System.out.println(i-1+" users loaded!");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		String nextCommand = "";
		while (!nextCommand.equals("exit")){
			System.out.println("\nEnter next command (type 'exit' to stop, 'help' for help):");
			nextCommand = br.readLine().trim();
			
			if (nextCommand.equals("exit")) {
				
			} else if (nextCommand.equals("print")) {
				for (Map.Entry<String,String> entry : credentials.entrySet()) {
					System.out.println(entry.getKey()+": "+entry.getValue());
				}		
			} else if (nextCommand.equals("persist")) {
				OutputStream stream = new FileOutputStream(new File("./users.properties"));
				props = new Properties();
				i=0;
				for (Map.Entry<String,String> entry : credentials.entrySet()) {
					i++;
					props.setProperty("user."+i+".name", entry.getKey());
					props.setProperty("user."+i+".token", "ENC("+encryptor.encrypt(entry.getValue())+")");
				} 
				props.store(stream, "User credentials for Authentification");
				stream.close();	
				System.out.println(i+ " Users written to ./users.properties");
			} else if (nextCommand.startsWith("add ")) {
				String username = nextCommand.substring(3).trim();
				String token = new BigInteger(130, random).toString(32);
				credentials.put(username, token);
				System.out.println("Added user: "+username+"("+token+")");
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
					//TODO
				} else {
					String token = new BigInteger(130, random).toString(32);
					credentials.put(username, token);
				}		
			} else if (nextCommand.equals("help")) {
				
			} else {
				System.out.println("Invalid command!");
			}
		}

		
	}
}
