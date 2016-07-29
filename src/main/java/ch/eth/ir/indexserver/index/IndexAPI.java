package ch.eth.ir.indexserver.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import ch.eth.ir.indexserver.resource.beans.DocumentVectorBean;
import ch.eth.ir.indexserver.resource.beans.QueryResultBean;


/**
 * Singleton class IndexAPI 
 * 
 * Interface between the web server and the Lucene index. Performs queries
 * retrieval against the index ands provides the results as Beans for the 
 * web service.
 * 
 * 
 * Lucene's internal caching and optimizations require the existence of a 
 * single index reader and searcher. To avoid creating one reader per request,
 * this class is a singleton object and gets injected wherever needed.
 */
@Singleton
public class IndexAPI {
	private static Logger log = Logger.getLogger(IndexAPI.class);
	
	private IndexReader reader = null;
	private IndexSearcher searcher = null;
	
	public IndexAPI() {
		File indexDirectory = new File(IndexConstants.INDEX_DIR);
		Directory index;
		try {
			index = FSDirectory.open(indexDirectory.toPath());
		} catch (IOException e) {
			log.fatal("could not open index directory", e);
			return;
		}
		try {
			reader = DirectoryReader.open(index);
		} catch (IOException e) {
			log.fatal("could not instantiate index reader",e);
			reader = null;
		}
		searcher = new IndexSearcher(reader);		
	}
	
	private Query buildQuery(List<String> terms, int minimumTermsShouldMatch) {
		log.info("called buildQuery(" + terms.toString() + ", " + minimumTermsShouldMatch + ")");

		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.setMinimumNumberShouldMatch(minimumTermsShouldMatch);
		for (String term : terms) {
			Term queryTerm = new Term(IndexConstants.CONTENT, term);
			queryBuilder.add(new TermQuery(queryTerm), Occur.SHOULD);
		}
		return queryBuilder.build();
	}
	
	
	/**
	 * 
	 */
	public DocumentVectorBean getDocumentVector(int docId) throws IOException {
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
	
	public QueryResultBean findNOverlappingDocuments(int minimumTermsShouldMatch, List<String> terms) throws IOException {
		log.info("called buildQuery(" + terms.toString() + ", " + minimumTermsShouldMatch + ")");
		QueryResultBean result = new QueryResultBean();
		if (terms.size() < minimumTermsShouldMatch) {
			log.warn("query will always be empty term.length < minimumTermsShouldMatch");
			return result;
		}
		// create query and search
		Query query = buildQuery(terms, minimumTermsShouldMatch);
		TopDocs luceneResult = searcher.search(query, reader.maxDoc());
		// no results
		if (luceneResult.totalHits == 0) {
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
