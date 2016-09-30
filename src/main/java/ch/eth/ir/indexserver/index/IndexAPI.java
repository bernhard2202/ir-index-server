package ch.eth.ir.indexserver.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import ch.eth.ir.indexserver.server.response.DocumentVectorBean;
import ch.eth.ir.indexserver.server.response.QueryTermResponse;


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
	
	public IndexSearcher getSearcher() {
		return searcher;
	}
	
	public IndexReader getReader() {
		return reader;
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
