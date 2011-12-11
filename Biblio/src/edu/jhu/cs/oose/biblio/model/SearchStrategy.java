package edu.jhu.cs.oose.biblio.model;

import edu.jhu.cs.oose.biblio.gui.SearchMode;

/** An object that knows how to do a search. */
public abstract class SearchStrategy {
	/** The SearchMode that this strategy knows how to do/ */
	private SearchMode mode;
	/** The name of this way of searching. */
	private String name; 
	
	/**
	 * Creates a new strategy with the given name and enum identifier
	 * @param mode the enum identifying this method of searching
	 * @param name the name of this method of searching
	 */
	public SearchStrategy(SearchMode mode, String name) {
		this.name = name;
		this.mode = mode;
	}
	
	/**
	 * The enum identifying this search mode.
	 * @return the enum identifying this search mode
	 */
	public SearchMode getMode() {
		return this.mode;
	}
	
	/**
	 * Returns the name of this way of searching.
	 * @return the name of this way of searching.  
	 */
	public String getName() {
		return this.name;
	}
}
