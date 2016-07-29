package ch.eth.ir.indexserver.resource.beans;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/** 
 * Encapsulates a number of Document Vector Beans {@see DocumentVectorBean}
 * for responding batch requests.
 */
public class DocumentVectorBatch {
	private List<DocumentVectorBean> documentVectors;

	public DocumentVectorBatch() {
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
