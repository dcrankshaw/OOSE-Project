package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
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
import edu.jhu.cs.oose.biblio.model.FilterSearchStrategy;
import edu.jhu.cs.oose.biblio.model.SearchManager;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.TextSearchStrategy;

/**
 * Provides the UI to search. Encloses the text field where search terms are entered, as well as the list of possible tags
 * and the tags/full text radio button to indicate the search mode
 */
public class SearchPanel extends JPanel {
	
	/** The text field for the user to enter search terms */
	private JTextField queryField;
	
	/** A radio button to indicate whether to search for tags */
	//private JRadioButton searchTagsButton;
	
	/** A radio button to indicate whether to search through full text */
	//private JRadioButton searchTextButton;
	
	/** A table listing all of the tags matching the search term */
	private JTable possibleTagsTable;
	/** A list of all the tags the user has selected. */
	private JList selectedTagsList;
	
	/** A scroll pane to contain the table of tags.	 */
	private JScrollPane  tagsScrollPane;
	
	/** The object that will actually do the searching. */
	private SearchManager controller;
	
	/**
	 * The table that stores which tags are currently
	 * selected for filtering and displays results of searching
	 * for tags
	 */
	private TagTableModel tagTable;
	
	/**
	 * The current strategy to use for searching when text
	 * is entered in the text field.
	 */
	private TextSearchStrategy textStrategy;
	/**
	 * The current strategy to use when Tags are selected for filtering.
	 */
	private FilterSearchStrategy filterStrategy;
	
	/**
	 * Creates a new UI for searching.
	 * @param textStrategies a list of strategies for searching based
	 * on the text field.
	 * @param filterStrategy the strategy to use for searching based on which
	 * Tags are selected in the table.
	 */
	public SearchPanel(List<TextSearchStrategy> textStrategies, FilterSearchStrategy filterStrategy) {
		this.filterStrategy = filterStrategy;
		this.textStrategy = textStrategies.get(0);
		
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
				if( e.newTags != null && e.newTags.size() > 0 ) {
					selectedTags.addAll(e.newTags);
				}
				if( e.removedTags != null && e.removedTags.size() > 0 ) {
					selectedTags.removeAll(e.removedTags);
				}
				executeFilter(selectedTags);
			}
		});
		possibleTagsTable = new JTable(tagTable);
		
		this.setLayout(new BorderLayout());
		
		JPanel upperPanel = new JPanel();
		upperPanel.setLayout(new BorderLayout());
		upperPanel.add(queryField, BorderLayout.NORTH);
		
		if( textStrategies.size() > 1 ) {
		
			JPanel radioPanel = new JPanel();
			ButtonGroup searchChoiceGroup = new ButtonGroup();
			radioPanel.setLayout(new GridLayout(1, textStrategies.size()));
			
			class SearchChoiceButtonListener implements ActionListener {
				private TextSearchStrategy strategy;
				SearchChoiceButtonListener(TextSearchStrategy s) {
					this.strategy = s;
				}
				@Override
				public void actionPerformed(ActionEvent e) {
					setTextSearchStrategy(strategy);
				}
			}
			
			for( TextSearchStrategy strategy : textStrategies ) {
				JRadioButton searchButton = new JRadioButton(strategy.getName());
				searchButton.addActionListener(new SearchChoiceButtonListener(strategy));
				searchChoiceGroup.add(searchButton);
				radioPanel.add(searchButton);
			}
			
			upperPanel.add(radioPanel, BorderLayout.CENTER);
			searchChoiceGroup.setSelected(searchChoiceGroup.getElements().nextElement().getModel(), true);
		}
		this.add(upperPanel, BorderLayout.NORTH);
		tagsScrollPane = new JScrollPane(possibleTagsTable);
		this.add(tagsScrollPane, BorderLayout.CENTER);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(new JLabel("Selected Tags:"), BorderLayout.NORTH);
		SortedListModel<String> selectedModel = new SortedListModel<String>();
		selectedTagsList = new JList(selectedModel);
		panel.add(selectedTagsList, BorderLayout.CENTER);
		this.add(panel, BorderLayout.SOUTH);
		tagTable.addTagSelectionListener(new SelectionListener(selectedModel));
	}
	
	/**
	 * A simple listener that keeps the JList of currently
	 * selected Tags up to date
	 */
	private class SelectionListener implements TagSelectionChangedListener {
		/** The list of Tag names to be displayed in the list */
		SortedListModel<String> model;
		
		/**
		 * Creates a new SelectionListener updating the given list.
		 * @param m the list to keep up to date
		 */
		SelectionListener( SortedListModel<String> m) {
			model = m;
		}
		
		@Override
		public void tagSelectionChanged(TagSelectionChangedEvent e) {
			if( e.removedTags != null  ) {
				for( Tag t : e.removedTags ) {
					model.remove(t.getName());
				}
			}
			if( e.newTags != null ) {
				for( Tag t: e.newTags ) {
					model.add(t.getName());
				}
			}
		}
	}
	
	/**
	 * Sets the object that will perform searches entered on this panel
	 * @param m the object to perform searches
	 */
	public void setSearchController(SearchManager m) {
		if( controller != null ) {
			controller.removeTagsListener(tagTable);
		}
		controller = m;
		if( controller != null ) {
			controller.addTagsListener(tagTable);
		}
	}
	
	/**
	 * Tells SearchManager to select all files tagged by the entire set of selectedTags
	 * @param selectedTags All of the tags a file needs to have
	 */
	private void executeFilter(Set<Tag> selectedTags)
	{
		filterStrategy.search(controller, selectedTags);
	}
	
	/**
	 * Sets the strategy used to search when text is entered in the text field.
	 * @param newStrat the strategy used to search when text is entered in the text field.
	 */
	private void setTextSearchStrategy(TextSearchStrategy newStrat) {
		this.textStrategy = newStrat;
	}
	
	/** Triggers execution of the search. */
	private void executeSearch() {
		this.textStrategy.search(controller, queryField.getText());
	}
}
