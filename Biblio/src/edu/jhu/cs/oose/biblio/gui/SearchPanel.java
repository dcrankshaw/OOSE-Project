package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JList;
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
	
	/** The text field for the user to enter search terms */
	private JTextField queryField;
	
	/** A radio button to indicate whether to search for tags */
	private JRadioButton searchTagsButton;
	
	/** A radio button to indicate whether to search through full text */
	private JRadioButton searchTextButton;
	
	/** A table listing all of the tags matching the search term */
	private JTable possibleTagsTable;
	private JList selectedTagsList;
	
	/** A scroll pane to contain the table of tags.	 */
	private JScrollPane  tagsScrollPane;
	
	/** Whether the next search should be tags or full text. */
	private SearchMode currentSearchMode;
	
	/** The object that will actually do the searching. */
	private SearchManager controller;
	
	/**
	 * The table that stores which tags are currently
	 * selected for filtering and displays results of searching
	 * for tags
	 */
	private TagTableModel tagTable;
	
	/**
	 * Creates a new UI for searching.
	 */
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
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel("Selected Tags:"), BorderLayout.SOUTH);
		SortedListModel<String> selectedModel = new SortedListModel<String>();
		selectedTagsList = new JList(selectedModel);
		panel.add(selectedTagsList, BorderLayout.CENTER);
		this.add(panel, BorderLayout.SOUTH);
		tagTable.addTagSelectionListener(new SelectionListener(selectedModel));
	}
	
	private class SelectionListener implements TagSelectionChangedListener {
		SortedListModel<String> model;
		SelectionListener( SortedListModel<String> m) {
			model = m;
		}
		@Override
		public void tagSelectionChanged(TagSelectionChangedEvent e) {
			for( Tag t : e.oldTags ) {
				model.remove(t.getName());
			}
			for( Tag t: e.newTags ) {
				model.add(t.getName());
			}
		}
	}
	
	/**
	 * Sets how the next search should be conducted,
	 * either for tags or for text
	 * @param newMode the next search method
	 */
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
	
	/** Triggers execution of the search. */
	private void executeSearch() {
		if( this.currentSearchMode == SearchMode.FULLTEXT ) {
			controller.searchText(queryField.getText());
		}
		else if( this.currentSearchMode == SearchMode.TAGS ) {
			controller.searchTags(queryField.getText());
		}
	}
}
