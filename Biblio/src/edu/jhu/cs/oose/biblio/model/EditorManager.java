package edu.jhu.cs.oose.biblio.model;

import java.util.Set;

public class EditorManager {
	
	public Set<Tag> getAllTags()
	{
		return null;
	}
	
	public Set<Category> getAllCategories()
	{
		return null;
	}
	
	// Can we do these with reflection or something? - Paul
	public Tag newTag() {
		// TODO insert this into the database
		return new Tag();
	}
	
	public void deleteTag(Tag toRemove) {
		// TODO remove the tag from the database
	}
	
	public Category newCategory() {
		// TODO insert this into the database
		return new Category();
	}
	
	public void deleteCategory(Category toRemove) {
		// TODO remove this from the database
	}

}
