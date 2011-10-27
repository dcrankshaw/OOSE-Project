package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "BOOKMARK" )
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
	
	/**
	 * A set of tags associated with this bookmark
	 */
	public Set<Tag> tags;

	public Bookmark(FileMetadata f, Location l) {
		this.file = f;
		this.location = l;
		this.tags = new HashSet<Tag>();
	}
	
	// This constructor does NOT copy the set of tags,
	// is that the desired behavior - Paul
	public Bookmark(FileMetadata f, Location l, Set<Tag> t) {
		this.file = f;
		this.location = l;
		this.tags = t;
	}
	
	// Paul: I don't like the existence of this method
	// Why should we use this over creating a new bookmark?
	// I could see changing the location (say, by dragging the
	// bookmark around in the window) but I don't see a use
	// case for completely setting a new place for the bookmark.
	// Also, I think mark is a bad name, but this needs to get discussed
	public boolean mark(FileMetadata f, Location l) {
		this.file = f;
		this.location = l;
		return true;
	}
	
	public FileMetadata getFile() {
		return this.file;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public Set<Tag> tags() {
		return this.tags;
	}
}
