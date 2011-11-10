package edu.jhu.cs.oose.biblio.model.tests;

import junit.framework.TestCase;
import edu.jhu.cs.oose.biblio.model.Bookmark;
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
	 * Bookmarks to use during testing
	 */
	private Bookmark bkmk1;
	
	/**
	 * File data to use during testing.
	 */
	private FileMetadata file;
	
	/**
	 * Location in file, to test the bookmark
	 */
	Location loc;
	
	/**
	 * Tests the mark method of Bookmark, I think (Paul).
	 * See my comments on that method.
	 */
	/*public void testMark() {
		
		DBManager manager = new DBManager();
		assertTrue("Bad input", bkmk.mark(file, loc));//test how to handle bad input
		manager.store(bkmk);

		
		// see DBManager.get(query) for why this has a warning
		Collection<Bookmark> l = (Collection<Bookmark>)manager.get("from BOOKMARK");
		
		// grab an element from the collection
		Bookmark b = l.iterator().next();
		
		// test if the data stored in database is consistent with the object data
		assertSame("Inconsistancy", b.getFile(), file);
		// TODO Paul: I have no idea what the line above does...
		
	}*/
	
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
