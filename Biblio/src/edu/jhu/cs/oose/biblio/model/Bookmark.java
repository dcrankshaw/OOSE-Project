package edu.jhu.cs.oose.biblio.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table( name = "BOOKMARK" )
/**
*Instantiate a bookmark that maps to a location in a file
*/
public class Bookmark {

	@Id
	@GenericGenerator(name="generator", strategy="increment")
	@GeneratedValue(generator="generator")
    @Column(name="BOOKMARK_ID")
	private int id;
	/*
	@Column(name="FMETA_ID")
	private int fid;
	
	@Column(name="LOC_ID")
	private int lid;
	*/
	/**
	* DO WE ALLOW MULTIPLE BOOKMARK POINT TO THE SAME FILEMETADATA??
	*/
	@OneToOne(optional=true, fetch=FetchType.EAGER)
	@JoinColumn(name="FMETA_ID")
	private FileMetadata file;

	/**
	*The location in the file
	*/
	@OneToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="LOC_ID")
	private Location location;
	
	/**
	 * A set of tags associated with this bookmark
	 */
	@ManyToMany(mappedBy="taggedBookmarks", fetch=FetchType.EAGER)
	private Set<Tag> tags;

	public Bookmark() {
		super();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	/*
	public void setLocId(int id) {
		this.lid = id;
	}
	
	public int getLocId() {
		return lid;
	}
	
	public int getFileId() {
		return fid;
	}
	
	public void setFileId(int fid) {
		this.fid = fid;
	}
	*/
	public FileMetadata getFile() {
		return this.file;
	}
	
	public void setFile(FileMetadata f) {
		this.file = f;
	}
	
	public Location getLocation() {
		return this.location;
	}
	
	public void setLocation(Location l) {
		this.location = l;
	}
	
	public void addTag(Tag t) {
		tags.add(t);
	}
	
	public Set<Tag> getTag() {
		return this.tags;
	}
}
