package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * Automatically recognizes tags that are entered in and converts the string to an atomic entity. If it cannot
 * find a match, it will create a new tag. Also displays all tags currently associated with this file.
 */

public class TagsListPanel extends JPanel {
	
	/** All of the tags already added to the file */
	public DefaultListModel tags;
	
	private FileMetadata file;
	
	/** The text entered into the pane by the user */
	public String text;
	
	JLabel tagsLabel;
	
	JTextField newTagField;
	
	JList addedTags;
	
	public TagsListPanel(FileMetadata fileMetadata)
	{
		file = fileMetadata;
		tagsLabel = new JLabel("Tags:");
		newTagField = new JTextField();
		newTagField.setColumns(10);
		// Paul: Can we do this with an ActionListener instead?
		// That's probably "the right way" to do what we want.
		newTagField.addKeyListener(new KeyAdapter() {
			
			@Override
			public void keyTyped(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					createTag();
				}
			}
		});
		tags = new DefaultListModel();
		addedTags = new JList(tags);
		addedTags.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		addedTags.setVisibleRowCount(-1);
		this.setLayout(new BorderLayout());
		this.add(newTagField, BorderLayout.SOUTH);
		this.add(addedTags, BorderLayout.CENTER);
		this.add(tagsLabel, BorderLayout.NORTH);
	}
	
	
	/** Parses the text the user has entered and attempts to find the matching tag */
	public void parseString() {}
	
	/** creates a new tag from user entered text and adds it to the list of tags associated with this file */
	private void createTag()
	{
		String tagName = newTagField.getText();
		newTagField.setText(null);
		addTag(new Tag(tagName));
	}
	
	public void addTag(Tag t)
	{
		//file.addTag(t);
		tags.addElement(t.name);
	}
}
