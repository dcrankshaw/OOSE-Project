package edu.jhu.cs.oose.biblio.model;

import java.util.Set;

/**
*Instantiate a bookmark that maps to a location in a file
*/
public class Bookmark {

	/**
	*The file that contains the location to which the bookmark maps
	*/
	public FileMetadata file;

	/**
	*The location in the file
	*/
	public Location location;
	//TODO

	/**
	 * A set of tags associated with this bookmark
	 */
	public Set<Tag> tags;

}
