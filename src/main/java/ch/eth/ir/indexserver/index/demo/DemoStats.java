package ch.eth.ir.indexserver.index.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.ParallelLeafReader;
import org.apache.lucene.index.SlowCodecReaderWrapper;
import org.apache.lucene.index.StandardDirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import ch.eth.ir.indexserver.index.IndexConstants;

public class DemoStats {
	private static Logger log = Logger.getLogger(DemoStats.class);
	
	private IndexReader indexReader = null;
	private final String INDEX_DIR = IndexConstants.INDEX_DIR;
	private Directory indexDirectory = null;
	
	public DemoStats() throws IOException {
		File index = new File(INDEX_DIR);
		if (!index.exists()) {
			log.fatal("Index directory '"+INDEX_DIR+"' does not exist!");
			throw new FileNotFoundException("Index directory '"+INDEX_DIR+"' could not be found.");
		}

		indexDirectory = FSDirectory.open(index.toPath());
		indexReader = DirectoryReader.open(indexDirectory);
	}

	public int getTermFrequency(String term, int docId) throws IOException{
		log.info("called getTermFrequency("+term+", "+docId+")");
		Terms docTerms = null;
		docTerms = indexReader.getTermVector(docId, IndexConstants.CONTENT);
		if (docTerms == null || docTerms.size() == 0) {
			log.debug("doc '"+docId+"' not found or empty");
			return 0;
		}
		TermsEnum termEnumerator = docTerms.iterator();
		BytesRef text = null;
		while ((text = termEnumerator.next()) != null) {
			if (term.equals(text.utf8ToString())){
				return (int) termEnumerator.totalTermFreq();
			}
		}
		log.debug("term '"+term+"' not found in  doc '"+docId+"'");
		return 0;
	}
	
	public long getCollectionFrequency(String term) throws IOException {
		log.info("called getCollectionFrequency("+term+")");
		Term luceneTerm = new Term(IndexConstants.CONTENT, new BytesRef(term));
		return indexReader.totalTermFreq(luceneTerm);
	}
	
	public int getDocumentFrequency(String term) throws IOException {
		log.info("called getDocumentFrequency("+term+")");
		Term luceneTerm = new Term(IndexConstants.CONTENT, new BytesRef(term));
		return indexReader.docFreq(luceneTerm);
	}
	
	public int getDocumentLenght(int docId) throws IOException {
		log.info("called getDocumentLenght("+docId+")");
		Terms docTerms = null;
		docTerms = indexReader.getTermVector(docId, IndexConstants.CONTENT);
		if (docTerms == null || docTerms.size() == 0) {
			log.debug("doc '"+docId+"' not found or empty");
			return 0;
		}
		TermsEnum termEnumerator = docTerms.iterator();
		int lenght = 0;
		while (termEnumerator.next() != null) {	
			lenght += (int) termEnumerator.totalTermFreq();
		}
		return lenght;
	}
	
	public void getTermCount() throws IOException {		
		Map<String, Long> terms = new HashMap<String,Long>();

		for (LeafReaderContext context : indexReader.leaves()) {
			Terms leafTerms = context.reader().terms(IndexConstants.CONTENT);
			TermsEnum termEnumerator = leafTerms.iterator();
			BytesRef text = null;
			while ((text = termEnumerator.next()) != null) {
				String term = text.utf8ToString();
				Long freq = termEnumerator.totalTermFreq(); 
				if (terms.containsKey(term)) {
					terms.put(term, terms.get(term)+freq);
				} else {
					terms.put(term, freq);
				}
			}
		}
		long total = 0;
		for (Long c : terms.values()) {
			total += c;
		}
		System.out.println("unique terms:" +terms.keySet().size());
		System.out.println("total terms in index:"+total);
	}
	
	public static void main(String[] args) throws IOException {		
		long startTime, endTime;
		int result;
		int docid = 107074;
		String term = "china";
		
		DemoStats instance = new DemoStats();
		
		// get term frequency
		startTime = System.currentTimeMillis();
		result = instance.getTermFrequency(term, docid);
		endTime = System.currentTimeMillis();
		System.out.println("Result: " + result + ", in "+(endTime-startTime)+"ms");
		
		// get document frequency 
		startTime = System.currentTimeMillis();
		result = instance.getDocumentFrequency(term);
		endTime = System.currentTimeMillis();
		System.out.println("Result: " + result + ", in "+(endTime-startTime)+"ms");

		// get document length 
		startTime = System.currentTimeMillis();
		result = instance.getDocumentLenght(docid*2);
		endTime = System.currentTimeMillis();
		System.out.println("Result: " + result + ", in "+(endTime-startTime)+"ms");
		
		System.out.println("term stats");
		instance.getTermCount();
		
	}
}
