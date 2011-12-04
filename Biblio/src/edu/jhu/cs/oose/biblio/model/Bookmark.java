package edu.jhu.cs.oose.biblio.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 *Instantiate a bookmark that maps to a location in a file
 */
@Entity
@Table( name = "BOOKMARK" )
public class Bookmark implements Keyed {

	/** The unique ID used in the database to identify this Bookmark */
	@Id
	@GenericGenerator(name="generator", strategy="increment")
	@GeneratedValue(generator="generator")
    @Column(name="BOOKMARK_ID")
	private int id;

	/*
	* DO WE ALLOW MULTIPLE BOOKMARK POINT TO THE SAME FILEMETADATA??
	* Yes
	*/
	/** The File this bookmark points to */
	@ManyToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="FMETA_ID", nullable=false)
	private FileMetadata file;

	/** The location in the file marked by this bookmark */
	@OneToOne(optional=false, fetch=FetchType.EAGER)
	@JoinColumn(name="LOC_ID")
	private Location location;
	
	/**
	 * A set of tags associated with this bookmark
	 */
	@ManyToMany(mappedBy="taggedBookmarks", fetch=FetchType.EAGER)
	private Set<Tag> tags;

	/**
	 * Creates a new, blank bookmark.
	 * This is only used by Hibernate.
	 * Use the other constructor instead.
	 */
	@SuppressWarnings("unused")
	private Bookmark() {
		tags = new HashSet<Tag>();
	}
	
	/**
	 * Creates a new Bookmark that points to the given location
	 * in the specified file.  This gets a primary key for the
	 * Bookmark, so there must be an open transaction.
	 * @param file the file the Bookmark is in
	 * @param loc the position in the file
	 */
	public Bookmark(FileMetadata file, Location loc) {
		this.file = file;
		this.location = loc;
		this.tags = new HashSet<Tag>();
		
		Database.getSessionFactory().getCurrentSession().save(this);
		@SuppressWarnings("unchecked")
		Database<Bookmark> db = (Database<Bookmark>)Database.get(Bookmark.class);
		db.add(this);
	}
	
	/**
	 * Gets the unique identifier to identifying this particular
	 * Bookmark in the database.
	 * @return the unique identifier for this bookmark
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the unique ID used in the database for this object.
	 * @param id the unique ID used in the database for this object.
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * Returns the metadata this bookmark marks (points to)
	 * @return the metadata this bookmark points to
	 */
	public FileMetadata getFile() {
		return this.file;
	}
	
	/**
	 * Sets the file this bookmark points to
	 * @param f the new file this bookmark should point to
	 */
	public void setFile(FileMetadata f) {
		this.file = f;
	}
	
	/**
	 * Gets the location in the file this bookmark points to.
	 * @return the location in the file this bookmark points to
	 */
	public Location getLocation() {
		return this.location;
	}
	
	/**
	 * Sets how far in the file this bookmark points
	 * @param l how far into the file the bookmark marks
	 */
	public void setLocation(Location l) {
		this.location = l;
	}
	
	/**
	 * Applies a tag to this bookmark
	 * @param t the tag to apply to this bookmark
	 */
	public void addTag(Tag t) {
		tags.add(t);
	}
	
	/**
	 * Returns the set of tags applied to this bookmark
	 * @return the set of tags applied to this bookmark
	 */
	public Set<Tag> getTags() {
		return this.tags;
	}
}
