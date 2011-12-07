package edu.jhu.cs.oose.biblio.gui;

import java.io.File;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The dialog the user sees when there are new files in
 * the folder that biblio is watching
 */
public class WatcherSelectorDialog extends JDialog {
	
	/** the owner of this dialog */
	JFrame owner;
	
	/**
	 * Creates a new dialog to import the given files.
	 * @param files the files presented to user for selecting and importing
	 * @param owner the parent view of this dialog box
	 */
	public WatcherSelectorDialog(List<File> files, JFrame owner) {
		super(owner, "Select Directories To Watch", true);
		this.owner = owner;
		JPanel watcherSelectorPanel = new WatcherSelectorPanel(files, this);
		this.setContentPane(watcherSelectorPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		setVisible(true);
	}
	
	/**
	 * return the JFrame that contain this Dialog
	 */
	public JFrame getParent() {
		return this.owner;
	}
}
