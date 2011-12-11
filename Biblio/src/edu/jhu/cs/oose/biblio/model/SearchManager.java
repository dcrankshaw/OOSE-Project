package edu.jhu.cs.oose.biblio.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import nl.siegmann.epublib.domain.Book;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 * Controls all of the searching logic. It gets a search term and mode from the
 * SearchPanel and actually executes the search. It then provides the results to
 * its listeners
 */
public class SearchManager {

	/** Listeners that should be notified when a search returns files. */
	private Set<SearchResultsListener> resultsListeners;
	
	/** Listeners that should be notified when a search returns bookmarks. */
	private Set<BookmarkSearchResultsListener> bookmarkResultsListeners;
	/** Listeners that should be notified when a search returns tags. */
	private Set<SearchTagsListener> tagListeners;
	/** The files that will be searched when full-text search is done. */
	private List<FileMetadata> selectedFiles;
	/** The tags that will be used for filtering */
	private List<Tag> tagResults;
	
	private List<Bookmark> selectedBookmarks;

	/** Creates a new SearchManager for searching the database */
	public SearchManager() {
		resultsListeners = new HashSet<SearchResultsListener>();
		tagListeners = new HashSet<SearchTagsListener>();
		bookmarkResultsListeners = new HashSet<BookmarkSearchResultsListener>();
		selectedFiles = new ArrayList<FileMetadata>();
		selectedBookmarks = new ArrayList<Bookmark>();
		searchTags("");
		filterBookmarksByTags(null);
		filterByTags(null);
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
		this();
		selectedFiles.addAll(files);
	}

	/** Fire the file search result to each listener. */
	private void fireSearchResult() {
		for (SearchResultsListener r : resultsListeners) {
			r.displayResults(selectedFiles);
		}
	}
	
