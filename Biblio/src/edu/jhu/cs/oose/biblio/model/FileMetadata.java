package edu.jhu.cs.oose.biblio.model;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * A set of information about a file
 */
@Entity
@Table( name = "FILEMETADATA" )
public abstract class FileMetadata {

	/**
	 * The set of tags associated with this file
	 */
	private Set<Tag> tags;

	/**
	 * The full path name of the file
	 */
	private String pathToFile;

	/**
	 * The date of the last time the file was opened
	 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastOpened;

	/**
	 * The number of the times the file has been opened
	 */
	private int openedCount;
	
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
	 * Get the file contents
	 * 
	 * @return contents The file contents
	 */
	public abstract FileContents getContents();
	
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
		return Collections.unmodifiableSet(tags);
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
	 * @throws Exception 
	 */
	//TODO change this to a more specific exception - Dan
	//yep, probably not throwing Exception
	public abstract int searchText(String searchTerm) throws Exception;
}
