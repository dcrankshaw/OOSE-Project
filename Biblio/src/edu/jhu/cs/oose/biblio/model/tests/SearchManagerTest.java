/**
 * 
 */
package edu.jhu.cs.oose.biblio.model.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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

	//SearchManager search;
	List<FileMetadata> testFiles;
	List<FileMetadata> originalFiles;
	List<FileMetadata> searchResults;
	Set<Tag> tagSearchResults;
	
	

	PDFFileMetadata testFile1,testFile2,testFile3;

	SessionFactory sessionFactory;

	protected void setUp() throws Exception {

		super.setUp();
		originalFiles = new ArrayList<FileMetadata>();
		testFiles = new ArrayList<FileMetadata>();
		searchResults = new ArrayList<FileMetadata>();
		tagSearchResults = new TreeSet<Tag>();
		String path1 = "testfiles/test4.pdf", path2 = "testfiles/test5.pdf", path3 = "testfiles/test6.pdf";
		this.fileExist(path1);//2 Occurrences of searchTerm "Ignorance"
		this.fileExist(path2);//5 Occurrences, should return on the top of the list
		this.fileExist(path3);//1 Occurrences
		fileExist("testfiles/test1.pdf");
		fileExist("testfiles/test2.pdf");
		fileExist("testfiles/test3.pdf");
		for(FileMetadata f: testFiles)
		{
			originalFiles.add(f);
		}
		System.out.println("\n\n\n\n\n\n");
		for(FileMetadata t: originalFiles)
		{
			System.out.println(t.getName());
		}
		System.out.println("\n\n\n\n\n\n");
		
		//search = new SearchManager(testFiles);
		
		
		sessionFactory = new Configuration().configure().buildSessionFactory();
	}
	
	/**
	 * setup helper function to add testfiles to the database
	 * @param path
	 */
	public void fileExist(String path){			
		File f = new File(path);
		if(f.exists())
		{
			testFiles.add(new PDFFileMetadata(path));
		}
		else
		{
			System.out.println("Error: " + path);
		}
	}

	public class TestListener implements SearchResultsListener{
		@Override
		public void displayResults(List<FileMetadata> results){
			searchResults.clear();
			for (FileMetadata f : results){
				searchResults.add((PDFFileMetadata) f);
			}
		}
	}
	
	

	/**
	 * Test if searchText() returns results in the correct order
	 */
	public void testSearchText() {

		assertFalse(originalFiles == null);
		SearchManager search = new SearchManager(originalFiles);
		String searchTerm = "Ignorance"; 

		List<PDFFileMetadata> expectedResults = new ArrayList<PDFFileMetadata>();
		expectedResults.add(new PDFFileMetadata("testfiles/test5.pdf"));
		expectedResults.add(new PDFFileMetadata("testfiles/test4.pdf"));
		expectedResults.add(new PDFFileMetadata("testfiles/test6.pdf"));

		
		try
		{
			
			search.addResultsListener(new TestListener());
			search.searchText(searchTerm);
			System.out.println("\n\n\n\n\n\n\nSearch text");
			
			for (FileMetadata f : searchResults){

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
	 * Test to ensure that the results pair compareTo gives the correct order
	 */
	public void testResultsPairClass() {
		SearchManager searcher = new SearchManager();
		SearchManager.ResultsPair moreFrequent = searcher.new ResultsPair(10, testFile1);
		SearchManager.ResultsPair lessFrequent = searcher.new ResultsPair(6, testFile2);
		assertTrue(moreFrequent.compareTo(lessFrequent) < 0);
		
	}
	

	/**
	 * Tests searching for tags based on their name
	 */
	public void testSearchTags() {
		
		testSearchCategory();
		tagsSamePrefix();
	}
	
	/**
	 * searches for tags matching a search term belonging to a given category.
	 * searchterm is of the form "category: term"
	 */
	public void testSearchCategory()
	{
		SearchManager searcher = new SearchManager();
		searcher.addTagsListener(new SearchTagsListener() {

			@Override
			public void matchedTags(List<Tag> matches) {
				setSearchCategoryResults(matches);
				
			}
			
		});
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		Category matches = new Category();
		matches.setName("matches");
		Category noMatch = new Category();
		noMatch.setName("hello");
		
		Tag aa = new Tag();
		aa.setName("aa");
		Tag aabb = new Tag();
		aabb.setName("aabb");
		Tag noA = new Tag();
		noA.setName("hhhhh");
		
		Tag otherCatAA = new Tag();
		otherCatAA.setName("aa Other Cat");
		Tag otherCatAABB = new Tag();
		otherCatAABB.setName("aabb Other Cat");
		
		matches.addTag(aa);
		matches.addTag(aabb);
		matches.addTag(noA);
		
		noMatch.addTag(otherCatAABB);
		noMatch.addTag(otherCatAA);
		
		session.save(matches);
		session.save(noMatch);
		session.save(aa);
		session.save(aabb);
		session.save(noA);
		session.save(otherCatAA);
		session.save(otherCatAABB);
		session.getTransaction().commit();
		
		searcher.searchTags("matches: aa");
		for(Tag t: tagSearchResults)
		{
			System.out.println(t.getName());
		}
		assertFalse(tagSearchResults == null);
		assertTrue(tagSearchResults.contains(aa));
		assertTrue(tagSearchResults.contains(aabb));
		assertFalse(tagSearchResults.contains(noA));
		assertFalse(tagSearchResults.contains(otherCatAA));
		assertFalse(tagSearchResults.contains(otherCatAABB));
		
		
		
		searcher.searchTags("hello: aa");
		assertFalse(tagSearchResults == null);
		assertFalse(tagSearchResults.contains(aa));
		assertFalse(tagSearchResults.contains(aabb));
		assertFalse(tagSearchResults.contains(noA));
		assertTrue(tagSearchResults.contains(otherCatAA));
		assertTrue(tagSearchResults.contains(otherCatAABB));
		
		searcher.searchTags("Hello:: aa");
		assertTrue(tagSearchResults.size() == 0);
		
		
		
		
		
	}
	
	/**
	 * helper method to set the results of a tag search
	 * @param matches the results of the search (given to a listener)
	 */
	private void setSearchCategoryResults(List<Tag> matches)
	{
		tagSearchResults.clear();
		if(matches != null)
			tagSearchResults.addAll(matches);
	}
	
	
	
	

	/**
	 * adds some tags with the same prefix and searches for that prefix expected
	 * result is that all of the tags will appear in the search result
	 */
	private void tagsSamePrefix()
	{
		
		SearchManager searcher = new SearchManager();
		String searchTerm = "abc";
		TagsSamePrefixSearchTagsListener myListener = new TagsSamePrefixSearchTagsListener(searchTerm, 4);

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
	}

	
	private class TagsSamePrefixSearchTagsListener implements SearchTagsListener
	{
		String searchTerm;
		int expectedNumResults;
		public TagsSamePrefixSearchTagsListener(String term, int numResults)
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
	 * Test the filterByTags function
	 */
	
	public void testFilterByTags() {
		SearchManager search = new SearchManager(originalFiles);
		
		Tag math = new Tag("math");
		Tag numbers = new Tag("numbers");
		Tag one = new Tag("one");
		Tag two = new Tag("two");
		Tag integer = new Tag("integer");
		Tag decimal = new Tag("decimal");
		Tag onePointFive = new Tag("1.5");
		Tag twoPointFive = new Tag("2.5");
		Tag english = new Tag("english");
		Tag letters = new Tag("letters");
		Tag a = new Tag("a");
		Tag b = new Tag("b");
		Tag c = new Tag("c");
		Tag school = new Tag("school");
		school.addChildren(math);
		school.addChildren(english);
		math.addChildren(numbers);
		numbers.addChildren(decimal);
		numbers.addChildren(integer);
		integer.addChildren(one);
		integer.addChildren(two);
		decimal.addChildren(onePointFive);
		decimal.addChildren(twoPointFive);
		english.addChildren(letters);
		letters.addChildren(a);
		letters.addChildren(b);
		letters.addChildren(c);
		
		/*-------------------------------------------------
		 * file0: letters, numbers, integer, onePointFive
		 * file1: letters, numbers, decimal, b
		 * file2: math, english, twoPointFive
		 * file3: school, b, c
		 --------------------------------------------------*/
		
		letters.addTaggedFiles(originalFiles.get(0));
		numbers.addTaggedFiles(originalFiles.get(0));
		integer.addTaggedFiles(originalFiles.get(0));
		onePointFive.addTaggedFiles(originalFiles.get(0));
		
		letters.addTaggedFiles(originalFiles.get(1));
		numbers.addTaggedFiles(originalFiles.get(1));
		decimal.addTaggedFiles(originalFiles.get(1));
		b.addTaggedFiles(originalFiles.get(1));
		
		math.addTaggedFiles(originalFiles.get(2));
		english.addTaggedFiles(originalFiles.get(2));
		twoPointFive.addTaggedFiles(originalFiles.get(2));
		
		school.addTaggedFiles(originalFiles.get(3));
		b.addTaggedFiles(originalFiles.get(3));
		c.addTaggedFiles(originalFiles.get(3));
		
		Set<Tag> filterBy = new HashSet<Tag>();
		filterBy.add(letters);
		filterBy.add(numbers);
		search.addResultsListener(new SearchResultsListener() {

			@Override
			public void displayResults(List<FileMetadata> results) {
				updateSearchResults(results);
			}
			
		});
		search.filterByTags(filterBy);
		System.out.println("Search Results");
		for(FileMetadata t: searchResults)
		{
			System.out.println(t.getName());
		}
		System.out.println("\nOriginal Files");
		for(FileMetadata t: originalFiles)
		{
			System.out.println(t.getName());
		}
		
		assertTrue(searchResults.contains(originalFiles.get(0)));
		assertTrue(searchResults.contains(originalFiles.get(1)));
		assertFalse(searchResults.contains(originalFiles.get(2)));
		assertFalse(searchResults.contains(originalFiles.get(3)));
		
		//TODO add more searching testcases here
		
		
		
	}
	
	/**
	 * helper method to update the searchResults after a 
	 * @param newResults
	 */
	private void updateSearchResults(List<FileMetadata> newResults)
	{
		searchResults = newResults;
	}
	
}

