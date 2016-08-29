package ch.eth.ir.indexserver.server.security;

import javax.inject.Singleton;

@Singleton
public class UserProperties {
	
	public UserProperties() { }

	public boolean validateToken(String token) {
		return token.equals("lalelu");
	}
	
}
