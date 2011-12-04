package edu.jhu.cs.oose.biblio.model.tests;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

public class PersistenceTest {
	private static void write() {
		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		
		session.getTransaction().begin();
		Tag mag = new Tag("Magnetism");
		Tag electric = new Tag("Electricity");
		mag.addChildren(electric);
		session.save(electric);
		session.save(mag);

		FileMetadata em = new PDFFileMetadata("testfiles/em.pdf");
		em.addTag(electric);
		electric.addTaggedFiles(em);
		session.save(em);
		
		Tag supercomputing = new Tag("Supercomputing");
		
		FileMetadata testFile = new PDFFileMetadata("testfiles/test1.pdf");
		testFile.addTag(supercomputing);
		supercomputing.addTaggedFiles(testFile);
		session.save(testFile);
		session.save(supercomputing);
		
		FileMetadata anotherFile = new PDFFileMetadata("testfiles/101_analysis_stories.pdf");
		FileMetadata again = new EpubFileMetadata("testfiles/sherlockholmes.epub");
		Tag fiction = new Tag("Fiction");
		again.addTag(fiction);
		fiction.addTaggedFiles(again);
		session.save(again);
		session.save(fiction);
		
		Tag light = new Tag("Light Reading");
		light.addTaggedFiles(anotherFile);
		anotherFile.addTag(light);
		session.save(light);
		/*light = new Tag("Analysis");
		light.addTaggedFiles(anotherFile);
		anotherFile.addTag(light);
		session.save(light);
		light = new Tag("Stories");
		light.addTaggedFiles(anotherFile);
		anotherFile.addTag(light);*/
		session.save(light);
		
		session.save(anotherFile);
		
		session.getTransaction().commit();
	}
	
	private static void read() {
		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		Criteria crit = session.createCriteria(Tag.class);
		System.out.println("***************************************************");
		List<Tag> results = (List<Tag>)crit.list();
		System.out.println("There are " + Integer.toString(results.size()) + " tags in the DB.");
		for( Tag t : results ) {
			System.out.println("Found tag " + t.getName());
		}
		System.out.println("***************************************************");
		session.getTransaction().commit();
	}
	
	public static void main(String[] args) {
		//write();
		read();
	}
}
