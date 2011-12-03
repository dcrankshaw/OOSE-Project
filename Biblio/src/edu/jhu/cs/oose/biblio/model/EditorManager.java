	package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Allows the GUI to edit objects and relationships by abstracting
 * out the database interactions
 *
 */
public class EditorManager {
	
	/**
	 * Gets all the instances of a particular class in the DB.
	 * @param cl the class to get all of
	 * @return a set of all the instances of a particular class in the DB.
	 */
	private Set<?> getAll(Class<?> cl) {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Criteria crit = session.createCriteria(cl);
		
		// Getting a database isn't type safe, but I promise it works
		Set<?> result = new HashSet<Keyed>(((Database<?>)Database.get(cl)).executeCriteria(crit));
		
		session.getTransaction().commit();
		return result;
	}
	
	/**
	 * Returns a set of all the Tags in the database.
	 * @return a set of all the Tags in the database.
	 */
	@SuppressWarnings("unchecked")
	public Set<Tag> getAllTags()
	{
		return (Set<Tag>)this.getAll(Tag.class);
	}
	
	/**
	 * Returns a set of all the Categories in the database.
	 * @return a set of all the Categories in the database.
	 */
	@SuppressWarnings("unchecked")
	public Set<Category> getAllCategories()

	{
		return (Set<Category>)this.getAll(Category.class);
	}
	
	// Can we do these with reflection or something? - Paul
	/**
	 * Insert a new Tag into the database.
	 * @return the new Tag as an object.
	 */
	public Tag newTag() {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Tag t = new Tag();
		session.save(t);
		session.getTransaction().commit();
		return t;
	}
	
	/**
	 * Remove the tag from the database.
	 * @param toRemove The name of the tag to remove.
	 */
	public void deleteTag(Tag toRemove) {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(toRemove);
		session.getTransaction().commit();
	}
	
	/**
	 * Insert a new Category into the database.
	 * @return the new Category as an object.
	 */
	public Category newCategory() {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Category c = new Category();
		session.save(c);
		session.getTransaction().commit();
		return c;
	}
	
	/**
	 * Remove this from the database.
	 * @param toRemove The name of the category to remove.
	 */
	public void deleteCategory(Category toRemove) {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(toRemove);
		session.getTransaction().commit();
	}

}
