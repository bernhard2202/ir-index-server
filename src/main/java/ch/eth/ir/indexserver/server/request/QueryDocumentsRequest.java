package ch.eth.ir.indexserver.server.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;

import ch.eth.ir.indexserver.index.IndexConstants;
import ch.eth.ir.indexserver.server.config.RequestProperties;
import ch.eth.ir.indexserver.server.response.QueryResultResponse;

/**
 * Queries documents from the index
 */
public class QueryDocumentsRequest extends AbstractRequest<QueryResultResponse> {
	private IndexSearcher searcher;
	private Set<String> terms;
	private int nOverlappingTerms;
	
	public QueryDocumentsRequest(IndexSearcher searcher, Set<String> terms, int nOverlappingTerms) {
		super();
		this.searcher = searcher;
		this.terms = terms;
		this.nOverlappingTerms = nOverlappingTerms==0 ? terms.size() : nOverlappingTerms;
	}

	/**
	 * Builds a Lucene query which searches for all documents which contain at least
	 * minimTermsShouldMatch terms of the given terms.
	 */
	private Query buildQuery() {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.setMinimumNumberShouldMatch(nOverlappingTerms);
		for (String term : terms) {
			Term queryTerm = new Term(IndexConstants.CONTENT, term);
			queryBuilder.add(new TermQuery(queryTerm), Occur.SHOULD);
		}
		return queryBuilder.build();
	}
	
	
	/**
	 * Perform the given query on the index
	 */
	public QueryResultResponse call() throws Exception {
		QueryResultResponse result = new QueryResultResponse();
		
		// create query and search
		Query query = buildQuery();
		TopDocs luceneResult = null;
		luceneResult = searcher.search(query, RequestProperties.MAX_SEARCH_RESULTS);
		
		// extract document id's
		ArrayList<Integer> docIds = new ArrayList<Integer>(luceneResult.totalHits);
		int max = Math.min(RequestProperties.MAX_SEARCH_RESULTS, luceneResult.totalHits);
		for (int i = 0; i < max; i++) {
			docIds.add(luceneResult.scoreDocs[i].doc);
		}
		
		// shuffle the results to hide the ranking algorithm
		Collections.shuffle(docIds);
		result.addAllDocuments(docIds);
		return result;		
	}

}
