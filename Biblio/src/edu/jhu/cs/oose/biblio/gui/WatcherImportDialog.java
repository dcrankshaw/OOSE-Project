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
public class WatcherImportDialog extends JDialog {
	
	/** the owner of this dialog */
	JFrame owner;
	
	/**
	 * Creates a new dialog to import the given files.
	 * @param files the files presented to user for selecting and importing
	 * @param owner the parent view of this dialog box
	 */
	public WatcherImportDialog(List<File> files, JFrame owner) {
		super(owner, "Watcher_Import", true);
		this.owner = owner;
		JPanel watcherImportPanel = new WatcherImportPanel(files, this);
		this.setContentPane(watcherImportPanel);
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
