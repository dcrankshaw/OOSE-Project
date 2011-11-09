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
// TODO Zach suggested we change the name of this class into SearchManager, to eliminate the confusion with MVC in grading
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
	 *
	 */
	public void searchText(List<FileMetadata> files, String searchTerm) throws Exception
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
		for(FileMetadata file: files)
		{	
			int freq = 0;
			try {
				freq = file.searchText(searchTerm);
			} catch (Exception e) {
				// TODO why does it have to be wrapped with try and catch??? 
				// Is that because the searchText method requires to throw Exception? -Cain
				// Yes - Dan
				e.printStackTrace();
				throw new Exception("Error in full text search");
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
		
		fireSearchResult(matchedFiles);
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
