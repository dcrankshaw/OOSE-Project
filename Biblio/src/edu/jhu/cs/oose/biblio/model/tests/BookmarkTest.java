package edu.jhu.cs.oose.biblio.model.tests;

import junit.framework.TestCase;

import org.hibernate.Session;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Location;
import edu.jhu.cs.oose.biblio.model.Tag;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * JUnit test class for making sure that the Bookmark class works
 * @author Cain Lu
 */
public class BookmarkTest {
	
	/**
	 * Bookmarks to use during testing
	 */
	private Bookmark bkmk;
	
	/**
	 * File data to use during testing.
	 */
	private FileMetadata file,file1;
	
	/**
	 * Location in file, to test the bookmark.
	 */
	Location loc, loc1;
	
	Tag tag, tag1;
	
	@Before
	public void setUp() {
	
		Session session = Database.getNewSession();
		session.beginTransaction();
		try{
			file = new TestMetadata("afile");
			loc = new Location(5);
			tag = new Tag("Tag1");
			tag = new Tag("Tag2");
		} catch (Exception e) {
			e.printStackTrace();
		}			
		bkmk = new Bookmark(file, loc);
		Database.commit();
		
	}
	


	@Test
	public void testSetGetId() {
		bkmk.setId(1);
		assertTrue(bkmk.getId()==1);
	}



	@Test
	public void testSetFile() {
		bkmk.setFile(file1);
		assertEquals(bkmk.getFile(), file1);
	}


	@Test
	public void testSetLocation() {
		bkmk.setLocation(loc1);
		assertEquals(bkmk.getLocation(), loc1);
	}

	@Test
	public void testAddTag() {
		bkmk.addTag(tag);
		assertTrue(bkmk.getTags().contains(tag));
	}

	@Test
	public void testGetTags() {
		bkmk.addTag(tag);
		bkmk.addTag(tag1);
		assertTrue(bkmk.getTags().contains(tag) && bkmk.getTags().contains(tag1));
	}

	/**
	 *  Test if the bookmarked file exists.
	 */
	@Test
	public void testGetFile() {
		assertNotNull("File does not exist", bkmk.getFile());
	}
	
	/**
	 *  Test if the bookmarked location exists.
	 */
	@Test
	public void testGetLocation() {
		assertNotNull("File does not exist", bkmk.getLocation());
	}

}
