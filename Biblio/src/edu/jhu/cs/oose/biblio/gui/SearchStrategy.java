package edu.jhu.cs.oose.biblio.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.jhu.cs.oose.biblio.model.SearchManager;
import edu.jhu.cs.oose.biblio.model.Tag;

public abstract class SearchStrategy {
	private SearchMode mode;
	private String name; 
	public abstract void search(SearchManager manager);
	
	public SearchStrategy(SearchMode mode, String name) {
		this.name = name;
		this.mode = mode;
	}
	
	public SearchMode getMode() {
		return this.mode;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setSearchTerm(String searchTerm) { }
	public void setTags(Set<Tag> tags) { }
	
	private static Map<SearchMode, SearchStrategy> strategies = makeStrategies();
	
	private static Map<SearchMode, SearchStrategy> makeStrategies() {
		Map<SearchMode, SearchStrategy> result = new HashMap<SearchMode, SearchStrategy>();
		result.put(SearchMode.TAGS, new TagSearchStrategy());
		result.put(SearchMode.FILTER, new FilterFilesSearchStrategy());
		result.put(SearchMode.FULLTEXT, new TextSearchStrategy());
		result.put(SearchMode.BOOKMARKS, new FilterBookmarksSearchStrategy());
		return result;
	}
	
	public static SearchStrategy getStrategy(SearchMode mode) {
		return strategies.get(mode);
	}
	
	private static class TagSearchStrategy extends SearchStrategy {
		
		private String searchTerm;
		
		TagSearchStrategy() {
			super(SearchMode.TAGS, "Search Tags");
		}
		
		@Override
		public void setSearchTerm(String searchTerm) {
			this.searchTerm = searchTerm;
		}
		
		@Override
		public void search(SearchManager manager) {
			manager.searchTags(searchTerm);
		}
		
	}

	private static class TextSearchStrategy extends SearchStrategy {
		
		private String searchTerm;
		
		TextSearchStrategy() {
			super(SearchMode.FULLTEXT, "Search Full Text");
		}
		
		@Override
		public void setSearchTerm(String searchTerm) {
			this.searchTerm = searchTerm;
		}
		
		@Override
		public void search(SearchManager manager) {
			manager.searchText(searchTerm);
		}
		
	}
	
	private static class FilterFilesSearchStrategy extends SearchStrategy {
		private Set<Tag> tags;
		
		public FilterFilesSearchStrategy() {
			super(SearchMode.FILTER, "Filter Files by Tags");
		}
		
		@Override
		public void setTags(Set<Tag> tags) {
			this.tags = tags;
		}
		
		@Override
		public void search(SearchManager mananger) {
			mananger.filterByTags(tags);
		}
	}

	private static class FilterBookmarksSearchStrategy extends SearchStrategy {
		private Set<Tag> tags;
		
		public FilterBookmarksSearchStrategy() {
			super(SearchMode.BOOKMARKS, "Filter Bookmarks by Tags");
		}
		
		@Override
		public void setTags(Set<Tag> tags) {
			this.tags = tags;
		}
		
		@Override
		public void search(SearchManager mananger) {
			//mananger.filterByTags(tags);
		}
	}
}
