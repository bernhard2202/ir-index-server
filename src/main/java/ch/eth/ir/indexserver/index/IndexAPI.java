package ch.eth.ir.indexserver.index;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


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
	
	public IndexReader getReader() {
		return reader;
	}
	
	public IndexSearcher getSearcher() {
		return searcher;
	}

}
