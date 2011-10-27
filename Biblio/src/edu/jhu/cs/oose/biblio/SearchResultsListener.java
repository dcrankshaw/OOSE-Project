package edu.jhu.cs.oose.biblio;

import java.util.List;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * When a search of the documents has been completed, this provides a list of the matching files.
 * The ordering of the files will reflect a priority ordering (either alphabetical or relevance
 * depending on the context)
 */

public abstract class SearchResultsListener {
	
	/**
	 * Called when a search is executed. Tells the owner of this SearchResultsListener
	 * to display the results of the search
	 * @param results The list of matching documents in order
	 */
	public void displayResults(List<FileMetadata> results){}
}
