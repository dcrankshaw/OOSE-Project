package edu.jhu.cs.oose.biblio.model.tests;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import edu.jhu.cs.oose.biblio.model.Tag;

public class PersistenceTest {
	private static void write() {
		SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
		Session session = sessionFactory.getCurrentSession();
		
		session.getTransaction().begin();
		Tag t = new Tag("FirstTag");
		session.save(t);
		t = new Tag("SecondTag");
		session.save(t);
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
