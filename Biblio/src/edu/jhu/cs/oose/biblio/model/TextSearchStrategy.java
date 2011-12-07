package edu.jhu.cs.oose.biblio.model;

import java.util.HashMap;
import java.util.Map;

import edu.jhu.cs.oose.biblio.gui.SearchMode;

public abstract class TextSearchStrategy extends SearchStrategy {
	public TextSearchStrategy(SearchMode mode, String name) {
		super(mode, name);
	}
	public abstract void search(SearchManager manager, String searchTerm);
	
	private static Map<SearchMode, TextSearchStrategy> textStrategies = makeTextStrategies();
	
	private static Map<SearchMode, TextSearchStrategy> makeTextStrategies() {
		Map<SearchMode, TextSearchStrategy> result = new HashMap<SearchMode, TextSearchStrategy>();
		result.put(SearchMode.TAGS, new TagTextSearchStrategy());
		result.put(SearchMode.FILTER, new FullTextSearchStrategy());
		return result;
	}
	
	public static TextSearchStrategy getStrategy(SearchMode mode) {
		return textStrategies.get(mode);
	}

	private static class TagTextSearchStrategy extends TextSearchStrategy {
		
		public TagTextSearchStrategy() {
			super(SearchMode.TAGS, "Search Tags");
		}
		
		@Override
		public void search(SearchManager manager, String searchTerm) {
			manager.searchTags(searchTerm);
		}
		
	}

	private static class FullTextSearchStrategy extends TextSearchStrategy {
				
		public FullTextSearchStrategy() {
			super(SearchMode.FULLTEXT, "Search Full Text");
		}
		
		@Override
		public void search(SearchManager manager, String searchTerm) {
			manager.searchText(searchTerm);
		}
		
	}
}
