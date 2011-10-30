package edu.jhu.cs.oose.biblio.model.tests;


import java.util.Date;

import junit.framework.TestCase;
import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

public class FileMetadataTest extends TestCase {
	
	FileMetadata file, file1;
	Tag tag, tag1;
	Bookmark bkmk;
	
	public void testGetContents() {
		assertTrue("Get contents does not return any contents.", file.getContents() != null ); 
		//fail("not yet implemented");
	}
	
	public void testUpdateLastOpened() {
		//fail("not yet implemented");

		Date d1 = file.getLastOpened();
		file.setLastOpened(new Date());
		assertTrue ("Updated LastOpened date is after now.", file.getLastOpened().after(d1));
	}
	
	public void testGetLastOpened(){
		//fail("not yet implemented");
	}
	
	public void testUpdateOpenCount(){
		//fail("not yet implemented");
	}
	
	public void testGetOpenCount(){
		//fail("not yet implemented");
	}
	
	public void testSetPathToFile(){
		//fail("not yet implemented");
	}
	
	public void testGetPathToFile(){
		//fail("not yet implemented");
		
	}
	
	public void testAddTags(){
		assertTrue("Bad input", tag.addChildren(tag1));
		
	}
	
	public void testGetTags(){
		//fail("not yet implemented");
		
	}
}
