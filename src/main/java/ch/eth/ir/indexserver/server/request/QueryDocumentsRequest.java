package ch.eth.ir.indexserver.server.request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
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
public class QueryDocumentsRequest extends AbstractPriorityRequest<QueryResultResponse> {
	private static Logger logger = Logger.getLogger(QueryDocumentsRequest.class);

	private IndexSearcher searcher;
	private List<String> terms;
	private int nOverlappingTerms;
	
	public QueryDocumentsRequest(IndexSearcher searcher, List<String> terms, int nOverlappingTerms) {
		super();
		this.searcher = searcher;
		this.terms = terms;
		this.nOverlappingTerms = nOverlappingTerms;
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
		if (terms.size() < nOverlappingTerms) {
			logger.warn("query will always be empty term.length < minimumTermsShouldMatch");
			return null;
		}
		if (terms.size()==2)
			Thread.sleep(20000);
		
		
		// create query and search
		Query query = buildQuery();
		TopDocs luceneResult = null;
		try {
			luceneResult = searcher.search(query, RequestProperties.MAX_SEARCH_RESULTS);
		} catch (IOException e) {
			logger.error("error on searching the index", e);
			return null;
		}
		// no results
		if (luceneResult == null || luceneResult.totalHits == 0) {
			return result;
		}
		
		// extract document id's
		ArrayList<Integer> docIds = new ArrayList<Integer>(luceneResult.totalHits);
		for (int i = 0; i < luceneResult.totalHits; i++) {
			docIds.add(luceneResult.scoreDocs[i].doc);
		}
		
		// shuffle the results to hide the ranking algorithm
		Collections.shuffle(docIds);
		result.addAllDocuments(docIds);
		return result;		
	}

}
