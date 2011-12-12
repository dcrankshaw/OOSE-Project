package edu.jhu.cs.oose.biblio.model.tests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.junit.Test;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Location;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * @author jiefengzhai
 * 
 * This unit test works only if the config file is set up to drop and recreate all tables every time the program run
 * Or the object created in some test will interference with other test and the object created in test will be stored
 * and interference with the actual program when it runs
 */
public class DatabaseTest extends TestCase{
	

	
	/**
	 * Test how hibernate map the object that has reference to other object into SQL database
	 * @throws Exception 
	 */
	@Test
	public void testDatabaseSchema() throws Exception {
		Session session = Database.getNewSession();
		
		// location
		Location loc = new Location(5);
		loc.setPercentageOfFile((float) 15.5);
		Database.update(loc);
		
		// PDFFileMetadata
		PDFFileMetadata pdfmeta = new PDFFileMetadata("testfiles/test4.pdf");
		Date d = Calendar.getInstance().getTime();
		pdfmeta.setLastOpened(d);
		assertEquals(d.getTime(), pdfmeta.getLastOpened().getTime());
		pdfmeta.setOpenedCount(11);
		Database.update(pdfmeta);
		
		// Tag
		Tag t = new Tag("ok");
		t.addTaggedFiles(pdfmeta);
		t.addTaggedBookmark(new Bookmark(pdfmeta, loc));
		Database.update(t);
		
		// store in database
		Database.commit();
		
		session = Database.getNewSession();
		@SuppressWarnings("unchecked")
		List<Location> locResult = (List<Location>) session.createQuery("from Location").list();
		@SuppressWarnings("unchecked")
		List<PDFFileMetadata> fileResult = (List<PDFFileMetadata>) session.createQuery("from PDFFileMetadata").list();
		@SuppressWarnings("unchecked")
		List<Bookmark> bmResult = (List<Bookmark>) session.createQuery("from Bookmark").list();
		@SuppressWarnings("unchecked")
		List<Tag> tagResult = (List<Tag>) session.createQuery("from Tag").list();
		
		Location l = locResult.get(0);
		PDFFileMetadata f = fileResult.get(0);
		Bookmark bm = bmResult.get(0);
		Tag tg = tagResult.get(0);
		
		assertEquals("ok", tg.getName());
		assertEquals((float) 15.5, l.getPercentageOfFile());
		assertEquals("testfiles/test4.pdf", f.getPathToFile());
		// database cut the Date object, every info other than year, month and date are lost
		Calendar cal=Calendar.getInstance();
		cal.setTime(f.getLastOpened());
		assertEquals(Calendar.getInstance().get(Calendar.DAY_OF_YEAR ), cal.get(Calendar.DAY_OF_YEAR ));
		assertEquals(Calendar.getInstance().get(Calendar.DATE ), cal.get(Calendar.DATE ));
		assertEquals(Calendar.getInstance().get(Calendar.MONTH ), cal.get(Calendar.MONTH ));
		assertEquals(11, f.getOpenedCount());
		assertEquals((float) 15.5, bm.getLocation().getPercentageOfFile());
		assertEquals(11, bm.getFile().getOpenedCount());
		assertEquals(1, tg.getTaggedBookmarks().size());
		assertEquals(1, tg.getTaggedFiles().size());
		assertEquals(1, f.getTags().size());
		List<Bookmark> bList = new LinkedList<Bookmark>();
		bList.addAll(tg.getTaggedBookmarks());
		assertEquals(1, bList.size());
		assertEquals((float) 15.5, bList.get(0).getLocation().getPercentageOfFile());
		assertEquals(11, bList.get(0).getFile().getOpenedCount());
		Database.commit();
	}
	
	/**
	 * Test if hibernate rollback works
	 * @throws Exception 
	 */
	@Test
	public void testRollback() throws Exception {

		// Commit *****************************************************************************************
		Session session = Database.getNewSession();
		new Tag("yoyoyo");
		Database.commit();
		
		// Rollback ***************************************************************************************
		session = Database.getNewSession();
		@SuppressWarnings("unchecked")
		List<Tag> tgResult = (List<Tag>) session.createQuery("from Tag").list();
		Tag tg = tgResult.get(0);
		tg.setName("Rap");
		Database.update(tg);
		Database.rollback();
		
		// Check ******************************************************************************************
		session = Database.getNewSession();
		@SuppressWarnings("unchecked")
		List<Tag> tagResult = (List<Tag>) session.createQuery("from Tag").list();
		Tag tt = tagResult.get(1);
		if (tt == null) {
			assertEquals(1, 2);
		}
		assertEquals(tt.getName(), "yoyoyo");
		Database.commit();
	}
	
	/**
	 * Testing
	 * 1) if merging class contructor with saving in database work
	 * 2) if 1) works with inheritance - meaning A is base class and B extend A
	 * but only A's constructor save A in database and B's constructor call super() only
	 * @throws Exception 
	 */
	@Test
	public void testTagConstructor() throws Exception {
		Database.getNewSession();
		List<FileMetadata> result = new ArrayList<FileMetadata>(2);
		result.add(new PDFFileMetadata("/test/foo.pdf"));
		Database.commit();
		
		Database.getNewSession();
		Tag t = Database.getTag("sup");
		if (t == null) {
			t = new Tag("sup");
		}
		FileMetadata fm = result.get(0);
		t.addTaggedFiles(fm);
		Database.getSession().update(t);
		Database.commit();
	}

	/**
	 * Test if Database connection is establish correctly with the given config file
	 */
	@Test
	public void testDatabaseConnection() {
		Session session = Database.getNewSession();
		
		Location loc = new Location(5);
		loc.setPercentageOfFile((float) 15.5);
		Database.commit();
		
		session = Database.getNewSession();
		@SuppressWarnings("unchecked")
		List<Location> result = (List<Location>) session.createQuery("from Location").list();
		Database.commit();
		Location l = result.get(0);
		assertEquals((float) 15.5, l.getPercentageOfFile());
	}
}