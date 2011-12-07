package edu.jhu.cs.oose.biblio.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.jhu.cs.oose.biblio.gui.SearchMode;

public abstract class FilterSearchStrategy extends SearchStrategy {
	public FilterSearchStrategy(SearchMode mode, String name) {
		super(mode, name);
	}
	public abstract void search(SearchManager manager, Set<Tag> tags);
	
	private static Map<SearchMode, FilterSearchStrategy> filterStrategies = makeFilterStrategies();

	private static Map<SearchMode, FilterSearchStrategy> makeFilterStrategies() {
		Map<SearchMode, FilterSearchStrategy> result = new HashMap<SearchMode, FilterSearchStrategy>();
		result.put(SearchMode.TAGS, new FilterFilesSearchStrategy());
		result.put(SearchMode.FILTER, new FilterBookmarksSearchStrategy());
		return result;
	}
	
	public static FilterSearchStrategy getStrategy(SearchMode mode) {
		return filterStrategies.get(mode);
	}
	
	private static class FilterFilesSearchStrategy extends FilterSearchStrategy {
		
		public FilterFilesSearchStrategy() {
			super(SearchMode.FILTER, "Filter Files by Tags");
		}
		
		@Override
		public void search(SearchManager mananger, Set<Tag> tags) {
			mananger.filterByTags(tags);
		}
	}

	private static class FilterBookmarksSearchStrategy extends FilterSearchStrategy {
		private Set<Tag> tags;
		
		public FilterBookmarksSearchStrategy() {
			super(SearchMode.BOOKMARKS, "Filter Bookmarks by Tags");
		}
		
		@Override
		public void search(SearchManager mananger, Set<Tag> tags) {
			//mananger.filterByTags(tags);
		}
	}
}



