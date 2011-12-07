package edu.jhu.cs.oose.biblio.model;

import edu.jhu.cs.oose.biblio.gui.SearchMode;

public abstract class SearchStrategy {
	private SearchMode mode;
	private String name; 
	
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
}
