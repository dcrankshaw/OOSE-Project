package edu.jhu.cs.oose.biblio.model.tests;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;
import junit.framework.TestCase;

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
		
		assertTrue("Bad input", tag.tagBookmark(bkmk));
	}
	
	public void testGetBookmarks() {
		fail("not yet implemented");
		//tag.getBookmarks();
	}
	
	public void testTagFile() {
		
		assertTrue("Bad input", tag.tagFile(file));
	}
	
	public void testGetFiles() {
		fail("not yet implemented");
		//tag.getFiles();
	}

	
	
	
	
	
	
	
	
	

}
