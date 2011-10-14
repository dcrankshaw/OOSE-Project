package edu.jhu.cs.oose.biblio.model;

/**
*Instantiate a Tag that associates a group of files or bookmarks.
*/
public class Tag {

	/**
	* The name of the Tag.
	*/
	public String name;

	/**
	* The derived tagging.
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
