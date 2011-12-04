package edu.jhu.cs.oose.biblio.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

/**
 * A set of information about a file.
 */
@Entity
@Table( name = "FILEMETADATA" )
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name="TYPE", discriminatorType=DiscriminatorType.INTEGER)
public abstract class FileMetadata implements Keyed {

	/** The ID used to identify this object in the database */
	@Id
	@GenericGenerator(name="generator", strategy="increment")
	@GeneratedValue(generator="generator")
    @Column(name="FMETA_ID")
	private int id;

	/**
	 * The set of tags associated with this file
	 */
	@ManyToMany(mappedBy="taggedFiles", fetch=FetchType.EAGER)
	private Set<Tag> tags;

	/**
	 * The full path name of the file
	 */
	@Column(name="PATH_TO_FILE")
	private String pathToFile;

	/**
	 * The date of the last time the file was opened
	 */
	@Column(name="LAST_OPENED")
	@Temporal(TemporalType.DATE)
	private Date lastOpened;

	/**
	 * The number of the times the file has been opened
	 */
	@Column(name="OPENED_COUNT")
	private int openedCount;
	
	/**
	 * Creates a new empty object.  This is mostly here so that
	 * Hibernate can fill in all the data.
	 */
	public FileMetadata() {
		this.lastOpened = new Date();
		this.openedCount = 0;
		this.pathToFile = "";
		this.tags = new HashSet<Tag>();
	}
    
	/**
	 * Creates a new for the contents on disk with default initialization for the other fields.
	 * @param path the path to the file contents residing on disk
	 */
	public FileMetadata(String path) {
		this.lastOpened = new Date();
		this.openedCount = 0;
		this.pathToFile = path;
		this.tags = new HashSet<Tag>();
	}
	
	/**
	 * Creates a new FileMetadata with all its fields set to the given arguments
	 * @param date the last time this document was opened
	 * @param timesOpened the number of times this was opened 
	 * @param path the path to the file contents residing on disk
	 * @param fileTags the tags to be applied to this file (This is NOT copied)
	 */
	public FileMetadata(Date date, int timesOpened, String path, Set<Tag> fileTags) {
		this.lastOpened = date;
		this.openedCount = timesOpened;
		this.pathToFile = path;
		this.tags = fileTags;
	}
	
	/**
	 * Gets the unique ID used in the database for this object.
	 * @return the unique ID used in the database for this object.
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
	 * Returns true if the two metadata objects describe the same file
	 * @param otherFile the other metadata object to compare with
	 * @return true if the two objects describe the same file
	 */
	public boolean equals(FileMetadata otherFile)
	{
		return otherFile.getPathToFile().equals(this.pathToFile);
	}

	/**
	 * Returns a copy of the set of tags applied to this file.
	 * @return a copy of the set of tags applied to this file.
	 */
	public Set<Tag> getTags() {
		return tags;
	}
	
	/**
	 * Adds the given tag to those applied to this FileMetadata
	 * @param t the tag to apply to this file
	 */
	public void addTag(Tag t) {
		tags.add(t);
	}

	/**
	 * Returns the path to the file contents residing on disk.
	 * @return the path to the file contents residing on disk.
	 */
	public String getPathToFile() {
		return pathToFile;
	}
	
	/**
	 * Sets the path to the on disk file
	 * @param pathToFile the new path to the on disk file
	 */
	public void setPathToFile(String pathToFile) {
		this.pathToFile = pathToFile;
	}

	/**
	 * Returns the last time this file was opened.
	 * @return the last time this file was opened.
	 */
	public Date getLastOpened() {
		return lastOpened;
	}

	/**
	 * Sets the last time this file was opened.
	 * @param lastOpened the last time this file was opened.
	 */
	public void setLastOpened(Date lastOpened) {
		this.lastOpened = lastOpened;
	}

	/**
	 * Returns the number of times this file has been opened.
	 * @return the number of times this file has been opened.
	 */
	public int getOpenedCount() {
		return openedCount;
	}

	/**
	 * Sets the number of times this file has been opened.
	 * @param openedCount the new number of times this file has been opened.
	 */
	public void setOpenedCount(int openedCount) {
		this.openedCount = openedCount;
	}
	
	/**
	 * Increases this file's opened count by 1
	 */
	public void incrementOpenCount() {
		this.openedCount += 1;
	}
	
    /**
	 * Searches the associated FileContents for the given search term
	 * @param searchTerm the text to search for
	 * @return the number of the times the term occurs
	 * @throws Exception if there was an error reading or parsing the file
	 */
	//TODO change this to a more specific exception - Dan
	//yep, probably not throwing Exception
	public abstract int searchText(String searchTerm) throws Exception;
	
	/**
	 * Returns a string identifying this file to the user.
	 * @return a string identifying this file to the user
	 */
	public String getName() {
		// TODO have this return only the last portion of the path
		if( this.pathToFile == null ) {
			return "";
		}
		return this.pathToFile;
	}
	
	/**
	 * Get the file contents
	 * @return contents The file contents
	 */
	public abstract FileContents getContents();
	
	@Override
	public int getKey() {
		return id;
	}
}
