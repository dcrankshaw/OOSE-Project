package edu.jhu.cs.oose.biblio.model.tests;

import junit.framework.TestCase;

import org.hibernate.Session;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Location;

/**
 * JUnit test class for making sure that the Bookmark class works
 * @author Cain Lu
 */
public class BookmarkTest extends TestCase {
	/**
	 * Bookmarks to use during testing
	 */
	private Bookmark bkmk;
	
	/**
	 * File data to use during testing.
	 */
	private FileMetadata file;
	
	/**
	 * Location in file, to test the bookmark
	 */
	Location loc;
	
	public void setUp() {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		
		file = new TestMetadata();
		loc = new Location(5);
		session.save(loc);
		session.save(file);
		
		bkmk = new Bookmark(file, loc);
		session.getTransaction().commit();
	}
	
	/**
	 *  Test if the bookmarked file exists.
	 */
	public void testGetFile() {
		assertNotNull("File does not exist", bkmk.getFile());
	}
	
	/**
	 *  Test if the bookmarked location exists.
	 */
	public void testGetLocation() {
		assertNotNull("File does not exist", bkmk.getLocation());
	}
		
}
