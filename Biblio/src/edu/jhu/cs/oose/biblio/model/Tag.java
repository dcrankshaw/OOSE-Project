package edu.jhu.cs.oose.biblio.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
*A label to associate a group of files and bookmarks.
*/
@Entity
@Table( name = "TAG" )
public class Tag implements Comparable<Tag> {
	
	@Id
	@GenericGenerator(name="generator", strategy="increment")
	@GeneratedValue(generator="generator")
    @Column(name="TAG_ID")
	private int id;
	
	/**
	* The name of the Tag.
	*/
	@Column(name="NAME", nullable=false)
	private String name;

	/**
	* The tags implied by this tag.
	*/
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(
		name="TAG_CHILD",
		joinColumns=@JoinColumn(name="TAG_PARENT_ID", referencedColumnName="TAG_ID"),
		inverseJoinColumns=@JoinColumn(name="TAG_CHILD_ID", referencedColumnName="TAG_ID")
	)
	private Set<Tag> children;

	/**
	* The set of files tagged by this Tag.
	*/
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(
		name="TAG_FILEMETADATA",
		joinColumns=@JoinColumn(name="TAG_ID", referencedColumnName="TAG_ID"),
		inverseJoinColumns=@JoinColumn(name="FMETA_ID", referencedColumnName="FMETA_ID")
	)
	private Set<FileMetadata> taggedFiles;

	/**
	* The set of bookmarks tagged by this Tag.
	*/
	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(
		name="TAG_BOOKMARK",
		joinColumns=@JoinColumn(name="TAG_ID", referencedColumnName="TAG_ID"),
		inverseJoinColumns=@JoinColumn(name="BOOKMARK_ID", referencedColumnName="BOOKMARK_ID")
	)
	private Set<Bookmark> taggedBookmarks;
	
	public Tag() {
		children = new HashSet<Tag>();
		taggedFiles = new HashSet<FileMetadata>();
		taggedBookmarks = new HashSet<Bookmark>();
	}
	
	public int getId() {
		return id;
	}
	
	public Tag(String tagName) {
		name = tagName;
		children = new HashSet<Tag>();
		taggedFiles = new HashSet<FileMetadata>();
		taggedBookmarks = new HashSet<Bookmark>();
	}

	public boolean addTag(Tag tag)
	{
		return false;
	}
	
	public boolean setName(String n) {
		this.name = n;
		return true;
	}
	
	public String getName(){
		return this.name;
	}
	
	public boolean addChildren(Tag tag) {
		return this.children.add(tag);
	}
	
	// TODO change these to return sets instead of collections?
	public Collection<Tag> getChildren() {
		return Collections.unmodifiableCollection(children);
	}
	
	public boolean addTaggedBookmark(Bookmark bkmk) {
		return this.taggedBookmarks.add(bkmk);
	}
	
	public Collection<Bookmark> getTaggedBookmarks() {
		return Collections.unmodifiableCollection(taggedBookmarks);
	}
	
	// does this also need to update the file's list of tags? - Paul
	public boolean addTaggedFiles(FileMetadata file) {
		return this.taggedFiles.add(file);
	}
	
	public Collection<FileMetadata> getTaggedFiles() {
		return Collections.unmodifiableCollection(taggedFiles);
	}
	
	@Override
	public int compareTo(Tag other) {
		return getName().compareTo(other.getName());
	}
}
