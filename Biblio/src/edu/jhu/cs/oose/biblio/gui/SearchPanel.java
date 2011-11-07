package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

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
	
	/** A scroll pane to contain the table of tags.	 */
	private JScrollPane  tagsScrollPane;
	
	public SearchPanel() {
		queryField = new JTextField();
		queryField.setColumns(20);
		possibleTagsTable = new JTable(new TagTableModel());
		
		this.setLayout(new BorderLayout());
		
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(queryField, BorderLayout.NORTH);
		
		JPanel radioPanel = new JPanel();
		ButtonGroup searchChoiceGroup = new ButtonGroup();
		radioPanel.setLayout(new GridLayout(1, 2));
		searchTagsButton = new JRadioButton("Search Tags");
		searchChoiceGroup.add(searchTagsButton);
		searchTextButton = new JRadioButton("Full Text Search");
		searchChoiceGroup.add(searchTextButton);
		radioPanel.add(searchTagsButton);
		radioPanel.add(searchTextButton);
		upperPanel.add(radioPanel, BorderLayout.CENTER);
		searchChoiceGroup.setSelected(searchTagsButton.getModel(), true);
		
		this.add(upperPanel, BorderLayout.NORTH);
		tagsScrollPane = new JScrollPane(possibleTagsTable);
		this.add(tagsScrollPane, BorderLayout.CENTER);
	}
	
	/**
	 * Gets the search term
	 * @return the search term
	 */
	public String getQueryText(){
		return null;
	}
	
	/** 
	 * Gets the type of search the user wants to do
	 * @return the search mode
	 */
	public SearchMode getSearchMode() {
		return SearchMode.TAGS;
	}
}
