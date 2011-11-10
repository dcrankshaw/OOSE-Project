package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.TagTableModel.TagSelectionChangedEvent;

/**
 * Listens for changes of which tags were selected for filtering 
 */
public interface TagSelectionChangedListener {
	/**
	 * Indicates that the selected tags for filtering have changed
	 * @param e the tags and how they have changed
	 */
	public void tagSelectionChanged(TagSelectionChangedEvent e);

}
