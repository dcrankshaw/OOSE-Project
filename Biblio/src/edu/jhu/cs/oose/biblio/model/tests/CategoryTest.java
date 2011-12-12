package edu.jhu.cs.oose.biblio.model.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.jhu.cs.oose.biblio.model.Category;
import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.Tag;

public class CategoryTest {
	Category c, c1;
	Tag tag, tag1;
	
	@Before
	public void setUp() throws Exception {
		Database.getNewSession();
		c = new Category("cat");
		c1 = new Category("cat1");
		
	}

	@Test
	public void testSetGetId() {
		c.setId(1);
		assertEquals(c.getId(),1);
			
	}

	@Test
	public void testGetClassName() {
		assertEquals(c.getClassName(), "Category");
	}


	@Test
	public void testSetGetName() {
		c.setName("cat1");
		assertEquals(c.getName(),"cat1");
	}


	@Test
	public void testToString() {
		assertTrue(c.toString().contains("1") && c.toString().contains("cat"));
	}

	@Test
	public void testGetTags() {
		c.addTag(tag);
		c.addTag(tag1);
		assertTrue(c.getTags().contains(tag)&& c.getTags().contains(tag1));
	}

	@Test
	public void testAddTag() {
		c.addTag(tag);
		assertTrue(c.getTags().contains(tag));
	}

	@Test
	public void testCompareTo() {	
		//System.out.println(c.compareTo(c1));
		assertTrue(c.compareTo(c1)<0);
	}

}
