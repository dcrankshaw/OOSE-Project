package edu.jhu.cs.oose.biblio;

/**
 * When a search of the tags has been completed, this provides a list of the matching tags.
 * The ordering of the tags will reflect a priority ordering (either alphabetical or relevance
 * depending on the context)
 */


public abstract class SearchTagsListener {
	
	/**
	 * Called when a search is executed. Tells the owner of this SearchTagsListener
	 * to display the results of the search
	 * @param matches All of the matching tags
	 */
	public void matchedTags(Set<Tags> matches) {}
	
	
	
	

}
