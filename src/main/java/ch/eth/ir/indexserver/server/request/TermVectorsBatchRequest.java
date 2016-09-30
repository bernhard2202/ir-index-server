package ch.eth.ir.indexserver.server.request;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import ch.eth.ir.indexserver.index.IndexConstants;
import ch.eth.ir.indexserver.server.response.DocumentVectorBatchResponse;
import ch.eth.ir.indexserver.server.response.DocumentVectorBean;

public class TermVectorsBatchRequest extends AbstractPriorityRequest<DocumentVectorBatchResponse> {
	//TODOprivate static Logger logger = Logger.getLogger(QueryDocumentsRequest.class);

	private IndexReader reader;
	private List<Integer> ids;
	
	public TermVectorsBatchRequest(IndexReader reader, List<Integer> ids) {
		super();
		this.reader = reader;
		this.ids = ids;
	}
	
	/**
	 * Retrieves a document vector for a given documentID
	 */
	private DocumentVectorBean getDocumentVector(int docId) throws IOException {
		Map<String, Long> documentVector = new HashMap<String, Long>();
		Terms docTerms = reader.getTermVector(docId, IndexConstants.CONTENT);
		if (docTerms == null || docTerms.size() == 0) {
			return new DocumentVectorBean(docId, documentVector);
		}
		TermsEnum termEnumerator = docTerms.iterator();
		BytesRef text = null;
		while ((text = termEnumerator.next()) != null) {
			documentVector.put(text.utf8ToString(), termEnumerator.totalTermFreq());
		}
		return new DocumentVectorBean(docId, documentVector);
	}
	
	@Override
	public DocumentVectorBatchResponse call() throws Exception {
		DocumentVectorBatchResponse batchResponse = new DocumentVectorBatchResponse();
		for (int id : ids) {
			batchResponse.addDocumentVector(getDocumentVector(id));
		}
		return batchResponse;
	}
}
