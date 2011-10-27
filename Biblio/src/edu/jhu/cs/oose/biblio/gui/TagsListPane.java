package edu.jhu.cs.oose.biblio.gui;

import java.util.List;

import edu.jhu.cs.oose.biblio.model.Tag;

/**
 * Automatically recognizes tags that are entered in and converts the string to an atomic entity. If it cannot
 * find a match, it will create a new tag. Also displays all tags currently associated with this file.
 */

public class TagsListPane {
	
	/** All of the tags already added to the file */
	public List<Tag> tags;
	
	/** The text entered into the pane by the user */
	public String text;
	
	
	/** Parses the text the user has entered and attempts to find the matching tag */
	public void parseString() {}
	
	/** creates a new tag from user entered text and adds it to the list of tags associated with this file */
	public void createTag(String tagname) {}

}
