package edu.jhu.cs.oose.biblio.gui;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * Provides the functionality needed to get the user input to edit tags.
 */
public class TagEditorPanel extends JPanel {
	
	/** A table containing all of the tags. The user can select from here to edit
	 * existing tags.
	 */
	private JTable overallTagTable;
	
	/**
	 * Contains all of the tags that currently selected tag has been associated with (all of these
	 * tags point to the selected tag).
	 */
	private TagsListPanel associatedTagPane;
	
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
	
	/** Create a new tag */
	public void newTag(){}
	
	/** Delete the selected tag */
	public void deleteTag(){}
	
	/** Add a new category */
	public void newCategory(){}
	
	/** Delete all of the currently selected categories */
	public void deleteCategory(){}
	
	

}
