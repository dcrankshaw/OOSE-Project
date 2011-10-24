package edu.jhu.cs.oose.biblio.model;

import java.util.Date;
import java.util.Set;

/**
 * A set of information about a file
 * 
 */

public class FileMetadata {
	
	/**
	 * The set of tags associated with this file
	 */
	public Set<Tag> tags;
	
	/**
	 * The full path name of the file
	 */
	public String pathToFile;
	
	/**  
	 * The date of the last time the file was opened
	 */
	public Date lastOpened;

	/**
	 * The number of the times the file has been opened
	 */
	public int openedCount;
	
	/**
	 * Get the file contents
	 * @return contents The file contents
	 */
	public FileContents getContents(){}
	
}
