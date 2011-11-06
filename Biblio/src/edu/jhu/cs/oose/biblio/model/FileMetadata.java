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
	 * The kind of file this is.
	 * TODO I (Paul) think we can do this with inheritance instead.
	 */
	private FileTypes type;

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
		// TODO I just put this here to make it compile...
		this.type = FileTypes.PDF;
	}
	
	/**
	 * Creates a new FileMetadata with all its fields set to the given arguments
	 * @param date the last time this document was opened
	 * @param timesOpened the number of times this was opened 
	 * @param path the path to the file contents residing on disk
	 * @param fileTags the tags to be applied to this file (This is NOT copied)
	 * @param fileType (Paul - not quite sure)
	 */
	public FileMetadata(Date date, int timesOpened, String path, Set<Tag> fileTags, FileTypes fileType) {
		this.lastOpened = date;
		this.openedCount = timesOpened;
		this.pathToFile = path;
		this.tags = fileTags;
		this.type = fileType;
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
		return (Set<Tag>) Collections.unmodifiableCollection(tags);
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
	 * Returns the type of this file.
	 * @return the type of this file.
	 */
	public FileTypes getType() {
		return this.type;
	}
	
	/**
	 * Searches the associated FileContents for the given search term
	 * @param searchTerm the text to search for
	 * @return the number of the times the term occurs
	 * @throws Exception 
	 */
	//TODO change this to a more specific exception - Dan
	public abstract int searchText(String searchTerm) throws Exception;

}
