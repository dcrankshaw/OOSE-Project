package edu.jhu.cs.oose.biblio.gui;

/**
 * Displays the entire file, drawing scrollbars, etc needed to view the whole file, depending what type of file it is.
 */

//TODO need methods?
public abstract class FullFilePanel extends FileDisplayPanel {
	
	/** Abstract method to draw the file */
	public abstract void displayFile();

}
