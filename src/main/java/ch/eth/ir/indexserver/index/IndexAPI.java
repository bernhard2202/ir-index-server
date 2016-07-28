package ch.eth.ir.indexserver.index;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import ch.eth.ir.indexserver.resource.beans.DocumentVectorBean;


/**
 * Singleton class IndexAPI 
 * 
 * Interface between the web server and the Lucene index. Lucene's internal
 * caching and optimizations require the existence of a single index reader
 * and searcher. To avoid creating one reader per request, this class encapsulates
 * both as singleton and gets injected wherever needed.
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

}
