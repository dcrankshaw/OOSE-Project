package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * Automatically recognizes tags that are entered in and converts the string to an atomic entity. If it cannot
 * find a match, it will create a new tag. Also displays all tags currently associated with this file.
 */
public class TagsListPanel extends JPanel {
	
	/** All of the tags already added to the file */
	private DefaultListModel tagsListModel;
	// TODO migrate this to a List<Tag> extends ListModel (for the JList)
	// or, combine the text field in the bottom with the
	// list, so that you type into the list, and it absorbs
	// recognized tags into atomic units.  Just my thoughts... Paul
	
	/** The set of tags that are displayed / managed by this panel */
	private Collection<Tag> tagSet;
	
	/** The file whose tags are displayed in this panel */
	private FileMetadata file;
	
	/** The Primary key array of all the tag that was created in this panel so far */
	private Set<String> newTags;
	
	/** The label saying "Tags:" */
	private JLabel tagsLabel;
	
	/** The text field for entering new tags */
	private JTextField newTagField;
	
	/** Display of the tags that have already been applied to this file. */
	private JList addedTags;
	
	/** Creates a new Panel that displays the tags applied to a file. */
	public TagsListPanel() {
		tagsLabel = new JLabel("Tags:");
		newTagField = new JTextField();
		newTagField.setColumns(10);
		newTagField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String tagName = newTagField.getText();
				newTagField.setText("");
				Tag t = findOrCreateTag(tagName);
				addTag(t);
			}
		});
		
		tagsListModel = new DefaultListModel();
		tagSet = null;
		newTags = new HashSet<String>();
		addedTags = new JList(tagsListModel);
		addedTags.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		addedTags.setVisibleRowCount(-1);
		this.setLayout(new BorderLayout());
		this.add(newTagField, BorderLayout.SOUTH);
		this.add(addedTags, BorderLayout.CENTER);
		this.add(tagsLabel, BorderLayout.NORTH);
	}
	
	/**
	 * Creates a new panel that displays the tags applied to the file
	 * @param fileMetadata the file whose tags should be displayed
	 */
	public TagsListPanel(FileMetadata fileMetadata)
	{
		this();
		this.setFile(fileMetadata);
	}
	
	/** Parses the text the user has entered and attempts to find the matching tag */
	public void parseString() {}
	
	/**
	 * Gets the tag specified by the string, or creates it if it does
	 * not already exist.
	 * @param tagName the tag name of the Tag
	 * @return the Tag with the given name
	 *  */
	private Tag findOrCreateTag(String tagName) {
		Tag t = Database.getTag(tagName);
		if (t == null) {
			t = new Tag(tagName);
		}
		return t;
	}
	
	/** delete those newly created tags */
	public void rollback() {
		for (String n : newTags) {
			Tag.delete(n);
		}
		newTags.clear();
	}
	
	/** store the fileMetaData into db and tag them with tags in addedTags */
	public void commit() {
		for (String n : newTags) {
			Tag t = Database.getTag(n);
			t.addTaggedFiles(file);
			//t.update();
		}
		newTags.clear();
	}
	
	/**
	 * Adds a tag to this file
	 * @param t the tag to apply 
	 */
	public void addTag(Tag t)
	{
		if( tagSet.add(t) ) {
			tagsListModel.addElement(t.getName());
		}
	}
	
	/**
	 * Sets the file whose tags should be displayed
	 * @param f the file whose tags should be displayed
	 */
	public void setFile(FileMetadata f) {
		setTagsList(f.getTags());
	}
	
	/**
	 * Sets the collection of Tags that this panel updates
	 * and displays
	 * @param newTags the set of Tags to manipulate
	 */
	public void setTagsList(Collection<Tag> newTags) {
		tagSet = newTags;
		tagsListModel.clear();
		for( Tag t : newTags ) {
			tagsListModel.addElement(t);
		}
	}
	
	/**
	 * Sets the title of this box
	 * @param title the new title of the box
	 */
	public void setTitle(String title) {
		this.tagsLabel.setText(title);
	}
}
