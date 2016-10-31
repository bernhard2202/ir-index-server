package ch.eth.ir.indexserver.server.response;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 
 * Encapsulates a number of Document Vector Beans {@see DocumentVectorBean}
 * for responding batch requests.
 */
public class DocumentVectorBatchResponse extends AbstractResponse {
	private List<DocumentVectorBean> documentVectors;

	public DocumentVectorBatchResponse() {
		documentVectors = new ArrayList<DocumentVectorBean>();
	}
	
	public void addDocumentVector(DocumentVectorBean docVec) {
		this.documentVectors.add(docVec);
	}

	@JsonProperty(value="document")
	public List<DocumentVectorBean> getDocumentVectors() {
		return this.documentVectors;
	}
}
