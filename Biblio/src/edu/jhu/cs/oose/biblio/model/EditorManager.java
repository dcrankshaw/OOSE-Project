package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.List;
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
	
	private Set<Tag> tags = new HashSet<Tag>();
	private Set<Category> categories = new HashSet<Category>();
	
	SessionFactory sessionFactory;//TODO private???
	
	/**
	 * Get all the tags in the database.
	 * @return tags A set of all tags currently in the database.
	 */
	@SuppressWarnings("unchecked")
	public Set<Tag> getAllTags()

	{
		Session session = SearchManager.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Criteria crit = session.createCriteria(Tag.class);
		Set<Tag> result = new HashSet<Tag>((List<Tag>)crit.list());
		session.getTransaction().commit();
		return result;
	}
	
	/**
	 * Get all the categories in the database.
	 * @return categories A set of all categories currently in the database.
	 */
	@SuppressWarnings("unchecked")
	public Set<Category> getAllCategories()

	{
		Session session = SearchManager.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Criteria crit = session.createCriteria(Category.class);
		Set<Category> result = new HashSet<Category>((List<Category>)crit.list());
		session.getTransaction().commit();
		return result;
	}
	
	// Can we do these with reflection or something? - Paul
	public Tag newTag() {
		Session session = SearchManager.getSessionFactory().getCurrentSession();
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
		Session session = SearchManager.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(toRemove);
		session.getTransaction().commit();
	}
	
	/**
	 * Insert a new category into the database.
	 * @param catName The name of the new category.
	 * @return the new category as an object.
	 */

	public Category newCategory() {
		Session session = SearchManager.getSessionFactory().getCurrentSession();
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
		Session session = SearchManager.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(toRemove);
		session.getTransaction().commit();
	}

}
