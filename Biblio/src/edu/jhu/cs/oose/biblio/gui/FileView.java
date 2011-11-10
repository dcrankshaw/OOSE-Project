package edu.jhu.cs.oose.biblio.gui;

/**
 * A FileView is something that displays a file.
 * It can be the tabbed display that contains a FileDisplay panel,
 * or perhaps another window that contains the display panel.
 * These objects have the power to make themselves visible.
 * The panel cannot make its tab the current tab, but this thing
 * can make the right tab the current tab.
 */
public interface FileView {
	public void makeVisible();
}
