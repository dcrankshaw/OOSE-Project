package edu.jhu.cs.oose.biblio.model;

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
	//TODO

	/**
	 * A set of tags associated with this bookmark
	 */
	public Set<Tag> tags;

	public Bookmark(FileMetadata f, Location l) {
		this.file = f;
		this.location = l;
	}
	
	public Bookmark(FileMetadata f, Location l, Set<Tag> t) {
		this.file = f;
		this.location = l;
		this.tags = t;
	}
	
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
