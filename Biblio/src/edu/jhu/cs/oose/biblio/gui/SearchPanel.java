package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import edu.jhu.cs.oose.biblio.gui.TagTableModel.TagSelectionChangedEvent;
import edu.jhu.cs.oose.biblio.model.SearchManager;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * Provides the UI to search. Encloses the text field where search terms are entered, as well as the list of possible tags
 * and the tags/full text radio button to indicate the search mode
 */
public class SearchPanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7701379661120997247L;


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
	
	private SearchMode currentSearchMode;
	
	private SearchManager controller;
	
	private TagTableModel tagTable;
	
	public SearchPanel() {
		currentSearchMode = SearchMode.TAGS;
		
		queryField = new JTextField();
		queryField.setColumns(20);
		queryField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				executeSearch();
			}
		});
		
		
		tagTable = new TagTableModel();
		tagTable.addTagSelectionListener(new TagSelectionChangedListener() {
			
			@Override
			public void tagSelectionChanged(TagSelectionChangedEvent e) {
				Set<Tag> selectedTags = new HashSet<Tag>(e.oldTags);
				selectedTags.addAll(e.newTags);
				selectedTags.removeAll(e.removedTags);
				executeFilter(selectedTags);
			}
		});
		possibleTagsTable = new JTable(tagTable);
		
		this.setLayout(new BorderLayout());
		
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(queryField, BorderLayout.NORTH);
		
		JPanel radioPanel = new JPanel();
		ButtonGroup searchChoiceGroup = new ButtonGroup();
		radioPanel.setLayout(new GridLayout(1, 2));
		searchTagsButton = new JRadioButton("Search Tags");
		searchTagsButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setSearchMode(SearchMode.TAGS);
			}
		});
		searchChoiceGroup.add(searchTagsButton);
		searchTextButton = new JRadioButton("Full Text Search");
		searchTextButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setSearchMode(SearchMode.FULLTEXT);
			}
		});
		searchChoiceGroup.add(searchTextButton);
		radioPanel.add(searchTagsButton);
		radioPanel.add(searchTextButton);
		upperPanel.add(radioPanel, BorderLayout.CENTER);
		searchChoiceGroup.setSelected(searchTagsButton.getModel(), true);
		
		this.add(upperPanel, BorderLayout.NORTH);
		tagsScrollPane = new JScrollPane(possibleTagsTable);
		this.add(tagsScrollPane, BorderLayout.CENTER);
	}
		
	public void setSearchMode(SearchMode newMode) {
		this.currentSearchMode = newMode;
	}
	
	/** 
	 * Gets the type of search the user wants to do
	 * @return the search mode
	 */
	public SearchMode getSearchMode() {
		return this.currentSearchMode;
	}
	
	/**
	 * Tells SearchManager to select all files tagged by the entire set of selectedTags
	 * @param selectedTags All of the tags a file needs to have
	 */
	private void executeFilter(Set<Tag> selectedTags)
	{
		controller.filterByTags(selectedTags);
	}
	
	private void executeSearch() {
		if( this.currentSearchMode == SearchMode.FULLTEXT ) {
			controller.searchText(queryField.getText());
		}
		else if( this.currentSearchMode == SearchMode.TAGS ) {
			controller.searchTags(queryField.getText());
		}
	}
}
