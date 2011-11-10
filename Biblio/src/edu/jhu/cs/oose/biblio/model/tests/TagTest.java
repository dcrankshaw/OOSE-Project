package edu.jhu.cs.oose.biblio.model.tests;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;
import junit.framework.TestCase;

// TODO This class needs to check the results of the methods
public class TagTest extends TestCase {
	
	Tag tag, tag1;
	Bookmark bkmk;
	FileMetadata file;
	
	public void testSetName() {
		tag.setName("tempTag");
		tag1.setName("tempTag");
		assertSame("Duplicate Tag", tag, tag1);//test whether allow different tags with the same name.(duplicates)		
	}
	
	public void testGetName() {
		fail("not yet implemented");
		tag.getName();
	}
	
	public void testAddChildren() {
		
		assertTrue("Bad input", tag.addChildren(tag1));
	}
	
	public void testGetChildren() {
		fail("not yet implemented");
		//tag.getChildren();
	}
	
	public void testTagBookmarks() {
		
	}
	
	public void testGetBookmarks() {
		fail("not yet implemented");
		//tag.getBookmarks();
	}
	
	
	
	public void testGetFiles() {
		fail("not yet implemented");
		//tag.getFiles();
	}

	
}
