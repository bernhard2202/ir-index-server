package ch.eth.ir.indexserver.index.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import ch.eth.ir.indexserver.index.IndexConstants;

/**
 * Index writer for the TIPSTER corpus
 * Given the root directory, all TIPSTER .zip files are read and 
 * their XML-File content extracted and written to an new index.
 * Furthermore some statistics (which wont change over time but are time intense to extract) 
 * are calculated and written to a properties file to safe runtime. 
 */
public class TIPSTERCorpusIndexer {
	private static Logger log = Logger.getLogger(TIPSTERCorpusIndexer.class);

	/* index corpus into this directory */
	static final File INDEX_DIR = new File(IndexConstants.INDEX_DIR);
	/* the index writer */
	private IndexWriter writer = null;
	/* field type for document content */
	FieldType contentFieldType = null;

	public TIPSTERCorpusIndexer() {
		IndexWriterConfig indexConfig = new IndexWriterConfig(new StandardAnalyzer());
		try {
			Directory indexDir = FSDirectory.open(INDEX_DIR.toPath());
			writer = new IndexWriter(indexDir, indexConfig);
		} catch (IOException e) {
			log.fatal("Creating index writer failed, index_dir = '"+INDEX_DIR.getAbsolutePath()+"'", e);
		}
		/* create special field type for content */
		contentFieldType = new FieldType();
		// indexes the documents and term frequencies but no offsets
		contentFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS);
		contentFieldType.setStored(true);
		contentFieldType.setStoreTermVectors(true);
		contentFieldType.setTokenized(true);
		// the freeze() call prevents further modifications in the future 
		// without re-running this indexer
		contentFieldType.freeze();
	}

	public void close() throws CorruptIndexException, IOException {
		writer.close();
	}

	/** 
	 * Index a single XML file in the corpus, extract the documentNumber and content 
	 */
	private void indexZipEntry(ZipFile zipFile, ZipEntry zipEntry) throws IOException, DocumentException {
		log.debug("Indexing file: " + zipEntry.getName());
		
		// extract the file content
		InputStream contentStream = zipFile.getInputStream(zipEntry);
        SAXReader reader = new SAXReader();
        org.dom4j.Document XMLDocument = reader.read(contentStream);
        
        if (XMLDocument==null) {
        	System.out.println(zipEntry.getName());
        	return;
        }

        /*
         * CHANGE THIS CODE IF XML FILE STRUCTURE CHANGED 
         */
        Node titleNode = XMLDocument.selectSingleNode( "/DOC/DOCNO");	
		String title = titleNode==null ? "" : titleNode.getText();
        
        /*
         * CHANGE THIS CODE IF XML FILE STRUCTURE CHANGED 
         */
		@SuppressWarnings("unchecked")
		List<Node> contentNodes = XMLDocument.selectNodes("/DOC/TEXT");
		StringBuffer contentB = new StringBuffer();
		for (Node node : contentNodes) {
			contentB.append(node.getText());
		}
		String content = contentB.toString();
		
		IOUtils.closeQuietly(contentStream);
		
		// index file content
		Field contentField = new Field(IndexConstants.CONTENT, content.trim(), contentFieldType);
		// index file name
		Field fileNameField = new StringField(IndexConstants.TITLE, title.trim(), Store.YES);
		
		Document document = new Document();
		document.add(contentField);
		document.add(fileNameField);
		writer.addDocument(document);
	}
	
	/**
	 * Extracts a dictionary for the given index reader. The dictionary
	 * maps every word in the corpus to the number of times it occurs 
	 * @throws IOException 
	 */
	private Map<String, Long> getDictionary(IndexReader indexReader) throws IOException {
		Map<String, Long> dict = new HashMap<String,Long>();
		for (LeafReaderContext context : indexReader.leaves()) {
			Terms leafTerms = context.reader().terms(IndexConstants.CONTENT);
			TermsEnum termEnumerator = leafTerms.iterator();
			BytesRef text = null;
			while ((text = termEnumerator.next()) != null) {
				String term = text.utf8ToString();
				if (dict.containsKey(term)) {
					dict.put(term, dict.get(term)+termEnumerator.totalTermFreq());
				} else {
					dict.put(term, termEnumerator.totalTermFreq());
				}
			}
		}
		return dict;
	}
	
	private long getDocumentLenght(Terms docTerms) throws IOException {
		if (docTerms == null) {
			return 0;
		}
		TermsEnum termEnumerator = docTerms.iterator();
		int lenght = 0;
		while (termEnumerator.next() != null) {	
			lenght += (int) termEnumerator.totalTermFreq();
		}
		return lenght;
	}
	
	/** 
	 * Extracts the average document length of the given index
	 * @throws IOException 
	 */
	private int getAverageDocumentLength(IndexReader reader) throws IOException {
		long length=0;
		for (int docId = 0; docId<reader.getDocCount(IndexConstants.CONTENT); docId++) {
			length += getDocumentLenght(reader.getTermVector(docId, IndexConstants.CONTENT));
		}
		return (int)(length/reader.getDocCount(IndexConstants.CONTENT));
	}
	
	
	/** 
	 * extracts a number of constant index statistics and writes them to a file 
	 * since they are comp. intense to extract and they wont change unless the
	 * index gets rebuild we safe a lot of comp. time by doing this 
	 */
	private void writeIndexStatistics(int maxDocId) throws IOException {
		Directory indexDirectory = FSDirectory.open(INDEX_DIR.toPath());
		IndexReader indexReader = DirectoryReader.open(indexDirectory);
		
		// get dictionary to extract statistics
		Map<String, Long> terms = getDictionary(indexReader);
		
		// total number of tokens in index 
		long total = 0;
		for (Long c : terms.values()) {
			total += c;
		}
		
		// number of unique tokens in index
		int unique = terms.keySet().size();
		
		// average document length
		int avgDl = getAverageDocumentLength(indexReader);	
		
		OutputStream stream = new FileOutputStream(new File("./index/index.properties"));
		Properties props = new Properties();
		
		props.setProperty("terms.total", String.valueOf(total));
		props.setProperty("terms.unique", String.valueOf(unique));
		props.setProperty("document.average.length", String.valueOf(avgDl));
		props.setProperty("document.max.id", String.valueOf(maxDocId));
		
		props.store(stream, "IndexProperties - do not change manually!");
		
		stream.close();
		indexReader.close();
	}

	public static void main(String[] args) throws ZipException, IOException, DocumentException {
	    /* sanity checks for corpus and index directory */
		String usage = "java ch.eth.ir.indexserver.index.TIPSTERCorpusIndexer <corpus_directory>";
	    if (args.length == 0) {
	      log.error("Usage: " + usage);
	      System.exit(1);
	    }

	    if (INDEX_DIR.exists()) {
	      log.error("Cannot save index to '" +INDEX_DIR+ "' directory, please delete it first");
	      System.exit(1);
	    }
	    
	    final File docDir = new File(args[0]);
	    if (!docDir.exists() || !docDir.canRead()) {
	    	log.error("Corpus directory '" +docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
	      System.exit(1);
	    }		
		
		TIPSTERCorpusIndexer indexer = new TIPSTERCorpusIndexer();

		if (indexer.writer == null) {
			// has already been logged!
			System.exit(-1);
		}
		
		/* index contents of zip files in corpus_directory */
	    long startTime = System.currentTimeMillis();	
		for (File f : docDir.listFiles()) {
			if (f.getName().endsWith(".zip")) {
				log.debug("process zip file "+f.getName());
				ZipFile zipFile = new ZipFile(f);
				Enumeration<? extends ZipEntry> zipContent = zipFile.entries();
				while (zipContent.hasMoreElements()) {
					indexer.indexZipEntry(zipFile, zipContent.nextElement());
				}
			}
		}
	    long endTime = System.currentTimeMillis();
	    log.info(indexer.writer.numDocs()+" files indexed; time taken: "+(endTime-startTime)+" ms");		
		
		int maxDocId = indexer.writer.numDocs();
		indexer.close();

		log.info("write constant index statistics to properties file...");
		indexer.writeIndexStatistics(maxDocId);
	}
}