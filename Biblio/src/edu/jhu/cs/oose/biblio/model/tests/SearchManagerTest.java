/**
 * 
 */
package edu.jhu.cs.oose.biblio.model.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.jhu.cs.oose.biblio.model.Category;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.SearchManager;
import edu.jhu.cs.oose.biblio.model.SearchResultsListener;
import edu.jhu.cs.oose.biblio.model.SearchTagsListener;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * Test the SearchManger class
 * 
 */
public class SearchManagerTest extends TestCase {
	SearchManager search, search1;
	List<FileMetadata> testFiles = new ArrayList<FileMetadata>();
	List<PDFFileMetadata> searchResults = new ArrayList<PDFFileMetadata>();
	List<Tag> tagResults = new ArrayList<Tag>();

	PDFFileMetadata testFile1, testFile2, testFile3;
	SessionFactory sessionFactory;

	protected void setUp() throws Exception {

		super.setUp();
		String path1 = "testfiles/test4.pdf", path2 = "testfiles/test5.pdf", path3 = "testfiles/test6.pdf";
		this.fileExist(path1);// 2 Occurrences of searchTerm "Ignorance"
		this.fileExist(path2);// 5 Occurrences, should return on the top of the
								// list
		this.fileExist(path3);// 1 Occurrences
		search = new SearchManager(testFiles);
		search1 = new SearchManager();

		sessionFactory = new Configuration().configure().buildSessionFactory();
	}

	public void testSearchCategory(){
		String searchTerm = "Author:Kafka";
		
		Tag tag1= new Tag();
		Tag tag2= new Tag();
		Tag tag3= new Tag();
		Tag tag4= new Tag();
		
		tag1.setName("Stephen Hawking");
		tag2.setName("Isaac Newton");
		tag3.setName("Franz Kafka");
		tag4.setName("Max Planck");
		
				
		Category cat1 = new Category("British Author");
		Category cat2 = new Category("German Author");

		cat1.addTag(tag1);
		cat1.addTag(tag2);
		cat2.addTag(tag3);
		cat2.addTag(tag4);
		
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		
		session.save(tag1);
		session.save(tag2);
		session.save(tag3);
		session.save(tag4);
		
		session.save(cat1);
		session.save(cat2);
		
		session.getTransaction().commit();
		
//		List<Tag> expectedTags = new ArrayList<Tag>();
//		expectedTags.add(tag2);
//		expectedTags.add(tag3);

		
		try
		{
			search1.searchCategory(searchTerm);
			search1.addTagsListener(new SearchTagsListener() {
			@Override
			public void matchedTags(List<Tag> matches) {
				for (Tag t : matches) {
					tagResults.add(t);
				}

			}
		});
			for (Tag t : tagResults){
				System.out.println(t.getName());
			}
			assertTrue(tagResults.contains(tag3));//Always fails here. 
			//Please help me check if there is an error in the algorithm of SearchCategory. -Cain
			
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();//
			fail("Threw unexpected exception");
		}
		
		
	}

	public void testGetContents() {
		fail("Not yet implemented");
	}

	/**
	 * Test if searchText() returns results in the correct order
	 * 
	 */
	public void testSearchText() {
		assertFalse(testFiles == null);
		String searchTerm = "Ignorance";
		List<PDFFileMetadata> expectedResults = new ArrayList<PDFFileMetadata>();
		expectedResults.add(new PDFFileMetadata("testfiles/test5.pdf"));
		expectedResults.add(new PDFFileMetadata("testfiles/test4.pdf"));
		expectedResults.add(new PDFFileMetadata("testfiles/test6.pdf"));

		try {
			search.searchText(searchTerm);
			search.addResultsListener(new SearchResultsListener() {
				@Override
				public void displayResults(List<FileMetadata> results) {
					for (FileMetadata f : results) {
						searchResults.add((PDFFileMetadata) f);
					}
				}
			}); 
			for (FileMetadata f : searchResults) {
				System.out.println(f.getPathToFile());
			}
			for (int i = 0; i < searchResults.size(); i++) {
				assertTrue(searchResults.get(i).equals(expectedResults.get(i)));

			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();//
			fail("Threw unexpected exception");
		}
	}

	/**
	 * Tests on searching for tags by text
	 */
	public void testSearchTags() {
		SearchManager searcher = new SearchManager();

		tagsSamePrefix(searcher);
	}

	private class MySearchTagsListener implements SearchTagsListener {
		String searchTerm;
		int expectedNumResults;

		public MySearchTagsListener(String term, int numResults) {
			searchTerm = term;
			expectedNumResults = numResults;
		}

		@Override
		public void matchedTags(List<Tag> matches) {
			assertEquals(matches.size(), expectedNumResults);
			for (Tag t : matches) {
				assertSame(true, t.getName().contains(searchTerm));
			}
		}
	}

	/**
	 * adds some tags with the same prefix and searches for that prefix expected
	 * result is that all of the tags will appear in the search result
	 */
	private void tagsSamePrefix(SearchManager searcher) {
		String searchTerm = "abc";
		MySearchTagsListener myListener = new MySearchTagsListener(searchTerm,
				4);
		searcher.addTagsListener(myListener);
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag("abc"));
		tags.add(new Tag("abcd"));
		tags.add(new Tag("abcdeefg"));
		tags.add(new Tag("xyzabc"));
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		for (Tag t : tags) {
			session.save(t);
		}
		session.getTransaction().commit();
		searcher.searchTags(searchTerm);
		searcher.removeTagsListener(myListener);
	}

	public void fileExist(String path) {
		File f = new File(path);
		if (f.exists()) {
			testFiles.add(new PDFFileMetadata(path));
		}
	}

//	public class TestListener implements SearchResultsListener {
//		@Override
//		public void displayResults(List<FileMetadata> results) {
//			for (FileMetadata f : results) {
//				searchResults.add((PDFFileMetadata) f);
//			}
//		}
//	}
//
//	public class TestTagListener implements SearchTagsListener {
//		@Override
//		public void matchedTags(List<Tag> matches) {
//			for (Tag t : matches) {
//				tagResults.add(t);
//			}
//
//		}
//	}

}
