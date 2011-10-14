package edu.jhu.cs.oose.biblio.gui;

import javax.swing.JPanel;

/**
 * Provides the UI to search. Encloses the text field where search terms are entered, as well as the list of possible tags
 * and the tags/full text radio button to indicate the search mode
 */

public class SearchPanel extends JPanel {
	
	/** The text field for the user to enter search terms */
	private JTextField queryField;
	
	/** A radio button to indicate whether to search for tags */
	private JRadioButton searchTagsButton;
	
	/** A radio button to indicate whether to search through full text */
	private JRadioButton searchTextButton;
	
	
	/** A table listing all of the tags matching the search term */
	private JTable possibleTagsTable;
	
	/** Gets all of the selected tags from the possibleTagsTable */
	public Set<Tag> getSelectedTags(){}
	
	/**
	 * Gets the search term
	 * @return the search term
	 */
	public String getQueryText(){}
	
	/** 
	 * Gets the type of search the user wants to do
	 * @return the search mode
	 */
	public SearchMode getSearchMode() {}
	
	
}
