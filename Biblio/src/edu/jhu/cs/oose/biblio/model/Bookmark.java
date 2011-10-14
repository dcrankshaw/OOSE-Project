package edu.jhu.cs.oose.biblio.model;

/**
*Instantiate a bookmark that maps to a location in a file
*/
public class Bookmark {

	/**
	*The file that contains the location to which the bookmark maps
	*/
	public FileMetadata file;

	/**
	*The location int the file
	*/
	public Location location;
	//TODO

	/**
	*Tagging of the bookmark that allows users to put up a inter-file 		*excerpt/study note.
	*/
	public Set<Tag> tags;

}
