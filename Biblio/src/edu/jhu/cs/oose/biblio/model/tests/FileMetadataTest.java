package edu.jhu.cs.oose.biblio.model.tests;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;
import junit.framework.TestCase;

public class FileMetadataTest extends TestCase {
	
	FileMetadata file, file1;
	Tag tag, tag1;
	Bookmark bkmk;

	public void testGetContents() {
		fail("not yet implemented");
	}
	
	public void testUpdateLastOpened()throws ParseException{
		fail("not yet implemented");
	    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
	    Date d1 = df.parse(file.lastOpened);
	    file.updateLastOpened();
	    assertTrue ("Updated LastOpened date is incorrect.", file.lastOpened.after(d1));

	}
	
	public void testGetLastOpened(){
		fail("not yet implemented");
	}
	
	public void testUpdateOpenCount(){
		fail("not yet implemented");
	}
	
	public void testGetOpenCount(){
		fail("not yet implemented");
	}
	
	public void testSetPathToFile(){
		fail("not yet implemented");
	}
	
	public void testGetPathToFile(){
		fail("not yet implemented");
		
	}
	
	public void testAddTags(){
		assertTrue("Bad input", tag.addChildren(tag1));
		
	}
	
	public void testGetTags(){
		fail("not yet implemented");
		
	}
}
