package ch.eth.ir.indexserver.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
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
	
	public DemoStats() throws IOException {
		File indexDirectory = new File(INDEX_DIR);
		if (!indexDirectory.exists()) {
			log.fatal("Index directory '"+INDEX_DIR+"' does not exist!");
			throw new FileNotFoundException("Index directory '"+INDEX_DIR+"' could not be found.");
		}
		Directory index = FSDirectory.open(indexDirectory.toPath());
		indexReader = DirectoryReader.open(index);
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
		
	}
}
