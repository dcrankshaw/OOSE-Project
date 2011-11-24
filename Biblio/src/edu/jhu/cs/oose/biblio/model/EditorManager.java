package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 * Allows the GUI to edit objects and relationships by abstracting
 * out the database interactions
 *
 */
public class EditorManager {
	
	public Set<Tag> getAllTags()
	{
		Session session = SearchManager.getSessionFactory().getCurrentSession();
		Criteria crit = session.createCriteria(Tag.class);
		return new HashSet<Tag>((List<Tag>)crit.list());
	}
	
	public Set<Category> getAllCategories()
	{
		Session session = SearchManager.getSessionFactory().getCurrentSession();
		Criteria crit = session.createCriteria(Category.class);
		return new HashSet<Category>((List<Category>)crit.list());
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
	
	public void deleteTag(Tag toRemove) {
		Session session = SearchManager.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(toRemove);
		session.getTransaction().commit();
	}
	
	public Category newCategory() {
		Session session = SearchManager.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Category c = new Category();
		session.save(c);
		session.getTransaction().commit();
		return c;
	}
	
	public void deleteCategory(Category toRemove) {
		Session session = SearchManager.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		session.delete(toRemove);
		session.getTransaction().commit();
	}

}
