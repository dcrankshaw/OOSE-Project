package edu.jhu.cs.oose.biblio.model;

/**
* An interface that sets the metadata for a file.
*/
public abstract class FileMetadata {

	/**
	* The tagging of the file.
	*/
	public Set<Tag> tags;

	/**
	* The directory path of the file.
	*/
	public String pathToFile;

	/**
	* The date of last time the file was opened.
	*/
	public Date lastOpened;

	/**
	* The number of times the file is opened.
	*/
	public int openedCount;

	/**
	* Get the stored information of the file.
	* @return contents The information add to the file.
	*/
	public FileContents getContents(){
	}

}

