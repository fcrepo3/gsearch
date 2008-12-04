package dk.defxws.fedoragsearch.test;

import java.io.IOException;
import java.text.Collator;
import java.util.Locale;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.ScoreDocComparator;
import org.apache.lucene.search.SortComparatorSource;
import org.apache.lucene.search.SortField;

// this test class has two variants for the demo sake
// 1. inspired by EscidocSearchResultComparator.java
// 2. inspired by Lucene in Action section 6.1

public class ComparatorSourceTest implements SortComparatorSource {

	private String param1;
	private String param2;

	public ComparatorSourceTest() {
		// TODO Auto-generated constructor stub
	}

	public ComparatorSourceTest(String param1, String param2) {
		this.param1 = param1;
		this.param2 = param2;
	}

	public ScoreDocComparator newComparator(IndexReader reader, String fieldName)
			throws IOException {
		if (param1 == null)
			return new ScoreDocComparatorTest(reader, fieldName);
		return new ScoreDocComparatorTest(reader, fieldName, param1, param2);
	}
	
	private static class ScoreDocComparatorTest
		implements ScoreDocComparator {

		private IndexReader reader;
		private String fieldName;
		private int param1 = -1;
		private int param2 = -1;
		
		public ScoreDocComparatorTest(IndexReader reader, String fieldName) {
			// inspired by EscidocSearchResultComparator.java
			this.reader = reader;
			this.fieldName = fieldName;
		}
		
		public ScoreDocComparatorTest(IndexReader reader, String fieldName, String param1, String param2) {
			// inspired by Lucene in Action section 6.1
			this.reader = reader;
			this.fieldName = fieldName;
			this.param1 = Integer.parseInt(param1);
			this.param2 = Integer.parseInt(param2);
		}
		
		public int compare(ScoreDoc i, ScoreDoc j) {
			String fieldValue1;
			String fieldValue2;
			try {
				fieldValue1 = reader.document(i.doc).get(fieldName);
				fieldValue2 = reader.document(j.doc).get(fieldName);
			} catch (CorruptIndexException e1) {
				return 0;
			} catch (IOException e1) {
				return 0;
			}
			if (param1 < 0 && param2 < 0) {
				// just for testing, compare field value as is
					return Collator.getInstance(Locale.getDefault()).compare(fieldValue1, fieldValue2);
			}
			// just for testing, "orientation" of doc pair wrt. param pair
			int deltax = fieldValue1.length() - param1;
			int deltay = fieldValue2.length() - param2;
			if (deltax>deltay) return 1;
			if (deltax<deltay) return -1;
			return 0;
		}
		
		public Comparable sortValue(ScoreDoc i) {
			return new Integer(i.doc);
		}
		
		public int sortType() {
			return SortField.FLOAT;
		}
	}
			

}
