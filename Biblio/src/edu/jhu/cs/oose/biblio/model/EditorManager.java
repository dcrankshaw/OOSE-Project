package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

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
		
		Session session = sessionFactory.getCurrentSession();
		tags = (HashSet<Tag>) session.createQuery("from Tag").list();
		return tags;
	}
	
	/**
	 * Get all the categories in the database.
	 * @return categories A set of all categories currently in the database.
	 */
	@SuppressWarnings("unchecked")
	public Set<Category> getAllCategories()
	{	
		Session session = sessionFactory.getCurrentSession();
		categories = (HashSet<Category>) session.createQuery("from Category").list();
		return categories;
	}
	/**
	 * Insert a new tag into the database.
	 * @param tagName The name of the new tag.
	 * @return the new tag as an object.
	 */
	// Can we do these with TODO reflection or something? - Paul
	public Tag newTag(String tagName) {
		Tag t = new Tag(tagName);
		Session session = sessionFactory.getCurrentSession();
		session.save(t);
		return new Tag();//TODO not sure what this new tag obj. is doing here.
	}
	
	/**
	 * Remove the tag from the database.
	 * @param toRemove The name of the tag to remove.
	 */
	public void deleteTag(Tag toRemove) {
		// TODO remove the tag from the database
	}
	
	/**
	 * Insert a new category into the database.
	 * @param catName The name of the new category.
	 * @return the new category as an object.
	 */
	public Category newCategory(String catName) {
		// TODO insert this into the database
		Category c = new Category(catName);
		Session session = sessionFactory.getCurrentSession();
		session.save(c);
		return new Category();
	}
	
	/**
	 * Remove this from the database.
	 * @param toRemove The name of the category to remove.
	 */
	public void deleteCategory(Category toRemove) {
		// TODO remove this from the database
	}

}
