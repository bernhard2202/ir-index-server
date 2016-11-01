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

public class TestClient {
	public static void main(String[] args) {
		if (args.length != 2 || args[0].length()>100 || args[1].length()>100) {
			System.out.println("usage client <url> <token>");
			System.exit(-1);
		}
	
		Client client = ClientBuilder.newClient();
		Random rand = new Random();
		
		/* target */ 
		
		WebTarget target = client.target(args[0]);
		String credentials = "Bearer "+args[1];
		
		/* statistics */
		
		int requestDocumentNotOk = 0;
		int requestDocumentOk = 0;
		int requestQueryOk = 0;
		int requestQueryNotOk = 0;		
		int requestFailedWithException=0;
		long responseTimeQuery=0;
		long responseTimeDoc=0;
		
		/* how many requests to issue in one experiment */
		int maxRequest = 10000;
		
		int i = 1;
		for (; i < maxRequest+1; i++) {
			/* thats a giant try-catch but we are not interested in proper error handling
			   rather than how many requests will make their way without ANY error */
			try {
				
				/* request document vectors for 100 random generated doc id's */
				
				WebTarget requestTarget = target.path("document/vector");
				for (int j=0; j<99; j++) {
					requestTarget = requestTarget.queryParam("id", rand.nextInt(828916)+1);
				}
				Invocation.Builder invocationBuilder =
						requestTarget.request(MediaType.APPLICATION_JSON);
				invocationBuilder.header("Authorization", credentials);
				
				/* do request */
				
				long startR = System.currentTimeMillis();
				Response response = invocationBuilder.get();				
				responseTimeDoc += (System.currentTimeMillis()-startR);
				
				/* count successful responses */
				
				DocumentVectorBatchResponse batch = null;
				if (response.getStatus()!=200) {
					requestDocumentNotOk++;
				} else {
					requestDocumentOk++;
					try {
						batch = response.readEntity(DocumentVectorBatchResponse.class);
					} catch (Exception e) {					
						System.err.println(response.readEntity(String.class));
						requestFailedWithException++;
						continue;
					}
				}

				response.close();
				
				/* now issue a random query with some of the terms received by the document */
				
				if (batch != null && batch.getDocumentVectors().size() > 0) {
					if (batch.getDocumentVectors().get(0).getTermFrequencies().size() > 3) {
						
						/* build query */
						
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
						requestTarget = requestTarget.queryParam("minOverlap", Math.min(queryTerms.size(), 2));
						
						invocationBuilder = requestTarget.request(MediaType.APPLICATION_JSON);
						invocationBuilder.header("Authorization", credentials);
						
						/* send request */
						
						startR = System.currentTimeMillis();
						response = invocationBuilder.get();
						responseTimeQuery += (System.currentTimeMillis()-startR);
						
						/* count successful responses */
						
						if (response.getStatus()!=200) {
							requestQueryNotOk++;
						} else {
							requestQueryOk++;
							try {
								@SuppressWarnings("unused")
								QueryResultResponse queryResult = response.readEntity(QueryResultResponse.class);
							} catch (Exception e) {
								System.err.println(response.readEntity(String.class));
								requestFailedWithException++;
							}
						}
						
						response.close();
					}
				}
				
				/* print some progress */
				
				if(i%100==0) {
					System.out.println(i+"/"+maxRequest);
				}
				System.out.print(".");
				
			} catch (Exception e) {
				e.printStackTrace(System.err);
				requestFailedWithException++;
				continue;
			}
		}
		
		/* show stats */
		
		System.out.println("\nSuccesfull document vector requests: "+requestDocumentOk+"; Errors: "+requestDocumentNotOk);
		System.out.println("Average resp. time:  "+(responseTimeDoc/(requestDocumentOk+requestDocumentNotOk+1)));
		
		System.out.println("Succesfull query requests: "+requestQueryOk+"; Errors: "+requestQueryNotOk);
		System.out.println("Average resp. time:  "+(responseTimeQuery/(requestQueryOk+requestQueryNotOk+1)));

		System.out.println("Failed requests: "+requestFailedWithException);
	}
}
