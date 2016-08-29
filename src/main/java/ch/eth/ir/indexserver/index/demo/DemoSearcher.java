package ch.eth.ir.indexserver.index.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import ch.eth.ir.indexserver.index.IndexConstants;

public class DemoSearcher {
	private static Logger log = Logger.getLogger(DemoSearcher.class);

	private IndexReader indexReader = null;
	private IndexSearcher indexSearcher = null;
	private Analyzer analyzer = null;

	public DemoSearcher() throws IOException {
		File indexDirectory = new File(IndexConstants.INDEX_DIR);
		if (!indexDirectory.exists()) {
			log.fatal("Index directory '" + IndexConstants.INDEX_DIR + "' does not exist!");
			throw new FileNotFoundException("Index directory '" + IndexConstants.INDEX_DIR + "' could not be found.");
		}

		Directory index = FSDirectory.open(indexDirectory.toPath());

		indexReader = DirectoryReader.open(index);
		indexSearcher = new IndexSearcher(indexReader);
		analyzer = new StandardAnalyzer();
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

	public List<Integer> query(List<String> terms, int minimumTermsShouldMatch) throws IOException {
		log.info("called buildQuery(" + terms.toString() + ", " + minimumTermsShouldMatch + ")");
		if (terms.size() < minimumTermsShouldMatch) {
			log.warn("query will always be empty term.length < minimumTermsShouldMatch");
			return null;
		}
		// create query and search
		Query query = buildQuery(terms, minimumTermsShouldMatch);
		TopDocs result = indexSearcher.search(query, indexReader.maxDoc());
		// no results
		if (result.totalHits == 0) {
			return null;
		}
		// extract document id's
		ArrayList<Integer> results = new ArrayList<Integer>(result.totalHits);
		for (int i = 0; i < result.totalHits; i++) {
			results.add(result.scoreDocs[i].doc);
		}
		// shuffle the results to hide the ranking algorithm
		Collections.shuffle(results);
		return results;
	}

	public List<String> processQuery(String query) throws IOException {
		log.info("called processQuery(" + query + ")");
		List<String> terms = new ArrayList<String>();
		// create stream and add char term attribute
		TokenStream stream = analyzer.tokenStream(IndexConstants.CONTENT, query);
		CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
		stream.reset();
		while (stream.incrementToken()) {
			terms.add(cattr.toString());
		}
		stream.end();
		stream.close();
		return terms;
	}

	public static void main(String[] args) throws IOException, ParseException {
		DemoSearcher searcher = new DemoSearcher();
		List<String> terms = searcher.processQuery("Alternative/renewable Energy Plant & Equipment Installation");
		System.out.println("query: " + terms);
		List<Integer> result = searcher.query(terms, 6);
		System.out.println("hits: " + result.size());
	}
}
