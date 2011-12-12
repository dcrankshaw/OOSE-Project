package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhu.cs.oose.biblio.model.Category;
import edu.jhu.cs.oose.biblio.model.EditorManager;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * Provides the functionality needed to get the user input to edit tags.
 */
public class TagEditorPanel extends JPanel {
	
	/** A table containing all of the tags. The user can select from here to edit
	 * existing tags.
	 */
	private JList overallTagTable;
	
	/** The label displaying the name of the currently selected Tag */
	private JLabel nameLabel;
	/**
	 * Contains all of the tags that currently selected tag has been associated with (all of these
	 * tags point to the selected tag).
	 */
	private TagsListPanel associatedTagPanel;
	
	/** The currently selected tag*/
	private Tag selectedTag;
	
	/** A button to create a new tag*/
	private JButton addTagButton;
	
	/** A button to delete the currently selected tag*/
	private JButton deleteTagButton;
	
	/** A button to add a new tag Category to the Biblio infrastructure */
	private JButton newCategoryButton;
	
	/** A button to delete all of the currently checked categories */
	private JButton deleteCategoryButton;
	
	/** A table containing all of the tag categories each with an accompanying checkbox */
	private JTable categoryTable;
	
	/** The presentation of the list of tags to the JList */
	private TagEditorListModel tagListModel;
	
	/** The representation of the table of Categories to the JTable */
	private CategoryTableModel categoryModel;
	
	/** Creates, lays out, and connects the GUI elements for editing/creating Tags and Categories. */
	public TagEditorPanel() {
		super();
		
		EditorManager manager = new EditorManager();
		tagListModel = new TagEditorListModel(manager);
		
		this.setLayout(new BorderLayout());
		
		JPanel subpanel = new JPanel();
		subpanel.setLayout(new BorderLayout());
		this.overallTagTable = new JList(tagListModel);
		JScrollPane scrollPane = new JScrollPane(this.overallTagTable);
		this.overallTagTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				setSelectedTag(e.getFirstIndex());
			}
		});
		subpanel.add(scrollPane, BorderLayout.CENTER);
		subpanel.add(new JLabel("Tags:"), BorderLayout.NORTH);
		
		JPanel subsubpanel = new JPanel();
		subsubpanel.setLayout(new BorderLayout());
		this.addTagButton = new JButton("New Tag");
		this.addTagButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newTag();
			}
		});
		subsubpanel.add(this.addTagButton, BorderLayout.WEST);
		
		this.deleteTagButton = new JButton("Delete Tag");
		this.deleteTagButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteTag();
			}
		});
		subsubpanel.add(this.deleteTagButton, BorderLayout.EAST);
		subpanel.add(subsubpanel, BorderLayout.SOUTH);
		
		this.add(subpanel, BorderLayout.WEST);
		
		subpanel = new JPanel();
		subpanel.setLayout(new BorderLayout());
		
		subsubpanel = new JPanel();
		subsubpanel.add(new JLabel("Name: "), BorderLayout.WEST);
		this.nameLabel = new JLabel("                ");
		subsubpanel.add(this.nameLabel, BorderLayout.CENTER);
		subpanel.add(subsubpanel, BorderLayout.NORTH);
		
		this.associatedTagPanel = new TagsListPanel();
		this.associatedTagPanel.setTitle("Associated Tags:");
		subpanel.add(this.associatedTagPanel, BorderLayout.CENTER);
		
		this.add(subpanel, BorderLayout.CENTER);
		
		subpanel = new JPanel();
		subpanel.setLayout(new BorderLayout());
		subpanel.add(new JLabel("Categories:"), BorderLayout.NORTH);
		
		categoryModel = new CategoryTableModel(manager);
		categoryModel.addSelectionListener(new TableSelectionChangedListener<Category>() {
			@Override
			public void selectionChanged(AbstractTableModel<Category>.SelectionChangedEvent e) {
				for( Category cat : e.oldTags ) {
					cat.getTags().remove(getSelectedTag());
				}
				for( Category cat : e.newTags ) {
					cat.getTags().add(getSelectedTag());
				}
			}
		});
		this.categoryTable = new JTable(this.categoryModel);
		scrollPane = new JScrollPane(this.categoryTable);
		subpanel.add(scrollPane, BorderLayout.CENTER);
		
		subsubpanel = new JPanel();
		subsubpanel.setLayout(new BorderLayout());
		this.newCategoryButton = new JButton("New Category");
		this.newCategoryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newCategory();
			}
		});
		subsubpanel.add(this.newCategoryButton, BorderLayout.WEST);
		
		this.deleteCategoryButton = new JButton("Delete Category");
		this.deleteCategoryButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteCategory();
			}
		});
		subsubpanel.add(this.deleteCategoryButton, BorderLayout.EAST);
		subpanel.add(subsubpanel, BorderLayout.SOUTH);
		
		this.add(subpanel, BorderLayout.EAST);
	}
	
	/** Create a new tag */
	public void newTag() {
		tagListModel.newTag();
	}
	
	/** Delete the selected tag */
	public void deleteTag() {
		int idx = overallTagTable.getSelectedIndex();
		tagListModel.deleteTag(idx);
	}
	
	/** Add a new category */
	public void newCategory() {
		categoryModel.newCategory();
	}
	
	/** Delete all of the currently selected categories */
	public void deleteCategory() {
		int idx = categoryTable.getSelectedRow();
		categoryModel.removeCategory(idx);
	}
	
	/**
	 * Sets the currently selected Tag to that given by this index
	 * @param selectedIndex the index of the Tag to select
	 */
	public void setSelectedTag(int selectedIndex) {
		this.selectedTag = this.tagListModel.getTag(selectedIndex);
		nameLabel.setText(selectedTag.getName());
		this.associatedTagPanel.setTagsList(selectedTag.getChildren());
		this.categoryModel.setTag(selectedTag);
	}
	
	/**
	 * Returns the currently selected Tag
	 * @return the currently selected Tag
	 */
	public Tag getSelectedTag() {
		return this.selectedTag;
	}
}
