package edu.jhu.cs.oose.biblio.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.jhu.cs.oose.biblio.gui.SearchMode;

/** A class that knows how to find the results tagged with certain Tags. */
public abstract class FilterSearchStrategy extends SearchStrategy {
	
	/**
	 * Creates a new search object with the given identifier and name.
	 * @param mode the enum identifying this search strategy
	 * @param name the name of this search strategy
	 */
	public FilterSearchStrategy(SearchMode mode, String name) {
		super(mode, name);
	}
	
	/**
	 * Calls the correct method on the manager to filter the
	 * results by the given set of Tags
	 * @param manager the manager to conduct the search
	 * @param tags the set of tags to use for filtering
	 */
	public abstract void search(SearchManager manager, Set<Tag> tags);
	
	/** A map from identifiers to actual search objects. */
	private static Map<SearchMode, FilterSearchStrategy> filterStrategies = makeFilterStrategies();

	/**
	 * Fills the map of identifiers to search objects
	 * @return a map of identifiers to search objects
	 */
	private static Map<SearchMode, FilterSearchStrategy> makeFilterStrategies() {
		Map<SearchMode, FilterSearchStrategy> result = new HashMap<SearchMode, FilterSearchStrategy>();
		result.put(SearchMode.FILTER, new FilterFilesSearchStrategy());
		result.put(SearchMode.BOOKMARKS, new FilterBookmarksSearchStrategy());
		return result;
	}
	
	/**
	 * Returns the object knowing how to do the search given by mode
	 * @param mode the method of searching
	 * @return an object knowing how to filter that way
	 */
	public static FilterSearchStrategy getStrategy(SearchMode mode) {
		return filterStrategies.get(mode);
	}
	
	/** Class that knows how to search for the files tagged with certain Tags */
	private static class FilterFilesSearchStrategy extends FilterSearchStrategy {
		/** Creates a new object that knows how to filter for files */
		public FilterFilesSearchStrategy() {
			super(SearchMode.FILTER, "Filter Files by Tags");
		}
		
		@Override
		public void search(SearchManager mananger, Set<Tag> tags) {
			mananger.filterByTags(tags);
		}
	}

	/** Class that knows how to search for the bookmarks tagged with certain Tags */
	private static class FilterBookmarksSearchStrategy extends FilterSearchStrategy {
		/** Creates a new object that knows how to filter for Bookmarks */
		public FilterBookmarksSearchStrategy() {
			super(SearchMode.BOOKMARKS, "Filter Bookmarks by Tags");
		}
		
		@Override
		public void search(SearchManager mananger, Set<Tag> tags) {
			// TODO
			//mananger.filterByTags(tags);
		}
	}
}
