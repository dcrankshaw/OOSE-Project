package edu.jhu.cs.oose.biblio.model;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

/**
 * A set of information about a file
 * 
 */

public class FileMetadata {

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
	private Date lastOpened;

	/**
	 * The number of the times the file has been opened
	 */
	private int openedCount;

	/**
	 * Get the file contents
	 * 
	 * @return contents The file contents
	 */
	public FileContents getContents() {
		return null;
	}
	
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

}
