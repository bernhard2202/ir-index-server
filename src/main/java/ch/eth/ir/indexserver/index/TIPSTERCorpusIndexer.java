package ch.eth.ir.indexserver.index;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
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
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * Index writer for the TIPSTER corpus
 * Given the root directory all TIPSTER-zip-files are read and 
 * their contents added to a lucene index. 
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

	/* index the entry of a zip file */
	private void indexZipEntry(ZipFile zipFile, ZipEntry zipEntry) throws IOException {
		log.info("Indexing file: " + zipEntry.getName());
		Document document = new Document();
		
		// extract the file content
		InputStream contentStream = zipFile.getInputStream(zipEntry);
		String fileContent = IOUtils.toString(contentStream, StandardCharsets.UTF_8);
		IOUtils.closeQuietly(contentStream);
		
		// index file contents
		//TODO: extract real content, remove meta information
		Field contentField = new Field(IndexConstants.CONTENT, fileContent, contentFieldType);
		// index file name
		Field fileNameField = new StringField(IndexConstants.TITLE, zipEntry.getName(), Store.YES);

		document.add(contentField);
		document.add(fileNameField);
		writer.addDocument(document);
	}

	public static void main(String[] args) throws ZipException, IOException {
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
		indexer.close();
	}

}