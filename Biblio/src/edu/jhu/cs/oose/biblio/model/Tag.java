package edu.jhu.cs.oose.biblio.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

/**
 * A label to associate a group of files and bookmarks.
 */
@Entity
@Table(name = "TAG")
public class Tag implements Comparable<Tag>, Keyed {

	/** The database's primary key for this Tag */
	@Id
	@GenericGenerator(name = "generator", strategy = "increment")
	@GeneratedValue(generator = "generator")
	@Column(name = "TAG_ID")
	private int id;

	/** The name of the Tag. */
	@Column(name = "NAME", nullable = false)
	private String name;

	/** The tags implied by this tag. */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "TAG_CHILD", joinColumns = @JoinColumn(name = "TAG_PARENT_ID", referencedColumnName = "TAG_ID"), inverseJoinColumns = @JoinColumn(name = "TAG_CHILD_ID", referencedColumnName = "TAG_ID"))
	private Set<Tag> children;

	/** The set of files tagged by this Tag. */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "TAG_FILEMETADATA", joinColumns = @JoinColumn(name = "TAG_ID", referencedColumnName = "TAG_ID"), inverseJoinColumns = @JoinColumn(name = "FMETA_ID", referencedColumnName = "FMETA_ID"))
	private Set<FileMetadata> taggedFiles;

	/** The set of bookmarks tagged by this Tag. */
	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "TAG_BOOKMARK", joinColumns = @JoinColumn(name = "TAG_ID", referencedColumnName = "TAG_ID"), inverseJoinColumns = @JoinColumn(name = "BOOKMARK_ID", referencedColumnName = "BOOKMARK_ID"))
	private Set<Bookmark> taggedBookmarks;

	/**
	 * Creates a new, blank Tag.
	 * This should only ever be used by Hibernate.
	 * DO NOT call this yourself.
	 */
	@SuppressWarnings("unused")
	private Tag() {
		name = null;
		children = new HashSet<Tag>();
		taggedFiles = new HashSet<FileMetadata>();
		taggedBookmarks = new HashSet<Bookmark>();
	}

	/**
	 * Creates a new Tag with the given name.
	 * This adds the Tag to our in memory cache, so you need to have
	 * an open transaction when this constructor runs.
	 * If you need an object, use this constructor;
	 * DO NOT use the default constructor.
	 * @param tagName the name of the new Tag.
	 */
	public Tag(String tagName) {
		name = tagName;
		children = new HashSet<Tag>();
		taggedFiles = new HashSet<FileMetadata>();
		taggedBookmarks = new HashSet<Bookmark>();
		
		Database.getSessionFactory().getCurrentSession().save(this);
		@SuppressWarnings("unchecked")
		Database<Tag> database = (Database<Tag>)Database.get(Tag.class);
		database.add(this);
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	/**
	 * Sets the name of this tag to the given value.  The name
	 * may not include the ':' character, as that denotes the end
	 * of a Category name when searching.  Attempting to set a name
	 * with a colon will fail
	 * @param n the new name of this Tag
	 * @return true if this is a valid tag name, or false if the name
	 * contains a colon and is not a valid name.
	 */
	public boolean setName(String n) {
		if(n.contains(":"))
			return false;
		else {
			this.name = n;
			return true;
		}
	}

	/**
	 * Returns the name of this tag
	 * @return the name of this tag
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Adds the given Tag to this one's children.  This means
	 * that this Tag implies that the given Tag also applies.
	 * That is, if I am Physics, and tag is "Particle accelerators",
	 * then "Particle accelerators" is a child of "Physics".
	 * @param tag the new Child
	 * @return true if adding the Child succeeded
	 * false if the child was already added
	 */
	public boolean addChild(Tag tag) {
		return this.children.add(tag);
	}

	// TODO change these to return sets instead of collections?
	/**
	 * Returns all of the children of this Tag.
	 * That is, all of the children that imply that this Tag
	 * also applies to a given file.
	 * @return the Tags that imply this one
	 */
	public Collection<Tag> getChildren() {
		return Collections.unmodifiableCollection(children);
	}

	/**
	 * Adds a Bookmark to the set of things that will be found
	 * when searching using this Tag.
	 * @param bkmk the bookmark to add
	 * @return true on success, false if the bookmark is already added.
	 */
	public boolean addTaggedBookmark(Bookmark bkmk) {
		return this.taggedBookmarks.add(bkmk);
	}

	/**
	 * Returns a collection of Bookmarks that have been tagged with this Tag.
	 * @return a collection of Bookmarks that have been tagged with this Tag.
	 */
	public Collection<Bookmark> getTaggedBookmarks() {
		return Collections.unmodifiableCollection(taggedBookmarks);
	}

	/**
	 * Adds a FileMetadata to the set of things that will be found
	 * when searching using this Tag.
	 * @param file the FileMetadata to add
	 * @return true on success, false if the FileMetadata is already added.
	 */
	public boolean addTaggedFiles(FileMetadata file) {
		return this.taggedFiles.add(file);
	}

	/**
	 * Returns a collection of FileMetadatas that have been tagged with this Tag.
	 * @return a collection of FileMetadatas that have been tagged with this Tag.
	 */
	public Collection<FileMetadata> getTaggedFiles() {
		return Collections.unmodifiableCollection(taggedFiles);
	}

	/** Compares tags based on a string comparison of their names */
	@Override
	public int compareTo(Tag other) {
		return getName().compareTo(other.getName());
	}

	/**
	 * Get all tags that imply this tag. Basically executes a breadth first search of all this tag's children.
	 * @return The set of all tags found
	 */
	public Set<Tag> getAllDescendants() {
		Set<Tag> descendants = new HashSet<Tag>();
		Queue<Tag> q = new LinkedList<Tag>();
		for(Tag child: this.children) {
			q.add(child);
		}
		while(!q.isEmpty())
		{
			Tag current = q.remove();
			if(descendants.add(current)) {
				for(Tag child: current.children)
				{
					q.add(child);
				}
			}
		}
		return descendants;
	}
	
	@Override
	public String toString() {
		return getName();
	}
}
