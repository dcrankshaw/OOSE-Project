package edu.jhu.cs.oose.biblio.model;

import java.util.Collection;

public interface Tagable {
	public boolean addTag(Tag t);
	public boolean removeTag(Tag t);
	public Collection<Tag> getTags();
}
