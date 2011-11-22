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
	
	public Tag newTag() {
		// TODO insert this into the database
		return new Tag();
	}
	
	public void deleteTag(Tag toRemove) {
		// TODO remove the tag from the database
	}

}
