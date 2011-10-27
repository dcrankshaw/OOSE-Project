package edu.jhu.cs.oose.biblio.model.tests;

import java.util.Collection;

import junit.framework.TestCase;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.DBManager;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Location;

public class BookmarkTest extends TestCase {
	Bookmark bkmk, bkmk1;
	FileMetadata file;
	Location loc;
	

	public void testMark() {
		
		DBManager manager = new DBManager();
		assertTrue("Bad input", bkmk.mark(file, loc));//test how to handle bad input
		manager.store(bkmk);

		
		// see DBManager.get(query) for why this has a warning
		Collection<Bookmark> l = (Collection<Bookmark>)manager.get("from BOOKMARK");
		
		// grab an element from the collection
		Bookmark b = l.iterator().next();
		
		// test if the data stored in database is consistent with the object data
		assertSame("Inconsistancy", b.getFile(), file);
		// Paul: I have no idea what the line above does...
		
		// test whether allow to bookmark the same destination twice.
		bkmk1.mark(file, loc);
		assertSame("Duplicate bookmark", bkmk, bkmk1);
	}
	
	public void testGetFile() {
		// test if the bookmarked file exists.
		assertNotNull("File does not exist", bkmk.getFile());
	}
	
	public void testGetLocation() {
		// test if the bookmarked location exists.
		assertNotNull("File does not exist", bkmk.getLocation());
	}
	
	
	public void testGetTags() {
		// test if data is consistent in the database.
		assertSame("Inconsistancy", bkmk.tags(),bkmk.tags);		
	}
	

	
}
