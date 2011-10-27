package edu.jhu.cs.oose.biblio.model;

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
	
	public boolean tagBookmark(Bookmark bkmk) {
		return this.taggedBookmarks.add(bkmk);
	}
	
	public boolean tagFile(FileMetadata file) {
		return this.taggedFiles.add(file);
	}


}
