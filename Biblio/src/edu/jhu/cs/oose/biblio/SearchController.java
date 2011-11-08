package edu.jhu.cs.oose.biblio;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import edu.jhu.cs.oose.biblio.gui.SearchPanel;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * Controls all of the searching logic. It gets a search term and mode from the SearchPanel
 * and actually executes the search. It then provides the results to its listeners
 */

public class SearchController {

	private Set<SearchResultsListener> resultsListeners;
	private Set<SearchTagsListener> tagListeners;
	
	/**
	 * Fire the file search result to each listener
	 * @param results
	 */
	private void fireSearchResult(List<FileMetadata> results){
		for (SearchResultsListener r : resultsListeners){
			r.displayResults(results);
		}
	}
	
	/**
	 * Fire the tag search result to each listener
	 * @param matches 
	 */
	private void fireSearchTags(Set<Tag> matches){
		for (SearchTagsListener t : tagListeners){
			t.matchedTags(matches);
		}
	}
	
	/** The UI for the user to enter a search term */
	public SearchPanel queryInterface;
	
	public SearchController() {
		resultsListeners = new HashSet<SearchResultsListener>();
		tagListeners = new HashSet<SearchTagsListener>();
	}
	
	/** Conducts a search of all of the tags */
	public void searchTags(EntityManagerFactory entityManagerFactory, String searchTerm)
	{
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		List<Tag> searchResults = (List<Tag>) entityManager.createQuery(
				"select tt, distinct ft from " +
				"(Select distinct t FROM Tag t join Tag_child c where t.name like \"%" + searchTerm + "%\" and c.parent_name = t.name) tt "
				+ "JOIN tag_file f ON f.tag = tt.tag JOIN file_table ft ON ft.name = f.file").getResultList();
		

		
		
	}
	
	/** Conducts a search of the full text of each document 
	 * @return */
	public List<ResultsPair> searchText(List<FileMetadata> files, String searchTerm)
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
		 * 
		 */
		
		/**
		 * 
		 */
		List<ResultsPair> pairs = new ArrayList<ResultsPair>();
		for(FileMetadata file: files)
		{	
			int freq = 0;
			try {
				freq = file.searchText(searchTerm);
			} catch (Exception e) {
				// TODO why does it have to be wrapped with try and catch??? 
				// Is that because the searchText method requires to throw Exception? -Cain
				e.printStackTrace();
			}
			if(freq != 0){
				//remove all files with 0 occurrences
				pairs.add(new ResultsPair(freq, file){});	
			}
		}
		Collections.sort(pairs);
		return pairs;
		
		
	}
	
	/**
	 * The comparable class that stores a tuple of <int, FileMetadata>
	 * Enable sort() in Collection utility class
	 *
	 */
	private class ResultsPair implements Comparable<ResultsPair>
	{
		private int freq;
		private FileMetadata file;
		
		private ResultsPair(int fq, FileMetadata fl){
			file = fl;
			freq = fq;			
		}

		@Override
		//TODO why public??
		public int compareTo(ResultsPair temp) {
			if( temp.freq >= this.freq ){
				return 0; //TODO need to check if it is sorting in the right order
				}
			else {
				return 1;
			}
		}
		
	}
	
	public void addResultsListener(SearchResultsListener list) {
		resultsListeners.add(list);
	}
	
	public void removeResultsListener(SearchResultsListener list) {
		resultsListeners.remove(list);
	}
	
	public void addTagsListener(SearchTagsListener list) {
		tagListeners.add(list);
	}
	
	public void removeTagsListener(SearchTagsListener list) {
		tagListeners.remove(list);
	}
}
