package edu.jhu.cs.oose.biblio.model.tests;


import java.util.Date;

import junit.framework.TestCase;

import org.hibernate.Session;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileContents;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * JUnit test class to make sure that FileMetadata works.
 * @author Cain Lu
 */
public class FileMetadataTest extends TestCase {
	
	/**
	 * An empty implementation of FileMetadata so that we
	 * can actually test something.
	 */
	private class TestMetadata extends FileMetadata {
		@Override
		public int searchText(String searchTerm) throws Exception {
			return 0;
		}

		@Override
		public FileContents getContents() {
			return null;
		}
	}
	
	/**
	 * The FileMetadata object to test
	 */
	FileMetadata file;
	/**
	 * Sample tags to apply to the file.
	 */
	Tag tag;
	/**
	 * Sample tags to apply to the file.
	 */
	Tag tag1;
	
	public void setUp() {
		file = new TestMetadata();
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		tag = new Tag("Tag");
		tag1 = new Tag("Tag1");
		session.getTransaction().commit();
	}
	
	/**
	 * Tests FileMetdata.getContents().
	 * This is an abstract method on FileMetadata, so this is not a good test. 
	 */
	/*public void testGetContents() {
		assertTrue("Get contents does not return any contents.", file.getContents() != null ); 
	}*/
	
	/**
	 * Tests FileMetadata.updateLastOpened().
	 */
	public void testUpdateLastOpened() {
		Date d1 = file.getLastOpened();
		file.setLastOpened(new Date());
		assertTrue ("Updated LastOpened date is after now.", file.getLastOpened().after(d1));
	}
	
	/**
	 * Tests FileMetadata.getLastOpened().
	 */
	// TODO implement this test
	public void testGetLastOpened(){
		//fail("not yet implemented");
	}
	
	/**
	 * Tests FileMetadata.updateOpenCount().
	 */
	// TODO implement this test
	public void testUpdateOpenCount(){
		//fail("not yet implemented");
	}
	
	/**
	 * Tests FileMetadata.getOpenCount().
	 */
	// TODO implement this test
	public void testGetOpenCount(){
		//fail("not yet implemented");
	}
	
	/**
	 * Tests FileMetadata.setPathToFile().
	 */
	// TODO implement this test
	public void testSetPathToFile(){
		//fail("not yet implemented");
	}
	
	/**
	 * Tests FileMetadata.getPathToFile().
	 */
	// TODO implement this test
	public void testGetPathToFile(){
		//fail("not yet implemented");
	}
	
	/**
	 * Tests FileMetadata.getLastOpened().
	 */
	// TODO Paul:I don't think this test works...
	public void testAddTags(){
		assertTrue("Bad input", tag.addChildren(tag1));
	}
	
	/**
	 * Test FileMetadata.getTags()
	 */
	// TODO implement this test
	public void testGetTags(){
		//fail("not yet implemented");
		
	}
}
