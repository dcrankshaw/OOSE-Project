package edu.jhu.cs.oose.biblio.gui;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * The dialog the user sees after they have selected files
 * and before they are fully imported.  The dialog allows
 * the user to apply tags before they are imported and specify
 * how they should be copied to the repository.
 */
public class ImportDialog extends JDialog
{
	/**
	 * Creates a new dialog to import the given files.
	 * @param files the files to be imported
	 * @param owner the parent view of this dialog box
	 */
	//Will probably need some sort of reference to the model/database
	public ImportDialog(List<FileMetadata> files, JFrame owner)
	{
		super(owner, "Import", true); //creates a modal dialog
		JPanel importPanel = new ImportPanel(files, this);
		this.setContentPane(importPanel);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.pack();
		setVisible(true);
	}
}
