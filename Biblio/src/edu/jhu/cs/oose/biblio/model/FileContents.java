package edu.jhu.cs.oose.biblio.model;

/**
*An interface for interacting with the contents of a file, contrast with the metadata.
*/
public interface FileContents {

	/**
	*Search the content of the file to rank its relevance that helps locate the
	*file.
	*@param searchTerm The word
	*@return relevance An integer that indicates how relevant this file is to the
	*given search term.(potentially how many times the keyword appears in the file)
	*/
	public int search(String searchTerm);

}
