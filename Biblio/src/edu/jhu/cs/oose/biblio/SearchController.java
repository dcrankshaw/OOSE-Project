package edu.jhu.cs.oose.biblio;

import edu.jhu.cs.oose.biblio.gui.SearchPanel;

/**
 * Controls all of the searching logic. It gets a search term and mode from the SearchPanel
 * and actually executes the search. It then provides the results to its listeners
 */

public class SearchController {

	/** The UI for the user to enter a search term */
	public SearchPanel queryInterface;
	
	/** An interface to display all of the search results */
	public SearchPanel results;
	
	/** Conducts a search of all of the tags */
	public void searchTags(){}
	
	/** Conducts a search of the full text of each document */
	public void searchText()
	{
		/*
		 * - get list of possible files to search (all of the files matching current tags filter)
		 * - for each file, conduct a textSearch(FileTextContents file, String searchTerm) on it
		 * - this will return an int indicating how well the contents match the search term
		 * - all files above a certain match threshold will be displayed ranked by how well they match
		 * 		--not in this iteration, but searching these could/should be done in separate threads
		 */
		
		/************************************************************************************
		 * 
		 * Things to Decide:
		 * -what string searching/matching algorithm to use
		 * 		-presumably this will lead to some sort of scoring algorithm for how well the text matches
		 * 		- this algorithm needs to take into account documents of differing length, so it can't be
		 * 		  absolute number of matches, must be normalized in some way
		 * 
		 * 
		 * 
		 */
		
		
		
		
	}
	
}