	/** Fire the bookmark search results to each listener. */
	private void fireBookmarkSearchResult() {
		for (BookmarkSearchResultsListener r : bookmarkResultsListeners) {
			r.displayResults(selectedBookmarks);
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

		selectedFiles.clear();
		if (tags != null && !tags.isEmpty()) {
			List<Set<FileMetadata>> taggedFiles = new ArrayList<Set<FileMetadata>>();
			Set<FileMetadata> filteredFiles = new HashSet<FileMetadata>();
			for (Tag currentTag : tags) {

				Set<Tag> currentChildren = currentTag.getAllDescendants();
				Set<FileMetadata> currentTaggedFiles = new HashSet<FileMetadata>();
				currentTaggedFiles.addAll(currentTag.getTaggedFiles());
				for (Tag currentChild : currentChildren) {
					currentTaggedFiles.addAll(currentChild.getTaggedFiles());
				}
				taggedFiles.add(currentTaggedFiles);
			}
			// get the list of files matching the first tag or its descendants
			filteredFiles.addAll(taggedFiles.get(0));
			// take the intersection of those files with the files matching the
			// rest of the tags
			for (int i = 1; i < taggedFiles.size(); i++) {
				filteredFiles.retainAll(taggedFiles.get(i));
			}
			selectedFiles.addAll(filteredFiles);
			Collections.sort(selectedFiles, new Comparator<FileMetadata>() {
				@Override
				public int compare(FileMetadata a, FileMetadata b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
		}
		// if no tags are specified, then match everything
		else {
			Session session = Database.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			
			/*Criteria crit = session.createCriteria(FileMetadata.class);*/
			@SuppressWarnings("unchecked")
			Database<FileMetadata> db = (Database<FileMetadata>)Database.get(FileMetadata.class);
			Query query = session.createQuery("from FileMetadata");
			selectedFiles = db.executeQuery(query);
			Collections.sort(selectedFiles, new Comparator<FileMetadata>() {
				@Override
				public int compare(FileMetadata a, FileMetadata b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
		}
		fireSearchResult();
	}
	
	/**
	 * Conducts a search of all of the tags for the ones that match the query
	 * string
	 * 
	 * @param searchTerm
	 *            the string to match against tag names
	 */
	public void searchTags(String searchTerm) {
		searchTerm = searchTerm.toLowerCase();
		if (searchTerm.contains(":"))
			searchCategory(searchTerm);
		else {
			Session session = Database.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			//Query searchQuery = session.createQuery("from Tag where :term like name.lower()");
			Query searchQuery = session.createQuery("from Tag where lower(name) like :term");
			searchQuery.setString("term", "%" + searchTerm + "%");
			
			/*
			Criteria crit = session.createCriteria(Tag.class).add(
					Restrictions.like("name", "%" + searchTerm + "%"));*/
			@SuppressWarnings("unchecked")
			Database<Tag> db = (Database<Tag>)Database.get(Tag.class);
			tagResults = db.executeQuery(searchQuery);
			session.getTransaction().commit();
			fireSearchTags(tagResults);
		}
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
		public ResultsPair(int d, FileMetadata fl) {
			file = fl;
			occurrences = d;
		}

		@Override
		public int compareTo(ResultsPair temp) {

			if (this.occurrences > temp.occurrences) {
				return -1;
			} else if (this.occurrences == temp.occurrences) {
				return 0;
			} else
				return 1;
		}

	}
	
	/**
	 * Similar to filtering files by tags, but searches for bookmarks instead
	 * @param tags The tags to filter by (selected from prior tag searches)
	 */
	
	public void filterBookmarksByTags(Set<Tag> tags) {

		selectedBookmarks.clear();
		if (tags != null && !tags.isEmpty()) {
			List<Set<Bookmark>> taggedBookmarks = new ArrayList<Set<Bookmark>>();
			Set<Bookmark> filteredBookmarks = new HashSet<Bookmark>();
			for (Tag currentTag : tags) {

				Set<Tag> currentChildren = currentTag.getAllDescendants();
				Set<Bookmark> currentTaggedBookmarks = new HashSet<Bookmark>();
				currentTaggedBookmarks.addAll(currentTag.getTaggedBookmarks());
				for (Tag currentChild : currentChildren) {
					currentTaggedBookmarks.addAll(currentChild.getTaggedBookmarks());
				}
				taggedBookmarks.add(currentTaggedBookmarks);
			}
			// get the list of bookmarks matching the first tag or its descendants
			filteredBookmarks.addAll(taggedBookmarks.get(0));
			// take the intersection of those bookmarks with the bookmarks matching the
			// rest of the tags
			for (int i = 1; i < taggedBookmarks.size(); i++) {
				filteredBookmarks.retainAll(taggedBookmarks.get(i));
			}
			selectedBookmarks.addAll(filteredBookmarks);
			Collections.sort(selectedBookmarks, new Comparator<Bookmark>() {
				@Override
				public int compare(Bookmark a, Bookmark b) {
					if(a.getFile().getName().compareTo(b.getFile().getName()) == 0) {
						if(a.getLocation().getPercentageOfFile() < b.getLocation().getPercentageOfFile()) {
							return 1;
						}
						else if(a.getLocation().getPercentageOfFile() == b.getLocation().getPercentageOfFile()) {
							return 0;
						}
						else {
							return -1;
						}	
					}
					else {
						return a.getFile().getName().compareTo(b.getFile().getName());
					}
				}
			});
		}
		// if no tags are specified, then match everything
		else {
			Session session = Database.getSessionFactory().getCurrentSession();
			session.beginTransaction();
			Query q = session.createQuery("from Bookmark");
			@SuppressWarnings("unchecked")
			Database<Bookmark> db = (Database<Bookmark>)Database.get(Bookmark.class);
			selectedBookmarks = db.executeQuery(q);
			Collections.sort(selectedBookmarks, new Comparator<Bookmark>() {
				@Override
				public int compare(Bookmark a, Bookmark b) {
					if(a.getFile().getName().compareTo(b.getFile().getName()) == 0) {
						if(a.getLocation().getPercentageOfFile() < b.getLocation().getPercentageOfFile()) {
							return 1;
						}
						else if(a.getLocation().getPercentageOfFile() == b.getLocation().getPercentageOfFile()) {
							return 0;
						}
						else {
							return -1;
						}	
					}
					else {
						return a.getFile().getName().compareTo(b.getFile().getName());
					}
				}
			});
		}
		fireBookmarkSearchResult();
	}
	
	

	/**
	 * Search for tags matching the given search term in the given category
	 * provides a null result set on a malformed input (more than one colon in the search string)
	 * 
	 * @param term the search query string
	 */
	private void searchCategory(String term) {
		List<Tag> results = null;
		// only search if colon appears exactly once in searchterm
		if (term.indexOf(":") == term.lastIndexOf(":")) {
			
			Set<Tag> potentialTags = new TreeSet<Tag>();
			results = new ArrayList<Tag>();

			String[] split = term.split(":");
			//we have already verified that a colon appears exactly once in the searchTerm, so we
			//know that String[] split will have exactly two items in it
			String category = split[0].trim();
			String tagName = "";
			if(split.length > 1) {
				tagName = split[1].trim();
			}

			Session session = Database.getSessionFactory().getCurrentSession();
			session.beginTransaction();	
			//Query query = session.createQuery("from Category where :category like name.lower()");
			Query query = session.createQuery("from Category where lower(name) like :category");
			query.setString("category", "%" + category + "%");
			@SuppressWarnings("unchecked")
			List<Category> cats = ((Database<Category>)Database.get(Category.class)).executeQuery(query);
			session.getTransaction().commit();
			for (Category c : cats) {
				potentialTags.addAll(c.getTags());
			}
			for (Tag t : potentialTags) {
				if (t.getName().contains(tagName)) {
					results.add(t);
				}
			}
		}
		fireSearchTags(results);
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
	 * Stops sending bookmark search results to the given listener.
	 * 
	 * @param listener
	 *            the listener to remove
	 */
	public void removeBookmarkResultsListener(BookmarkSearchResultsListener listener) {
		bookmarkResultsListeners.remove(listener);
	}
	
	
	/**
	 * Adds an object that wants to know about bookmarks that are found after a search.
	 * 
	 * @param listener
	 *            an object that wants to know about bookmarks that are found.
	 */
	public void addBookmarkResultsListener(BookmarkSearchResultsListener listener) {
		bookmarkResultsListeners.add(listener);
		listener.displayResults(selectedBookmarks);
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
		this.fireSearchTags(tagResults);
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
