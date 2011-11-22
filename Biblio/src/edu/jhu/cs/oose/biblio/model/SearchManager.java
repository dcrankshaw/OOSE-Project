package edu.jhu.cs.oose.biblio.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;

/**
 * Controls all of the searching logic. It gets a search term and mode from the
 * SearchPanel and actually executes the search. It then provides the results to
 * its listeners
 */
public class SearchManager {

	/** Listeners that should be notified when a search returns files. */
	private Set<SearchResultsListener> resultsListeners;
	/** Listeners that should be notified when a search returns tags. */
	private Set<SearchTagsListener> tagListeners;
	/** The files that will be searched when full-text search is done. */
	private List<FileMetadata> selectedFiles;
	// TODO global??
	SessionFactory sessionFactory;

	public SearchManager() {
		sessionFactory = new Configuration().configure().buildSessionFactory();
		resultsListeners = new HashSet<SearchResultsListener>();
		tagListeners = new HashSet<SearchTagsListener>();
		selectedFiles = new ArrayList<FileMetadata>();
	}

	// Constructor just for testing purposes
	/**
	 * Creates a SearchManager that will search the given files. This is useful
	 * for our testing.
	 * 
	 * @param files
	 *            the files the Manager should search
	 */
	public SearchManager(List<FileMetadata> files) {
		sessionFactory = new Configuration().configure().buildSessionFactory();
		resultsListeners = new HashSet<SearchResultsListener>();
		tagListeners = new HashSet<SearchTagsListener>();
		selectedFiles = files;
		// TODO: how to instantiate an EntityManagerFactory?
		// entityManagerFactory = new EntityManagerFactory();
	}

	/** Fire the file search result to each listener. */
	private void fireSearchResult() {
		for (SearchResultsListener r : resultsListeners) {
			r.displayResults(selectedFiles);
		}
	}

	/**
	 * Fire the tag search result to each listener
	 * 
	 * @param matches
	 *            the tags that matched the original query
	 */
	private void fireSearchTags(List<Tag> matches) {
		for (SearchTagsListener t : tagListeners) {
			t.matchedTags(matches);
		}
	}

	/**
	 * Select all files tagged with this entire set of tags or any child tags of
	 * these tags
	 * 
	 * @param tags
	 *            the tags to filter by
	 */
	public void filterByTags(Set<Tag> tags) {
		// TODO this method is a little complicated. It will be implemented in
		// the next iteration
		Collection<String> tagNames = new ArrayList<String>();
		for (Tag tag : tags) {
			tagNames.add(tag.getName());
		}

		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(Tag.class);
		crit.add(Restrictions.in("name", tagNames));
		crit.setFetchMode("Tag_child", FetchMode.JOIN);

		fireSearchResult();
		/**
		 * from tag_child c where c.parent_name in t.name
		 */
	}

	/**
	 * Conducts a search of all of the tags for the ones that match the query
	 * string
	 * 
	 * @param searchTerm
	 *            the string to match against tag names
	 */
	public void searchTags(String searchTerm) {
		/*
		 * EntityManager entityManager =
		 * entityManagerFactory.createEntityManager();
		 * entityManager.getTransaction().begin(); List<Tag> searchResults =
		 * (List<Tag>) entityManager.createQuery( "select tt, distinct ft from "
		 * +
		 * "(Select distinct t FROM Tag t join Tag_child c where t.name like \"%"
		 * + searchTerm + "%\" and c.parent_name = t.name) tt " +
		 * "JOIN tag_file f ON f.tag = tt.tag JOIN file_table ft ON ft.name = f.file"
		 * ).getResultList();
		 * 
		 * entityManager.close();
		 */

		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		Criteria crit = session.createCriteria(Tag.class).add(
				Restrictions.like("name", "%" + searchTerm + "%"));
		@SuppressWarnings("unchecked")
		List<Tag> results = (List<Tag>) crit.list();
		session.getTransaction().commit();
		fireSearchTags(results);
	}

	/**
	 * Conducts a search of the full text of each document.
	 * 
	 * @param searchTerm
	 *            the text to find in the document.
	 */
	public void searchText(String searchTerm) {
		/* ***********************************************************************************
		 * We need to figure out a way (or if we even need to) normalize our
		 * search results that doesn't automatically give higher precedence to
		 * longer documents - Dan
		 * 
		 * Also, eventually searching can be done in a separate thread(s)
		 */
		List<ResultsPair> pairs = new ArrayList<ResultsPair>();
		for (FileMetadata file : selectedFiles) {
			int freq = 0;
			try {
				freq = file.searchText(searchTerm);
			} catch (Exception e) {

				e.printStackTrace();
				// TODO: maybe launch a dialog warning about a corrupted file -
				// Dan
			}
			if (freq != 0) {
				// remove all files with 0 occurrences
				pairs.add(new ResultsPair(freq, file));
			}
		}

		Collections.sort(pairs);
		List<FileMetadata> matchedFiles = new ArrayList<FileMetadata>();

		for (ResultsPair pair : pairs) {
			matchedFiles.add(pair.file);
		}
		selectedFiles = matchedFiles;

		fireSearchResult();
	}

