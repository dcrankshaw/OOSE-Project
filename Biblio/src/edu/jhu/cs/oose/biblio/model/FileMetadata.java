package edu.jhu.cs.oose.biblio.model;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.jpedal.exception.PdfException;

@Entity
@Table( name = "FILEMETADATA" )
/**
 * A set of information about a file
 * 
 */

public abstract class FileMetadata {

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
	
	public boolean equals(FileMetadata otherFile)
	{
		return otherFile.getPathToFile().equals(this.pathToFile);
	}

	public Set<Tag> getTags() {
		return (Set<Tag>) Collections.unmodifiableCollection(tags);
	}

	public String getPathToFile() {
		return pathToFile;
	}

	public void setPathToFile(String pathToFile) {
		this.pathToFile = pathToFile;
	}

	public Date getLastOpened() {
		return lastOpened;
	}

	public void setLastOpened(Date lastOpened) {
		this.lastOpened = lastOpened;
	}

	public int getOpenedCount() {
		return openedCount;
	}

	public void setOpenedCount(int openedCount) {
		this.openedCount = openedCount;
	}

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
