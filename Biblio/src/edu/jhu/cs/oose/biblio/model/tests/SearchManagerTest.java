
package edu.jhu.cs.oose.biblio.model.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;

import edu.jhu.cs.oose.biblio.model.Category;
import edu.jhu.cs.oose.biblio.model.Database;
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

	SearchManager searcher;
	List<FileMetadata> originalFiles;
	List<FileMetadata> searchResults;
	Set<Tag> tagSearchResults;
	List<Tag> categorySearchResults;
	
	@Before
	protected void setUp()
	{	Database.getNewSession();

		originalFiles = new ArrayList<FileMetadata>();
		searchResults = new ArrayList<FileMetadata>();
		tagSearchResults = new TreeSet<Tag>();
		categorySearchResults = new ArrayList<Tag>();
		fileExist("testfiles/test1.pdf");
		fileExist("testfiles/test2.pdf");
		fileExist("testfiles/test3.pdf");
		fileExist("testfiles/test4.pdf");
		fileExist("testfiles/test5.pdf");
		fileExist("testfiles/test6.pdf");
		
	}

	PDFFileMetadata testFile1, testFile2, testFile3;
	/**
	 * setup helper function to add testfiles to the database
	 * 
	 * @param path
	 */
	public void fileExist(String path) {
		File f = new File(path);
		if (f.exists()) {
			originalFiles.add(new PDFFileMetadata(path));
		} else {
			System.out.println("Error: " + path);
		}
	}

	public class TestListener implements SearchResultsListener {
		@Override
		public void displayFileResults(List<FileMetadata> results) {
			searchResults.clear();
			for (FileMetadata f : results) {
				searchResults.add((PDFFileMetadata) f);
			}
		}
	}

	/**
	 * Test if searchText() returns results in the correct order
	 */
	@Test
	public void testSearchText() {
		List<FileMetadata> testFiles = new ArrayList<FileMetadata>();
		testFiles.add(new PDFFileMetadata("testfiles/test5.pdf"));// 5 Occurrences, should return on the top of the list
		testFiles.add(new PDFFileMetadata("testfiles/test4.pdf"));// 2 Occurrences of searchTerm "Ignorance"
		testFiles.add(new PDFFileMetadata("testfiles/test6.pdf"));// 1 Occurrences
		
		searcher = new SearchManager(originalFiles);
		assertFalse(originalFiles == null);	
		String searchTerm = "Ignorance";

		List<PDFFileMetadata> expectedResults = new ArrayList<PDFFileMetadata>();
		expectedResults.add(new PDFFileMetadata("testfiles/test5.pdf"));
		expectedResults.add(new PDFFileMetadata("testfiles/test4.pdf"));
		expectedResults.add(new PDFFileMetadata("testfiles/test6.pdf"));

		try {

			searcher.addResultsListener(new TestListener());
			searcher.searchText(searchTerm);
			//System.out.println("\n\n\n\n\n\n\nSearch text");

			for (FileMetadata f : searchResults) {

				System.out.println(f.getPathToFile());
			}
			for (int i = 0; i < searchResults.size(); i++) {
				assertTrue(searchResults.get(i).equals(expectedResults.get(i)));

			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			fail("Threw unexpected exception");
		}
		Database.commit();
	}

	/**
	 * Tests searching for tags based on their name
	 */
	@Test
	public void testSearchTags() {
		try {
		testSearchCategory();
		tagsSamePrefix();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		Database.commit();
	}

	/**
	 * searches for tags matching a search term belonging to a given category.
	 * searchterm is of the form "category: term"
	 */
	@Test
	public void testSearchCategory() throws Exception{
		
		searcher = new SearchManager();
		//categorySearchResults = new ArrayList<Tag>();
		searcher.addTagsListener(new SearchTagsListener() {

			@Override
			public void matchedTags(List<Tag> matches) {
				setSearchCategoryResults(matches);

			}

		});
		Category matches = new Category("matches");
		Category noMatch = new Category("hello");

		Tag aa = new Tag("aa");
		Tag aabb = new Tag("aabb");
		Tag noA = new Tag("hhhh");

		Tag otherCatAA = new Tag("aa Other Cat");
		Tag otherCatAABB = new Tag("aabb Other Cat");

		matches.addTag(aa);
		matches.addTag(aabb);
		matches.addTag(noA);

		noMatch.addTag(otherCatAABB);
		noMatch.addTag(otherCatAA);


		Database.commit();
		boolean found1 = false;
		boolean found2 = false;
		//searcher.searchTags("matches: aa");
		searcher.searchTags("aa");
		System.out.println("-----------------------------------");
		for (Tag t : categorySearchResults) {
			System.out.println(t.getName());
			if(t.getName().compareToIgnoreCase(aa.getName()) == 0)
				found1 = true;
			if(t.getName().compareToIgnoreCase(aabb.getName()) == 0)
				found2 = true;
			
		}
		System.out.println("Actual\n\n");
		System.out.println(aa.getName());
		System.out.println(aabb.getName());
		System.out.println("\n\n");
		
		
		assertTrue(found1 && found2);
		
		assertTrue(categorySearchResults.contains(aa));
		assertTrue(categorySearchResults.contains(aabb));
		assertFalse(categorySearchResults.contains(noA));
		
		
		/*
		assertFalse(tagSearchResults == null);
		assertTrue(tagSearchResults.contains(aa));
		assertTrue(tagSearchResults.contains(aabb));
		assertFalse(tagSearchResults.contains(noA));
		assertFalse(tagSearchResults.contains(otherCatAA));
		assertFalse(tagSearchResults.contains(otherCatAABB));

		searcher.searchTags("Matches: aA");
		for (Tag t : tagSearchResults) {
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
*/
	}

	/**
	 * helper method to set the results of a tag search
	 * 
	 * @param matches
	 *            the results of the search (given to a listener)
	 */
	private void setSearchCategoryResults(List<Tag> matches) {
		categorySearchResults.clear();
		if (matches != null) {
			System.out.println("Matches in listener:");
			for(Tag t: matches)
			{
				System.out.println(t.getName());
			}
			categorySearchResults.addAll(matches);
		}
	}

	/**
	 * adds some tags with the same prefix and searches for that prefix expected
	 * result is that all of the tags will appear in the search result
	 */
	private void tagsSamePrefix() {

		SearchManager searcher = new SearchManager();
		String searchTerm = "abc";
		TagsSamePrefixSearchTagsListener myListener = new TagsSamePrefixSearchTagsListener(
				searchTerm, 4);

		searcher.addTagsListener(myListener);
		List<Tag> tags = new ArrayList<Tag>();
		Database.getNewSession().beginTransaction();
		try {
			tags.add(new Tag("abc"));

			tags.add(new Tag("abcd"));
			tags.add(new Tag("abcdeefg"));
			tags.add(new Tag("xyzabc"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		Database.commit();
		searcher.searchTags(searchTerm);
	}

	private class TagsSamePrefixSearchTagsListener implements
			SearchTagsListener {
		String searchTerm;
		int expectedNumResults;

		public TagsSamePrefixSearchTagsListener(String term, int numResults) {
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
	 * Test the filterByTags function
	 */
	@Test
	public void testFilterByTags() throws Exception {
		searcher = new SearchManager(originalFiles);

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
		school.addChild(math);
		school.addChild(english);
		math.addChild(numbers);
		numbers.addChild(decimal);
		numbers.addChild(integer);
		integer.addChild(one);
		integer.addChild(two);
		decimal.addChild(onePointFive);
		decimal.addChild(twoPointFive);
		english.addChild(letters);
		letters.addChild(a);
		letters.addChild(b);
		letters.addChild(c);

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
		Database.commit();
		searcher.addResultsListener(new SearchResultsListener() {

			@Override
			public void displayFileResults(List<FileMetadata> results) {
				updateSearchResults(results);
			}

		});
		searcher.filterByTags(filterBy);
		System.out.println("Search Results");
		for (FileMetadata t : searchResults) {
			System.out.println(t.getName());
		}
		System.out.println("\nOriginal Files");
		for (FileMetadata t : originalFiles) {
			System.out.println(t.getName());
		}

		assertTrue(searchResults.contains(originalFiles.get(0)));
		assertTrue(searchResults.contains(originalFiles.get(1)));
		assertFalse(searchResults.contains(originalFiles.get(2)));
		assertFalse(searchResults.contains(originalFiles.get(3)));

		// TODO add more searching testcases here

	}
	

	/**
	 * helper method to update the searchResults after a
	 * 
	 * @param newResults
	 */
	private void updateSearchResults(List<FileMetadata> newResults) {
		searchResults = newResults;
	}

}