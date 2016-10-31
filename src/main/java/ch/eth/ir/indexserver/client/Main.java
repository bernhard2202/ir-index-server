package ch.eth.ir.indexserver.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.eth.ir.indexserver.server.response.DocumentVectorBatchResponse;
import ch.eth.ir.indexserver.server.response.QueryResultResponse;

public class Main {
	public static void main(String[] args) {
		if (args.length != 2 || args[0].length()>100 || args[1].length()>100) {
			System.out.println("usage client <url> <token>");
			System.exit(-1);
		}
		Client client = ClientBuilder.newClient();
//		WebTarget target = client.target("http://idvm-infk-hofmann04.inf.ethz.ch:8080/irserver/");
		WebTarget target = client.target(args[0]);
		Random rand = new Random();
		
		String credentials = "Bearer "+args[1];
		int requestDocumentNotOk = 0;
		int requestDocumentOk=0;
		int requestQueryOk=0;
		int requestQueryNotOk=0;
		
		int requestFailedWithException=0;
		
		int i = 1;
		long responseTimeQuery=0;
		long responseTimeDoc=0;

		int maxRequest = 100000;
		
		for (; i < maxRequest+1; i++) {
			try {
				WebTarget requestTarget = target.path("document/vector");
				for (int j=0; j<99; j++) {
					requestTarget = requestTarget.queryParam("id", rand.nextInt(828916)+1);
				}
				Invocation.Builder invocationBuilder =
						requestTarget.request(MediaType.APPLICATION_JSON);
				invocationBuilder.header("Authorization", credentials);
				
				long startR = System.currentTimeMillis();
				
				Response response = invocationBuilder.get();
				
				
				responseTimeDoc += (System.currentTimeMillis()-startR);
				
				if (response.getStatus()!=200) {
					requestDocumentNotOk++;
				} else {
					requestDocumentOk++;
				}
				DocumentVectorBatchResponse batch = null;
				try {
					batch = response.readEntity(DocumentVectorBatchResponse.class);
				} catch (Exception e) {
					e.printStackTrace(System.err);
					
					System.err.println(response.readEntity(String.class));
					requestFailedWithException++;
					continue;
				}
				response.close();
				
				if (batch != null && batch.getDocumentVectors().size() > 0) {
					if (batch.getDocumentVectors().get(0).getTermFrequencies().size() > 3) {
						Map<String, Long> termVector = batch.getDocumentVectors().get(0).getTermFrequencies();
						List<String> terms = new ArrayList<String>();
						terms.addAll(termVector.keySet());
						
						requestTarget = target.path("index/query");
						Set<String> queryTerms = new HashSet<String>();
						for (int j=0; j<4; j++) {
							int index = rand.nextInt(termVector.size());
							queryTerms.add(terms.get(index));
						}
						
						for (String s : queryTerms)
							requestTarget = requestTarget.queryParam("term",s);
						requestTarget = requestTarget.queryParam("minOverlap", Math.min(queryTerms.size(), 3));
						
						invocationBuilder = requestTarget.request(MediaType.APPLICATION_JSON);
						invocationBuilder.header("Authorization", credentials);
						
						
						startR = System.currentTimeMillis();
						response = invocationBuilder.get();
						responseTimeQuery += (System.currentTimeMillis()-startR);
						
						try {
							@SuppressWarnings("unused")
							QueryResultResponse queryResult = response.readEntity(QueryResultResponse.class);
						} catch (Exception e) {
							e.printStackTrace(System.err);

							System.err.println(response.readEntity(String.class));
							requestFailedWithException++;
						}
						if (response.getStatus()!=200) {
							requestQueryNotOk++;
						} else {
							requestQueryOk++;
						}
						
						response.close();
					}
				}

				if(i%100==0) {
					System.out.println(i+"/"+maxRequest);
					System.out.println((responseTimeDoc/(requestDocumentOk+requestDocumentNotOk+1)));
				}
				System.out.print(".");
			} catch (Exception e) {
				e.printStackTrace(System.err);
				requestFailedWithException++;
				continue;
			}
		}
		
		System.out.println("\nSuccesfull document vector requests: "+requestDocumentOk+"; Errors: "+requestDocumentNotOk);
		System.out.println("Average resp. time:  "+(responseTimeDoc/(requestDocumentOk+requestDocumentNotOk+1)));
		
		System.out.println("Succesfull query requests: "+requestQueryOk+"; Errors: "+requestQueryNotOk);
		System.out.println("Average resp. time:  "+(responseTimeQuery/(requestQueryOk+requestQueryNotOk+1)));

		System.out.println("Failed requests: "+requestFailedWithException);
	}
}
