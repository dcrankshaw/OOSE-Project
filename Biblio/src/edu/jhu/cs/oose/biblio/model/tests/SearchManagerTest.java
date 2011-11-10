/**
 * 
 */
package edu.jhu.cs.oose.biblio.model.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.jhu.cs.oose.biblio.model.SearchManager;
import edu.jhu.cs.oose.biblio.model.SearchTagsListener;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * 
 *
 */
public class SearchManagerTest extends TestCase {
	
	PDFFileMetadata testFile1,testFile2,testFile3;
	SessionFactory sessionFactory;
	
	protected void setUp() throws Exception {
		
		super.setUp();
		String path1 = "testfiles/test1.pdf", path2 = "testfiles/test2.pdf", path3 = "testfiles/test3.pdf";
		File f1 = new File(path1), f2 = new File(path2), f3 = new File(path3);
		if(f1.exists())
		{
			testFile1 = new PDFFileMetadata(new Date(), 0, path1, new TreeSet<Tag>());
		}
		else
		{
			testFile1 = null;
		}
		if(f2.exists())
		{
			testFile2 = new PDFFileMetadata(new Date(), 0, path2, new TreeSet<Tag>());
		}
		else
		{
			testFile2 = null;
		}
		if(f3.exists())
		{
			testFile2 = new PDFFileMetadata(new Date(), 0, path3, new TreeSet<Tag>());
		}
		else
		{
			testFile2 = null;
		}//TODO maybe abstract to a class -Cain
		
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}

	public void testGetContents() {
		fail("Not yet implemented");
	}

	public void testSearchText() {
		String search1 = "Gildas"; //String occurs once in document
		String search0 = "kjsdhfkjdsh"; //String occurs 0 times
		String searchMany = "Maxwell";
		assertFalse(testFile1 == null);
		try
		{
			int results1 = testFile1.searchText(search1);
			assertEquals(1, results1);
			assertEquals(0, testFile1.searchText(search0));
			int resultsMany = testFile1.searchText(searchMany);
			assertTrue(resultsMany > 10);
			System.out.println(resultsMany);
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();//
			fail("Threw unexpected exception");
		}
	}
	
	public void testSearchTags() {
		SearchManager searcher = new SearchManager();
		
		tagsSamePrefix(searcher);
		
		
		
	}
	
	private class MySearchTagsListener implements SearchTagsListener
	{
		String searchTerm;
		int expectedNumResults;
		public MySearchTagsListener(String term, int numResults)
		{
			searchTerm = term;
			expectedNumResults = numResults;
		}
		
		@Override
		public void matchedTags(List<Tag> matches) {
			assertEquals(matches.size(), expectedNumResults);
			for(Tag t: matches)
			{
				assertSame(true, t.getName().contains(searchTerm));
			}
		}
	}
	
	/**
	 * adds some tags with the same prefix and searches for that prefix
	 */
	private void tagsSamePrefix(SearchManager searcher)
	{
		String searchTerm = "abc";
		MySearchTagsListener myListener = new MySearchTagsListener(searchTerm, 4);
		searcher.addTagsListener(myListener);
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag("abc"));
		tags.add(new Tag("abcd"));
		tags.add(new Tag("abcdeefg"));
		tags.add(new Tag("xyzabc"));
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		for(Tag t: tags)
		{
			session.save(t);
		}
		session.getTransaction().commit();
		searcher.searchTags(searchTerm);
		searcher.removeTagsListener(myListener);
	}
	
}