	/**
	 * A Comparable object to store the results of each file's search. The
	 * object allows for sorting based on the relevance of each search results,
	 * so we can provide a list of search results ordered by relevance.
	 */
	private class ResultsPair implements Comparable<ResultsPair> {
		/** The number of times this result was found in the file. */
		private int occurrences;
		/** The file that was searched. */
		private FileMetadata file;

		/**
		 * Creates a new pair of the occurrences of the search term in this file
		 * 
		 * @param d
		 *            the number of occurrences of the search term
		 * @param fl
		 *            the file that was searched
		 */
		private ResultsPair(int d, FileMetadata fl) {
			file = fl;
			occurrences = d;
		}

		@Override
		public int compareTo(ResultsPair temp) {

			// TODO: the more occurrences a pair has, the closer to the front of
			// the list (and therefore the "smaller")
			// it should be, double check that this does that - Dan
			if (this.occurrences > temp.occurrences) {
				return -1;
			} else if (this.occurrences == temp.occurrences) {
				return 0;
			} else
				return 1;
		}

	}
	
	//Accidentally did this...
	//TODO not sure if the interaction with database is correct. Please complete the testing if you decide to use it.
	/**
	 * Search bookmarks tagged by the same tag. 
	 * 
	 * @param tagName
	 */
	@SuppressWarnings("unchecked")
	public void searchBookmark(String tagName) {

		Collection<Bookmark> bkmarks = new ArrayList<Bookmark>();
		Session session = sessionFactory.getCurrentSession();
		bkmarks = (ArrayList<Bookmark>) session.createQuery("from Bookmark").list();
		boolean match = false;
		for (Bookmark bk : bkmarks) {
			for (Tag bkTag : bk.getTags()) {
				if (bkTag.getName() == tagName) {
					match = true;
				}
			}
			if (match) {
				selectedFiles.add(bk.getFile());
			}
		}
		fireSearchResult();
	}
	
	
	/**
	 * Search tags of based on Category
	 * @param term
	 */
	@SuppressWarnings("unchecked")
	public void searchCategory(String term) {
		String[] temp;
		String catName = null;
		String tagName = null;
		List<Category> cats = new ArrayList<Category>();
		List<Tag> selectedTags = new ArrayList<Tag>();
		List<Tag> results = new ArrayList<Tag>();
		
		for (int i = 0; i < term.length(); i++)
			if (term.charAt(i) == ':') {
				temp = term.split(":");
				catName = temp[0];
				tagName = temp[1];break;
			}
		
		Session session = sessionFactory.getCurrentSession();
		session.beginTransaction();

		Criteria crit = session.createCriteria(Category.class).add(
				Restrictions.like("name", "%" + catName + "%"));
		cats = (List<Category>)crit.list();
		for (Category c : cats){
			selectedTags.addAll(c.getTag());
		}
		 
		session.getTransaction().commit();		
		
		for (Tag t : selectedTags){
			if ( t.getName() == tagName){
				results.add(t);
			}
		}
		if (results.isEmpty()){
			System.out.println("No Match Found.");
		}
		else{
			fireSearchTags(results);
		}
		
	}

	/**
	 * Adds an object that wants to know about files that are found.
	 * 
	 * @param listener
	 *            an object that wants to know about files that are found.
	 */
	public void addResultsListener(SearchResultsListener listener) {
		resultsListeners.add(listener);
		listener.displayResults(selectedFiles);

	}

	/**
	 * Stops sending search results made of files to the given object.
	 * 
	 * @param listener
	 *            the object that wants to stop getting notifications.
	 */
	public void removeResultsListener(SearchResultsListener listener) {
		resultsListeners.remove(listener);
	}

	/**
	 * Adds an object that wants to know about tags that are found.
	 * 
	 * @param listener
	 *            an object that wants to know about tags that are found.
	 */
	public void addTagsListener(SearchTagsListener listener) {
		tagListeners.add(listener);
	}

	/**
	 * Stops sending search results made of tags to the given object.
	 * 
	 * @param listener
	 *            the object that wants to stop getting notifications.
	 */
	public void removeTagsListener(SearchTagsListener listener) {
		tagListeners.remove(listener);
	}
}
