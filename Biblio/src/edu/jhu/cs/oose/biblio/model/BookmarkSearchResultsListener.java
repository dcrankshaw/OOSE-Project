package edu.jhu.cs.oose.biblio.model;

import java.util.List;

/**
 * When a search of the documents has been completed, this provides a list of the matching files.
 * The ordering of the files will reflect a priority ordering (either alphabetical or relevance
 * depending on the context)
 */
public interface BookmarkSearchResultsListener {
	
	/**
	 * Called when a search is executed. Tells the owner of this SearchResultsListener
	 * to display the results of the search
	 * @param results The list of matching documents in order
	 */
	public void displayResults(List<Bookmark> results);
}
