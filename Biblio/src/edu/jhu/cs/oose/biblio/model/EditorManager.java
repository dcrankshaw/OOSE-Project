package edu.jhu.cs.oose.biblio.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class EditorManager {
	
	private Set<Tag> tags = new HashSet<Tag>();
	private Set<Category> categories = new HashSet<Category>();
	
	SessionFactory sessionFactory;//TODO private???
	
	@SuppressWarnings("unchecked")
	public Set<Tag> getAllTags()
	{	
		
		Session session = sessionFactory.getCurrentSession();
		tags = (HashSet<Tag>) session.createQuery("from Tag").list();
		return tags;
	}
	
	@SuppressWarnings("unchecked")
	public Set<Category> getAllCategories()
	{	
		Session session = sessionFactory.getCurrentSession();
		categories = (HashSet<Category>) session.createQuery("from Category").list();
		return categories;
	}
	
	// Can we do these with reflection or something? - Paul
	public Tag newTag(String tagName) {
		// TODO insert this into the database
		Tag t = new Tag(tagName);
		Session session = sessionFactory.getCurrentSession();
		session.save(t);
		return new Tag();//not sure what this new tag obj. is doing here.
	}
	
	public void deleteTag(Tag toRemove) {
		// TODO remove the tag from the database
	}
	
	public Category newCategory(String catName) {
		// TODO insert this into the database
		Category c = new Category(catName);
		Session session = sessionFactory.getCurrentSession();
		session.save(c);
		return new Category();
	}
	
	public void deleteCategory(Category toRemove) {
		// TODO remove this from the database
	}

}
