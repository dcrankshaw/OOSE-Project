package iTag;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table( name = "BOOKMARK" )
public class Bookmark {
	
	private FileMetadata file;
	private Location location;
	private Set<Tag> tags;
	
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
	
	public Set<Tag> getTags() {
		return this.tags;
	}
}
