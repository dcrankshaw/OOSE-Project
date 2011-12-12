/**
 * 
 */
package edu.jhu.cs.oose.biblio.model.tests;

import static org.junit.Assert.*;

import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * JUnit tests on FileMetadata works.
 * @author Cain Lu
 */
public class FileMetadataTest {
	/**
	 * The FileMetadata object to test
	 */
	FileMetadata file, file1, file2;
	/**
	 * Sample tags to apply to the file.
	 */
	Tag tag,tag1;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() {
		file = new TestMetadata();
		file1 = new TestMetadata();
		file2 = new TestMetadata();
		Session session = Database.getNewSession();
		session.beginTransaction();
		try {
			tag = new Tag("Tag");			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			tag1 = new Tag("Tag1");					
		} catch (Exception e) {
			e.printStackTrace();
		}
		Database.commit();
		
		
	}


	/**
	 * Test method for {@link edu.jhu.cs.oose.biblio.model.FileMetadata#setId(int)} and {@link edu.jhu.cs.oose.biblio.model.FileMetadata#getId(int)}.
	 */
	@Test
	public void testSetGetId() {
		file.setId(1);	
		assertTrue(file.getId()==1);
	}

	/**
	 * Test method for {@link edu.jhu.cs.oose.biblio.model.FileMetadata#equals(edu.jhu.cs.oose.biblio.model.FileMetadata)}.
	 */
	@Test
	public void testEqualsFileMetadata() {
		file.setId(1);
		file.addTag(tag);
		file.setPathToFile("testFiles/test1.pdf");	
		file1.setId(2);
		file1.addTag(tag1);
		file1.setPathToFile("testFiles/test2.pdf");		
		file2.setId(1);
		file2.addTag(tag);
		file2.setPathToFile("testFiles/test1.pdf");	
		assertTrue(file.equals(file2));
		assertFalse(file.equals(file1));
	}

	/**
	 * Test method for {@link edu.jhu.cs.oose.biblio.model.FileMetadata#getTags()}.
	 */
	@Test
	public void testGetTags() {
		file.addTag(tag);
		file.addTag(tag1);
		assertTrue(file.getTags().contains(tag)&& file.getTags().contains(tag1));		
	}

	/**
	 * Test method for {@link edu.jhu.cs.oose.biblio.model.FileMetadata#addTag(edu.jhu.cs.oose.biblio.model.Tag)}.
	 */
	@Test
	public void testAddTag() {
		file.addTag(tag);
		assertTrue(file.getTags().contains(tag));
	}

	/**
	 * Test method for {@link edu.jhu.cs.oose.biblio.model.FileMetadata#SetPathToFile()} and {@link edu.jhu.cs.oose.biblio.model.FileMetadata#getPathToFile()}.
	 */
	@Test
	public void testSetGetPathToFile() {
		file.setPathToFile("testFiles/test1.pdf");
		assertEquals(file.getPathToFile(), "testFiles/test1.pdf");
		
	}

	/**
	 * Test method for {@link edu.jhu.cs.oose.biblio.model.FileMetadata#getName()}.
	 */
	@Test
	public void testGetName() {
		file.setPathToFile("testFiles/test1.pdf");
		assertEquals(file.getName(), "testFiles/test1.pdf");
	}


}
