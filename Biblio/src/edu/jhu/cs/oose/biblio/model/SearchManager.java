package edu.jhu.cs.oose.biblio.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import edu.jhu.cs.oose.biblio.gui.SearchPanel;

/**
 * Controls all of the searching logic. It gets a search term and mode from the SearchPanel
 * and actually executes the search. It then provides the results to its listeners
 */
public class SearchManager {

	private Set<SearchResultsListener> resultsListeners;
	private Set<SearchTagsListener> tagListeners;
	private List<FileMetadata> selectedFiles;
	
	EntityManagerFactory entityManagerFactory;
	
	
	
	/** The UI for the user to enter a search term */
	public SearchPanel queryInterface; //TODO add a listener to this in our constructor - Dan
	
	public SearchManager() {
		resultsListeners = new HashSet<SearchResultsListener>();
		tagListeners = new HashSet<SearchTagsListener>();
		selectedFiles = new ArrayList<FileMetadata>();
		//TODO: how to instantiate an EntityManagerFactory? 
		//entityManagerFactory = new EntityManagerFactory();
	}
	
	//Constructor just for testing purposes
	public SearchManager(List<FileMetadata> files) {
		resultsListeners = new HashSet<SearchResultsListener>();
		tagListeners = new HashSet<SearchTagsListener>();
		selectedFiles = files;
		//TODO: how to instantiate an EntityManagerFactory? 
		//entityManagerFactory = new EntityManagerFactory();
	}
	
	
	/**
	 * Fire the file search result to each listener
	 * @param results
	 */
	private void fireSearchResult(){
		for (SearchResultsListener r : resultsListeners){
			r.displayResults(selectedFiles);
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
	
	public void filterByTags(List<Tag> tags)
	{
		
	}
	
	/** Conducts a search of all of the tags */
	public void searchTags(String searchTerm)
	{
		EntityManager entityManager = entityManagerFactory.createEntityManager();
		entityManager.getTransaction().begin();
		List<Tag> searchResults = (List<Tag>) entityManager.createQuery(
				"select tt, distinct ft from " +
				"(Select distinct t FROM Tag t join Tag_child c where t.name like \"%" + searchTerm + "%\" and c.parent_name = t.name) tt "
				+ "JOIN tag_file f ON f.tag = tt.tag JOIN file_table ft ON ft.name = f.file").getResultList();
		

		
		
	}
	
	/** Conducts a search of the full text of each document 
	 *
	 */
	public void searchText(String searchTerm)
	{
		
		
		
		
		/************************************************************************************
		 * We need to figure out a way (or if we even need to) normalize our search
		 * results that doesn't automatically give higher precedence to longer documents - Dan
		 * 
		 * Also, eventually searching can be done in a separate thread(s)
		 *
		 */
		
		/**
		 * 
		 */
		List<ResultsPair> pairs = new ArrayList<ResultsPair>();
		for(FileMetadata file: selectedFiles)
		{	
			int freq = 0;
			try {
				freq = file.searchText(searchTerm);
			} catch (Exception e) {
				// TODO why does it have to be wrapped with try and catch??? 
				// Is that because the searchText method requires to throw Exception? -Cain
				// Yes - Dan
				e.printStackTrace();
				//TODO: maybe launch a dialog warning about a corrupted file - Dan
			}
			if(freq != 0){
				//remove all files with 0 occurrences
				pairs.add(new ResultsPair(freq, file){});	
			}
		}
		
		Collections.sort(pairs);
		List<FileMetadata> matchedFiles = new ArrayList<FileMetadata>();
		
		for(ResultsPair pair: pairs)
		{
			matchedFiles.add(pair.file);
		}
		selectedFiles = matchedFiles;
		
		fireSearchResult();
	}
	
	/**
	 * A Comparable object to store the results of each file's search. The object
	 * allows for sorting based on the relevance of each search results, so we
	 * can provide a list of search results ordered by relevance.
	 *
	 */
	private class ResultsPair implements Comparable<ResultsPair>
	{
		private int occurrences;
		private FileMetadata file;
		
		private ResultsPair(int d, FileMetadata fl){
			file = fl;
			occurrences = d;			
		}

		@Override
		public int compareTo(ResultsPair temp) {

			//TODO: the more occurrences a pair has, the closer to the front of the list (and therefore the "smaller")
			// it should be, double check that this does that - Dan
			if( this.occurrences > temp.occurrences ){
				return -1; 
				}
			else if (this.occurrences == temp.occurrences) {
				return 0;
			}
			else return 1;
		}
		
	}
	
	public void addResultsListener(SearchResultsListener listener) {
		resultsListeners.add(listener);
		listener.displayResults(selectedFiles);
		
	}
	
	public void removeResultsListener(SearchResultsListener listener) {
		resultsListeners.remove(listener);
	}
	
	public void addTagsListener(SearchTagsListener listener) {
		tagListeners.add(listener);
	}
	
	public void removeTagsListener(SearchTagsListener listener) {
		tagListeners.remove(listener);
	}
}
