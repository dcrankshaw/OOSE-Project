package edu.jhu.cs.oose.biblio.model.tests;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Test;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Location;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

public class DatabaseTest extends TestCase{
	
	@Test
	public void testTagConstructor() throws Exception {
		SessionFactory sessionFactory = Database.getSessionFactory();
		
		sessionFactory.getCurrentSession().beginTransaction();
		List<FileMetadata> result = new ArrayList<FileMetadata>(2);
		result.add(new PDFFileMetadata("/test/foo.pdf"));
		sessionFactory.getCurrentSession().getTransaction().commit();
		
		sessionFactory.getCurrentSession().beginTransaction();
		Tag t = Database.getTag("sup");
		if (t == null) {
			t = new Tag("Sup");
		}
		FileMetadata fm = result.get(0);
		t.addTaggedFiles(fm);
		sessionFactory.getCurrentSession().update(t);
		sessionFactory.getCurrentSession().getTransaction().commit();
	}

	@Test
	public void testDatabaseConnection() {
		SessionFactory sessionFactory = Database.getSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		
		Location loc = new Location(5);
		loc.setPercentageOfFile((float) 15.5);
		session.save(loc);
		Database.commit();
		
		session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		@SuppressWarnings("unchecked")
		List<Location> result = (List<Location>) session.createQuery("from Location").list();
		Database.commit();
		Location l = result.get(0);
		assertEquals((float) 15.5, l.getPercentageOfFile());
	}

	public void testDatabaseSession() {
		SessionFactory sessionFactory = Database.getSessionFactory();
		sessionFactory.getCurrentSession().beginTransaction();
		Tag t = new Tag("sup");
		sessionFactory.getCurrentSession().save(t);
		sessionFactory.getCurrentSession().getTransaction().commit();
	}

	@Test
	public void testDatabaseSchema() {
		SessionFactory sessionFactory = Database.getSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		
		// location
		Location loc = new Location(5);
		loc.setPercentageOfFile((float) 15.5);
		
		// PDFFileMetadata
		PDFFileMetadata pdfmeta = new PDFFileMetadata("testfiles/test4.pdf");
		Date d = Calendar.getInstance().getTime();
		pdfmeta.setLastOpened(d);
		assertEquals(d.getTime(), pdfmeta.getLastOpened().getTime());
		pdfmeta.setOpenedCount(11);
		
		// Bookmark
		Bookmark b = new Bookmark(pdfmeta, loc);
		
		// Tag
		Tag t = new Tag("Pop Song");
		t.addTaggedFiles(pdfmeta);
		t.addTaggedBookmark(b);
		
		// store in database
		session.save(loc);
		session.save(pdfmeta);
		session.save(b);
		session.save(t);
		Database.commit();
		
		session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		@SuppressWarnings("unchecked")
		List<Location> locResult = (List<Location>) session.createQuery("from Location").list();
		@SuppressWarnings("unchecked")
		List<PDFFileMetadata> fileResult = (List<PDFFileMetadata>) session.createQuery("from PDFFileMetadata").list();
		@SuppressWarnings("unchecked")
		List<Bookmark> bmResult = (List<Bookmark>) session.createQuery("from Bookmark").list();
		@SuppressWarnings("unchecked")
		List<Tag> tagResult = (List<Tag>) session.createQuery("from Tag").list();
		
		Database.commit();
		Location l = locResult.get(0);
		PDFFileMetadata f = fileResult.get(0);
		Bookmark bm = bmResult.get(0);
		Tag tg = tagResult.get(0);
		
		assertEquals((float) 15.5, l.getPercentageOfFile());
		assertEquals("testfiles/test4.pdf", f.getPathToFile());
		// database cut the Date object, every info other than year, month and date are lost
		Calendar cal=Calendar.getInstance();
		cal.setTime(f.getLastOpened());
		assertEquals(Calendar.getInstance().get(Calendar.DAY_OF_YEAR ), cal.get(Calendar.DAY_OF_YEAR ));
		assertEquals(Calendar.getInstance().get(Calendar.DATE ), cal.get(Calendar.DATE ));
		assertEquals(Calendar.getInstance().get(Calendar.MONTH ), cal.get(Calendar.MONTH ));
		assertEquals(11, f.getOpenedCount());
		assertEquals("Pop Song", tg.getName());
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
	}

	@Test
	public void testRollback() {
		SessionFactory sessionFactory = Database.getSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		// Commit *****************************************************************************************
		session.getTransaction().begin();
		Tag t = new Tag("Pop Song");
		session.save(t);
		Database.commit();
		
		// Rollback ***************************************************************************************
		session = sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		@SuppressWarnings("unchecked")
		List<Tag> tagResult = (List<Tag>) session.createQuery("from Tag").list();
		Tag tg = tagResult.get(0);
		int id = tg.getId();
		tg.setName("Rap");
		session.update(tg);
		Database.rollback();
		
		// Check ******************************************************************************************
		session = sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		Tag tt = (Tag) session.get(Tag.class, id);
		assertEquals(tt.getName(), "Pop Song");
		Database.commit();
	}
}