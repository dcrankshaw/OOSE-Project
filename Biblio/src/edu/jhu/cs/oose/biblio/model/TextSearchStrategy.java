package edu.jhu.cs.oose.biblio.model;

import java.util.HashMap;
import java.util.Map;

import edu.jhu.cs.oose.biblio.gui.SearchMode;

/** A class that knows how to take text and do a search using that as input. */
public abstract class TextSearchStrategy extends SearchStrategy {
	/**
	 * Creates a new object that knows how to search from text.
	 * @param mode the enum identifying this method of searching
	 * @param name the name of this search method
	 */
	public TextSearchStrategy(SearchMode mode, String name) {
		super(mode, name);
	}
	
	/**
	 * Do the search for searchTemr using the given SearchManager
	 * @param manager the object that will actually perform the search
	 * @param searchTerm the query for which to search
	 */
	public abstract void search(SearchManager manager, String searchTerm);
	
	/** The map from enum to the object knowing how to do the search */
	private static Map<SearchMode, TextSearchStrategy> textStrategies = makeTextStrategies();
	
	/**
	 * Fills the map of identifiers to search objects
	 * @return a map of identifiers to search objects
	 */
	private static Map<SearchMode, TextSearchStrategy> makeTextStrategies() {
		Map<SearchMode, TextSearchStrategy> result = new HashMap<SearchMode, TextSearchStrategy>();
		result.put(SearchMode.TAGS, new TagTextSearchStrategy());
		result.put(SearchMode.FULLTEXT, new FullTextSearchStrategy());
		return result;
	}
	
	/**
	 * Returns the object knowing how to do the filtering given by mode
	 * @param mode the method of searching
	 * @return an object knowing how to filter that way, or null of mode is not a filtering mode
	 */
	public static TextSearchStrategy getStrategy(SearchMode mode) {
		return textStrategies.get(mode);
	}

	/** Class that knows how to search for the Tags with certain name */
	private static class TagTextSearchStrategy extends TextSearchStrategy {
		/** Creates a new object that knows how to find Tags based their names */
		public TagTextSearchStrategy() {
			super(SearchMode.TAGS, "Search Tags");
		}
		
		@Override
		public void search(SearchManager manager, String searchTerm) {
			manager.searchTags(searchTerm);
		}
		
	}

	/** Class that knows how to search the full text of the documents */
	private static class FullTextSearchStrategy extends TextSearchStrategy {
		/** Creates a new object that knows how to search the full text of the documents */
		public FullTextSearchStrategy() {
			super(SearchMode.FULLTEXT, "Search Full Text");
		}
		
		@Override
		public void search(SearchManager manager, String searchTerm) {
			manager.searchText(searchTerm);
		}
	}
}
