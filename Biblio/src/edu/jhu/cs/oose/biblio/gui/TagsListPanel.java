package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;
import edu.jhu.cs.oose.biblio.model.TagListener;
import edu.jhu.cs.oose.biblio.model.Tagable;

/**
 * Automatically recognizes tags that are entered in and converts the string to an atomic entity. If it cannot
 * find a match, it will create a new tag. Also displays all tags currently associated with this file.
 */
public class TagsListPanel extends JPanel {
	
	/** All of the tags already added to the file */
	private SortedListModel<Tag> tagsListModel;
	
	/** The things whose Tags are displayed on this panel */
	private Tagable data;
	
	/** The label saying "Tags:" */
	private JLabel tagsLabel;
	
	/** The text field for entering new tags */
	private JTextField newTagField;
	
	/** Display of the tags that have already been applied to this file. */
	private JList addedTags;
	
	/**
	 * Listens for updates to the data's collection of Tags,
	 * and updates this panel
	 */
	private TagListener listener;
	private TagListener tagChangedListener;
	
	/** Creates a new Panel that displays the tags applied to a file. */
	public TagsListPanel() {
		tagsLabel = new JLabel("Tags:");
		newTagField = new JTextField();
		newTagField.setColumns(10);
		newTagField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				parseString();
			}
		});
		
		tagsListModel = new SortedListModel<Tag>();
		data = null;
		addedTags = new JList(tagsListModel);
		addedTags.setLayoutOrientation(JList.VERTICAL);
		addedTags.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		addedTags.setVisibleRowCount(-1);
		this.setLayout(new BorderLayout());
		this.add(newTagField, BorderLayout.SOUTH);
		this.add(addedTags, BorderLayout.CENTER);
		this.add(tagsLabel, BorderLayout.NORTH);
		listener = new TagListener() {
			@Override
			public void nameChanged(Tagable tag) {
				TagsListPanel.this.setTags(TagsListPanel.this.data);
			}
			@Override
			public void childrenChanged(Tagable tag) {
			}
		};
		this.tagChangedListener = new TagListener() {

			@Override
			public void nameChanged(Tagable tag) {}

			@Override
			public void childrenChanged(Tagable tag) {
				TagsListPanel.this.setTags(tag);
			}
			
		};
		this.addedTags.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if( e.getKeyCode() == KeyEvent.VK_BACK_SPACE ) {
					removeTag();
				}
			}
		});
	}
	
	/**
	 * Creates a new panel that displays the tags applied to the file
	 * @param fileMetadata the file whose tags should be displayed
	 */
	public TagsListPanel(FileMetadata fileMetadata)
	{
		this();
		this.setTags(fileMetadata);
	}
	
	/** Parses the text the user has entered and attempts to find the matching tag */
	public void parseString() {
		if( null == this.data ) {
			return;
		}
		String tagName = newTagField.getText();
		boolean started_session = false;
		if( Database.getSession() == null ) {
			Database.getNewSession();
			started_session = true;
		}
		Tag t = findOrCreateTag(tagName);
		if(t != null) {
			newTagField.setText("");
			addTag(t);
		}
		if( started_session ) {
			Database.commit();
		}
	}
	
	/**
	 * Gets the tag specified by the string, or creates it if it does
	 * not already exist.
	 * @param tagName the tag name of the Tag
	 * @return the Tag with the given name
	 *  */
	private Tag findOrCreateTag(String tagName) {
		Tag t = Database.getTag(tagName);
		if (t == null) {
			try {
				t = new Tag(tagName);
			} catch (Exception e) {
				System.out.println("Cannot create a tag with a colon in it");
			}
		}
		return t;
	}

	/**
	 * Adds a tag to this file
	 * @param t the tag to apply 
	 */
	public void addTag(Tag t)
	{
		if( null != data ) {
			if( data.addTag(t) ) {
				Database.update(t);
				Database.update(data);
				t.addListener(this.listener);
			}
		}
	}
	
	/**
	 * Gets the currently selected Tag and
	 * removes it from the file/ thing backing the panel
	 */
	private void removeTag() {
		if( null != data ) {
			int idx = this.addedTags.getSelectedIndex();
			if( idx < 0 || idx >= this.tagsListModel.getSize() ) {
				return;
			}
			Tag tag = this.tagsListModel.getElementAt(idx);
			if( data.removeTag(tag) ) {
				tagsListModel.remove(tag);
				Database.update(tag);
				Database.update(data);
				tag.removeListener(this.listener);
			}
		}
	}
		
	/**
	 * Sets the thing whose Tags should be displayed on this Panel
	 * @param newData the new thing whose Tags should be displayed
	 */
	public void setTags(Tagable newData) {
		if( null != this.data ) {
			for( Tag t : this.data.getTags() ) {
				t.removeListener(this.listener);
			}
			this.data.removeListener(this.tagChangedListener);
		}
		data = newData;
		tagsListModel.clear();
		if( null != newData ) {
			for( Tag t : newData.getTags() ) {
				@SuppressWarnings("unchecked")
				Database<Tag> db = (Database<Tag>)Database.get(Tag.class);
				db.add(t);
				t.addListener(this.listener);
				tagsListModel.add(t);
			}
			this.data.addListener(this.tagChangedListener);
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
