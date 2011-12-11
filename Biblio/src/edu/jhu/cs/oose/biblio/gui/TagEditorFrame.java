package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.jhu.cs.oose.biblio.gui.CategoryTableModel.CategorySelectionChangedEvent;
import edu.jhu.cs.oose.biblio.model.Category;
import edu.jhu.cs.oose.biblio.model.EditorManager;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * Provides the functionality needed to get the user input to edit tags.
 */
public class TagEditorFrame extends JFrame {
	
	/** A table containing all of the tags. The user can select from here to edit
	 * existing tags.
	 */
	private JList overallTagTable;
	
	/** The text field displaying the name of the currently selected Tag */
	private JTextField nameField;
	/**
	 * Contains all of the tags that currently selected tag has been associated with (all of these
	 * tags point to the selected tag).
	 */
	private TagsListPanel associatedTagPanel;
	
	/** The currently selected tag*/
	private Tag selectedTag;
	
	/** A table containing all of the tag categories each with an accompanying checkbox */
	private JTable categoryTable;
	
	/** The presentation of the list of tags to the JList */
	private TagEditorListModel tagListModel;
	
	/** The representation of the table of Categories to the JTable */
	private CategoryTableModel categoryModel;
	
	/** Creates, lays out, and connects the GUI elements for editing/creating Tags and Categories. */
	public TagEditorFrame() {
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
				if( e.getValueIsAdjusting() == false ) {
					setSelectedTag();
				}
			}
		});
		subpanel.add(scrollPane, BorderLayout.CENTER);
		subpanel.add(new JLabel("Tags:"), BorderLayout.NORTH);
		
		JPanel subsubpanel = new JPanel();
		subsubpanel.setLayout(new BorderLayout());
		JButton addTagButton = new JButton("New Tag");
		addTagButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newTag();
			}
		});
		subsubpanel.add(addTagButton, BorderLayout.WEST);
		
		JButton deleteTagButton = new JButton("Delete Tag");
		deleteTagButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteTag();
			}
		});
		subsubpanel.add(deleteTagButton, BorderLayout.EAST);
		subpanel.add(subsubpanel, BorderLayout.SOUTH);
		
		this.add(subpanel, BorderLayout.WEST);
		
		subpanel = new JPanel();
		subpanel.setLayout(new BorderLayout());
		
		subsubpanel = new JPanel();
		subsubpanel.add(new JLabel("Name: "), BorderLayout.WEST);
		this.nameField = new JTextField();
		this.nameField.setColumns(30);
		this.nameField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateTagName();
			}
		});
		subsubpanel.add(this.nameField, BorderLayout.CENTER);
		subpanel.add(subsubpanel, BorderLayout.NORTH);
		
		this.associatedTagPanel = new TagsListPanel();
		this.associatedTagPanel.setTitle("Associated Tags:");
		subpanel.add(this.associatedTagPanel, BorderLayout.CENTER);
		
		this.add(subpanel, BorderLayout.CENTER);
		
		subpanel = new JPanel();
		subpanel.setLayout(new BorderLayout());
		subpanel.add(new JLabel("Categories:"), BorderLayout.NORTH);
		
		categoryModel = new CategoryTableModel(manager);
		categoryModel.addCategorySelectionListener(new CategorySelectionListener() {
			@Override
			public void categorySelectionChanged(CategorySelectionChangedEvent e) {
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
		JButton newCategoryButton = new JButton("New Category");
		newCategoryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newCategory();
			}
		});
		subsubpanel.add(newCategoryButton, BorderLayout.WEST);
		
		JButton deleteCategoryButton = new JButton("Delete Category");
		deleteCategoryButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				deleteCategory();
			}
		});
		subsubpanel.add(deleteCategoryButton, BorderLayout.EAST);
		subpanel.add(subsubpanel, BorderLayout.SOUTH);
		
		this.add(subpanel, BorderLayout.EAST);
		
		this.setTitle("Manage Tags");
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
	 * Sets the currently selected Tag to that given by the JList's selected index
	 */
	public void setSelectedTag() {
		int selectedIndex = this.overallTagTable.getSelectedIndex();
		if( selectedIndex < 0 ) {
			this.selectedTag = null;
			this.nameField.setText("");
			@SuppressWarnings("unchecked")
			Collection<Tag> emptySet = (Collection<Tag>)Collections.EMPTY_SET;
			this.associatedTagPanel.setTagsList(emptySet);
			this.categoryModel.setTag(null);
		}
		else {
			this.selectedTag = this.tagListModel.getTag(selectedIndex);
			this.nameField.setText(selectedTag.getName());
			this.associatedTagPanel.setTagsList(selectedTag.getChildren());
			this.categoryModel.setTag(selectedTag);
		}
		this.pack();
		this.nameField.revalidate();
		this.associatedTagPanel.revalidate();
		this.repaint();
		this.nameField.repaint();
		this.associatedTagPanel.repaint();
	}
	
	/**
	 * Returns the currently selected Tag
	 * @return the currently selected Tag
	 */
	public Tag getSelectedTag() {
		return this.selectedTag;
	}
	
	/**
	 * Responds to the user entering a new name for the selected Tag
	 */
	private void updateTagName() {
		if( null == this.selectedTag ) {
			return;
		}
		// When the tags list updates, the selection may change,
		// which will trigger all the listeners, so we need to
		// make sure that the right tag is selected
		// before we return
		Tag currentTag = this.selectedTag;
		String newName = this.nameField.getText();
		if( currentTag.setName(newName) ) {
			this.tagListModel.forceUpdate();
			this.overallTagTable.setSelectedIndex(this.tagListModel.indexOf(currentTag));
		}
	}
}
