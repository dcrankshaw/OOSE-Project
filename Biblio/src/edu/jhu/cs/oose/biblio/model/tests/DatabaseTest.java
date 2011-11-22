package edu.jhu.cs.oose.biblio.model.tests;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.Test;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.Location;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

public class DatabaseTest extends TestCase{

	@Test
	public void testDatabaseConnection() {
		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		
		Location loc = new Location();
		loc.setPercentageOfFile((float) 15.5);
		session.save(loc);
		session.getTransaction().commit();
		
		session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		List<Location> result = (List<Location>) session.createQuery("from Location").list();
		session.getTransaction().commit();
		Location l = result.get(0);
		assertEquals((float) 15.5, l.getPercentageOfFile());
	}

	
	@Test
	public void testDatabaseSchema() {
		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		
		// location
		Location loc = new Location();
		loc.setPercentageOfFile((float) 15.5);
		
		// PDFFileMetadata
		PDFFileMetadata pdfmeta = new PDFFileMetadata();
		pdfmeta.setPathToFile("Back to Back");
		Date d = Calendar.getInstance().getTime();
		pdfmeta.setLastOpened(d);
		assertEquals(d.getTime(), pdfmeta.getLastOpened().getTime());
		pdfmeta.setOpenedCount(11);
		
		// Bookmark
		Bookmark b = new Bookmark();
		b.setFile(pdfmeta);
		b.setLocation(loc);
		
		// Tag
		Tag t = new Tag();
		t.setName("Pop Song");
		t.addTaggedFiles(pdfmeta);
		t.addTaggedBookmark(b);
		
		// store in database
		session.save(loc);
		session.save(pdfmeta);
		session.save(b);
		session.save(t);
		session.getTransaction().commit();
		
		session = sessionFactory.getCurrentSession();
		session.beginTransaction();
		List<Location> locResult = (List<Location>) session.createQuery("from Location").list();
		List<PDFFileMetadata> fileResult = (List<PDFFileMetadata>) session.createQuery("from PDFFileMetadata").list();
		List<Bookmark> bmResult = (List<Bookmark>) session.createQuery("from Bookmark").list();
		List<Tag> tagResult = (List<Tag>) session.createQuery("from Tag").list();
		
		session.getTransaction().commit();
		Location l = locResult.get(0);
		PDFFileMetadata f = fileResult.get(0);
		Bookmark bm = bmResult.get(0);
		Tag tg = tagResult.get(0);
		
		assertEquals((float) 15.5, l.getPercentageOfFile());
		assertEquals("Back to Back", f.getPathToFile());
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
		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		// Commit *****************************************************************************************
		session.getTransaction().begin();
		Tag t = new Tag();
		t.setName("Pop Song");
		session.save(t);
		session.getTransaction().commit();
		
		// Rollback ***************************************************************************************
		session = sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		List<Tag> tagResult = (List<Tag>) session.createQuery("from Tag").list();
		Tag tg = tagResult.get(0);
		int id = tg.getId();
		tg.setName("Rap");
		session.update(tg);
		session.getTransaction().rollback();
		
		// Check ******************************************************************************************
		session = sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		Tag tt = (Tag) session.get(Tag.class, id);
		assertEquals(tt.getName(), "Pop Song");
		session.getTransaction().commit();
	}
}