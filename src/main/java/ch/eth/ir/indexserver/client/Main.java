package ch.eth.ir.indexserver.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

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
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target("http://idvm-infk-hofmann04.inf.ethz.ch:8080/irserver/");
//		WebTarget target = client.target("http://localhost:8080/irserver/");
		Random rand = new Random();
		
		String credentials = "Bearer 1audh2egrg542je98292t92l35";
//		String credentials = "Bearer svifpfvdl9fqso91t8fgs8432j";
		int requestDocumentNotOk = 0;
		int requestDocumentOk=0;
		int requestQueryOk=0;
		int requestQueryNotOk=0;
		
		int requestFailedWithException=0;
		
		int i = 1;
		long responseTimeQuery=0;
		long responseTimeDoc=0;

		int maxRequest = 1000;
		
		for (; i < maxRequest+1; i++) {
			try {
				WebTarget requestTarget = target.path("document/vector");
				for (int j=0; j<99; j++) {
					requestTarget = requestTarget.queryParam("id", rand.nextInt(1078050)+1);
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
								
				DocumentVectorBatchResponse batch = response.readEntity(DocumentVectorBatchResponse.class);
				response.close();
				
				if (batch.getDocumentVectors().size() > 0) {
					if (batch.getDocumentVectors().get(0).getTermFrequencies().size() > 3) {
						Map<String, Long> termVector = batch.getDocumentVectors().get(0).getTermFrequencies();
						List<String> terms = new ArrayList<String>();
						terms.addAll(termVector.keySet());
						
						requestTarget = target.path("index/query");
						
						for (int j=0; j<4; j++) {
							int index = rand.nextInt(termVector.size());
							requestTarget = requestTarget.queryParam("term", terms.get(index));
						}
						requestTarget = requestTarget.queryParam("minOverlap", 3);
						
						invocationBuilder = requestTarget.request(MediaType.APPLICATION_JSON);
						invocationBuilder.header("Authorization", credentials);
						
						
						startR = System.currentTimeMillis();
						response = invocationBuilder.get();
						responseTimeQuery += (System.currentTimeMillis()-startR);
						
						@SuppressWarnings("unused")
						QueryResultResponse queryResult = response.readEntity(QueryResultResponse.class);

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
