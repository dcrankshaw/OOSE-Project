package edu.jhu.cs.oose.biblio.gui;

/** The different modes to search through the Biblio infrastructure */
public enum SearchMode {

	/** Search for Tags with certain names. */
	TAGS,
	/** Search for Files tagged with certain tags. */
	FILTER,
	/** Search for files containing certain text. */
	FULLTEXT,
	/** Search for bookmarks with certain tags. */
	BOOKMARKS;
}
