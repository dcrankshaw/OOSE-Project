package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * A FileView is something that displays a file.
 * It can be the tabbed display that contains a FileDisplay panel,
 * or perhaps another window that contains the display panel.
 * These objects have the power to make themselves visible.
 * The panel cannot make its tab the current tab, but this thing
 * can make the right tab the current tab.
 */
public interface FileView {
	/**
	 * Makes this view visible, say, by selecting the right tab
	 * or making the window the one on top.
	 */
	public void makeVisible();
	
	/** Lays out the view and redraws it */
	public void revalidate();
	
	/** Returns the file displayed by this view */
	public FileMetadata getFile();
}
