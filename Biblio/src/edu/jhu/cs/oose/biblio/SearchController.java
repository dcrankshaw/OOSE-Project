package edu.jhu.cs.oose.biblio;

/**
 * Controls all of the searching logic. It gets a search term and mode from the SearchPanel
 * and actually executes the search. It then provides the results to its listeners
 */

public class SearchController {

	/** The UI for the user to enter a search term */
	public SearchPanel queryInterface;
	
	/** An interface to display all of the search results */
	public SearchResultsPanel results;
	
	/** Conducts a search of all of the tags */
	public void searchTags(){}
	
	/** Conducts a search of the full text of each document */
	public void searchText() {}
	
}
