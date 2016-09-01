package ch.eth.ir.indexserver.client;

import java.util.Random;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Main {
	public static void main(String[] args) {
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://localhost:7777/ir-server").path("document/vector");
		Random rand = new Random();
		
		int requestNotOk = 0;
		int i = 0;
		long responseTime=0;
		
		int maxRequest = 60050;
		
		for (; i < maxRequest; i++) {
			try {
				WebTarget requestTarget = target.queryParam("id", rand.nextInt(1078050)+1);
				Invocation.Builder invocationBuilder =
						requestTarget.request(MediaType.APPLICATION_JSON);
				invocationBuilder.header("Authorization", "Bearer j2vsp3a99ot6p3ch7huip936n6");
				
				long startR = System.currentTimeMillis();
				
				Response response = invocationBuilder.get();
				
				responseTime += (System.currentTimeMillis()-startR);
				
				if (response.getStatus()!=200) {
					requestNotOk++;
				}
				if(i%1000==0) {
					System.out.println(i+"/"+maxRequest);
				}
				Thread.sleep(2);
			} catch (Exception e) {
				e.printStackTrace(System.err);
				requestNotOk++;
				continue;
			}
		}
		
		System.out.println("Sent requests: "+i+" of "+maxRequest);
		System.out.println("Successful responses (200): "+(i-requestNotOk));
		System.out.println("Average response time per sent request: "+(responseTime/i) + "ms");
		
	}
}
