package edu.jhu.cs.oose.biblio.model.tests;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.Location;

/**
 * Unit Test for Location class.
 * 
 *
 */
public class LocationTest {
	Location l;
	
	@Before
	public void setUp() throws Exception {
		Database.getNewSession();
		l = new Location(5);
	}
	
	/**
	 * Test setId and getId.
	 */
	@Test
	public void testSetGetId() {
		l.setId(1);
		assertTrue(l.getId()==1);
	}

	/**
	 * Test setPercentageOfFile and getPercentageOfFile.
	 */
	@Test
	public void testSetGetPercentageOfFile() {
		l.setPercentageOfFile(7);
		assertTrue(l.getPercentageOfFile()==7);

	}

}
