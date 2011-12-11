package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Query;
import org.hibernate.Session;

/**
 * Allows the GUI to edit objects and relationships by abstracting out the
 * database interactions
 * 
 */
public class EditorManager {

	/**
	 * Gets all the instances of a particular class in the DB.
	 * 
	 * @param cl
	 *            the class to get all of
	 * @return a set of all the instances of a particular class in the DB.
	 */
	private Set<?> getAll(Class<?> cl) {
		Session session = Database.getNewSession();
		session.beginTransaction();
		String tableName = cl.getName();
		int idx = tableName.lastIndexOf('/');
		if( idx >= 0 ) {
			tableName= tableName.substring(idx);
		}
		Query query = session.createQuery("from " + tableName);
		// Getting a database isn't type safe, but I promise it works
		Set<?> result = new HashSet<Keyed>(
				((Database<?>) Database.get(cl)).executeQuery(query));

		session.getTransaction().commit();
		return result;
	}

	/**
	 * Returns a set of all the Tags in the database.
	 * 
	 * @return a set of all the Tags in the database.
	 */
	@SuppressWarnings("unchecked")
	public Set<Tag> getAllTags()
	{
		return (Set<Tag>)this.getAll(Tag.class);
	}

	/**
	 * Returns a set of all the Categories in the database.
	 * 
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
	 * 
	 * @return the new Tag as an object.
	 */
	public Tag newTag() {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			Tag t = new Tag("Untitled Tag");
			session.save(t);
			session.getTransaction().commit();
			return t;
		} catch (Exception e) {
			/*
			 * exception will never happen because Tag constructor only throws
			 * exception if tagname has a colon in it. The tagname in this
			 * method is hardcoded and will never have a colon in the name
			 */
			return null;
		}
	}

	/**
	 * Remove the tag from the database.
	 * 
	 * @param toRemove
	 *            The name of the tag to remove.
	 */
	public void deleteTag(Tag toRemove) {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(toRemove);
		session.getTransaction().commit();
	}

	/**
	 * Insert a new Category into the database.
	 * 
	 * @return the new Category as an object.
	 */
	public Category newCategory() {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		try {
			Category c = new Category("Untitled Category");
			session.save(c);
			session.getTransaction().commit();
			return c;
		} catch (Exception e) {
			/*
			 * exception will never happen because Tag constructor only throws
			 * exception if tagname has a colon in it. The tagname in this
			 * method is hardcoded and will never have a colon in the name
			 */
			return null;
		}
	}

	/**
	 * Remove this from the database.
	 * 
	 * @param toRemove
	 *            The name of the category to remove.
	 */
	public void deleteCategory(Category toRemove) {
		Session session = Database.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(toRemove);
		session.getTransaction().commit();
	}

}
