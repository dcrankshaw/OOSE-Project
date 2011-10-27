package edu.jhu.cs.oose.biblio.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
*A label to associate a group of files and bookmarks.
*/
public class Tag {

	/**
	* The name of the Tag.
	*/
	public String name;

	/**
	* The tags implied by this tag.
	*/
	public Set<Tag> children;

	/**
	* The set of files tagged by this Tag.
	*/
	public Set<FileMetadata> taggedFiles;

	/**
	* The set of bookmarks tagged by this Tag.
	*/
	public Set<Bookmark> taggedBookmarks;
	
	public Tag(String tagName) {
		name = tagName;
		children = new HashSet<Tag>();
		taggedFiles = new HashSet<FileMetadata>();
	}

	public boolean addTag(Tag tag)
	{
		return false;
	}

	// TODO This constructor does not copy the sets, is that desirable?  - Paul
	public Tag(Set<Tag> c, String n, Set<Bookmark> t, Set<FileMetadata> ta) {
		this.children = c;
		this.name = n;
		this.taggedBookmarks = t;
		this.taggedFiles = ta;
	}
	
	public boolean setName(String n) {
		this.name = n;
		return true;
	}
	
	public String getName(){
		return this.name;
	}
	
	public boolean addChildren(Tag tag) {
		return this.children.add(tag);
	}
	
	public Collection<Tag> getChildren() {
		return Collections.unmodifiableCollection(children);
	}
	
	public boolean tagBookmark(Bookmark bkmk) {
		return this.taggedBookmarks.add(bkmk);
	}
	
	public Collection<Bookmark> getBookmarks() {
		return Collections.unmodifiableCollection(taggedBookmarks);
	}
	
	// does this also need to update the file's list of tags? - Paul
	public boolean tagFile(FileMetadata file) {
		return this.taggedFiles.add(file);
	}
	
	public Collection<FileMetadata> getFiles() {
		return Collections.unmodifiableCollection(taggedFiles);
	}
}
