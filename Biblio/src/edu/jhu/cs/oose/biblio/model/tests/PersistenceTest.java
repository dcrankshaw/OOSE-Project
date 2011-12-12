package edu.jhu.cs.oose.biblio.model.tests;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

public class PersistenceTest {
	
	/**
	 * Fills the DB with some known tags and files.
	 * We use this to put the DB in a good state for GUI testing.
	 */
	private static void write() throws Exception {
		Session session = Database.getNewSession();
		session.getTransaction();
		
		session.getTransaction().begin();
		Tag mag = new Tag("Magnetism");
		Tag electric = new Tag("Electricity");
		mag.addChild(electric);
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
		light = new Tag("Analysis");
		light.addTaggedFiles(anotherFile);
		anotherFile.addTag(light);
		session.save(light);
		light = new Tag("Stories");
		light.addTaggedFiles(anotherFile);
		anotherFile.addTag(light);
		session.save(light);
		
		session.save(anotherFile);
		
		Database.commit();
	}
	
	/** Reads the database and prints out all the tags that are in it */
	private static void read() {
		Session session = Database.getNewSession();
		Criteria crit = session.createCriteria(Tag.class);
		System.out.println("***************************************************");
		@SuppressWarnings("unchecked")
		Database<Tag> db = (Database<Tag>)Database.get(Tag.class);
		List<Tag> results = db.executeCriteria(crit);
		System.out.println("There are " + Integer.toString(results.size()) + " tags in the DB.");
		for( Tag t : results ) {
			System.out.println("Found tag " + t.getName());
		}
		System.out.println("***************************************************");
		Database.commit();
	}
	
	public static void main(String[] args) {
		try {
			write();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//read();
	}
}
