package ch.eth.ir.indexserver.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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

import ch.eth.ir.indexserver.server.response.DocumentVectorBean;
import ch.eth.ir.indexserver.server.response.QueryTermResponse;
import ch.eth.ir.indexserver.server.response.QueryResultResponse;


/**
 * Singleton class IndexAPI 
 * 
 * Interface between the web server and the Lucene index. Performs queries and
 * retrieval against the index ands provides the results as beans for the web 
 * service.
 * 
 * Lucene's internal caching and optimizations require the existence of a 
 * single index reader and searcher. To avoid creating one reader per request,
 * this class is designed as a singleton and gets injected wherever needed.
 * Note since IndexReader and IndexSearcher are both thread-safe this class
 * is thread safe as well.
 */
@Singleton
public class IndexAPI {
	private static Logger log = Logger.getLogger(IndexAPI.class);
	
	private IndexReader reader = null;
	private IndexSearcher searcher = null;
	private Analyzer analyzer = null;
	
	private int averageDocumentLenght = -1;
	private long totalTokens = -1;
	private long uniqueTokens = -1;
	private int documentCount = -1;
	
	
	public IndexAPI() throws IOException {
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
		analyzer = new StandardAnalyzer();
		
		documentCount = reader.getDocCount(IndexConstants.CONTENT);
		
		Properties props = new Properties();
		FileInputStream stream = new FileInputStream("./index/index.properties");
		props.load(stream);
		stream.close();
		averageDocumentLenght = Integer.parseInt(props.getProperty("document.average.length"));
		totalTokens = Long.parseLong(props.getProperty("terms.total"));
		uniqueTokens = Long.parseLong(props.getProperty("terms.unique"));
	}
	
	/**
	 * Builds a Lucene query which searches for all documents which contain at least
	 * minimTermsShouldMatch terms of the given terms.
	 */
	private Query buildQuery(List<String> terms, int minimumTermsShouldMatch) {
		BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder();
		queryBuilder.setMinimumNumberShouldMatch(minimumTermsShouldMatch);
		for (String term : terms) {
			Term queryTerm = new Term(IndexConstants.CONTENT, term);
			queryBuilder.add(new TermQuery(queryTerm), Occur.SHOULD);
		}
		return queryBuilder.build();
	}
	
	
	/**
	 * Retrieves a document vector for a given documentID
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
	
	/**
	 * Returns all document id's of documents containing at least minimumTermsShouldMatch terms
	 * of the given terms.
	 */
	public QueryResultResponse findNOverlappingDocuments(int minimumTermsShouldMatch, List<String> terms) throws IOException {
		QueryResultResponse result = new QueryResultResponse();
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
	
	/**
	 * Applies the same preprocessing, word filtering and term splitting to the given query
	 * as was used for the documents at building time of the index 
	 * @throws IOException 
	 */
	public QueryTermResponse preprocess(String query) throws IOException {
		QueryTermResponse terms = new QueryTermResponse();
		// create stream and add char term attribute
		TokenStream stream = analyzer.tokenStream(IndexConstants.CONTENT, query);
		CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		while (stream.incrementToken()) {
			terms.addQueryTerm(cattr.toString());
		}
		stream.end();
		stream.close();
		return terms;
	}
	
	/** 
	 * Returns the document frequency for a given term
	 * @param term
	 * @return Document frequency of the term
	 */
	public int getDocumentFrequency(String term) throws IOException {
		Term luceneTerm = new Term(IndexConstants.CONTENT, new BytesRef(term));
		return reader.docFreq(luceneTerm);
	}
	
	/** 
	 * Returns the collection frequency for a given term
	 * @param term
	 * @return Collection frequency of the term
	 */
	public long getCollectionFrequency(String term) throws IOException {
		Term luceneTerm = new Term(IndexConstants.CONTENT, new BytesRef(term));
		return reader.totalTermFreq(luceneTerm);
	}
	
	/**
	 * Returns the number of documents in the collection
	 * @return
	 * @throws IOException
	 */
	public int getNumberOfDocuments() throws IOException {
		return documentCount;
	}
	
	public long getTotalNumberOfTerms() throws IOException {
		return this.totalTokens;
	}
	
	public long getNumberOfUniqueTerms() {
		return this.uniqueTokens;
	}
	
	public int getAverageDocumentLength() {
		return this.averageDocumentLenght;
	}
}
