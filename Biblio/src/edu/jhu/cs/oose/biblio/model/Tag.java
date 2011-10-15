package edu.jhu.cs.oose.biblio.model;

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

}
