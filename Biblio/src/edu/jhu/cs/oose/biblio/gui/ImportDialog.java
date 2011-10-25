package edu.jhu.cs.oose.biblio.gui;

import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

public class ImportDialog extends JDialog
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//Will probably need some sort of refernce to the model/database
	public ImportDialog(List<FileMetadata> files, JFrame owner)
	{
		
		super(owner, "Import", true); //creates a modal dialog
		JPanel importPanel = new ImportPanel(files);
		this.setContentPane(importPanel);
		
		
		
	}
	
	
	
}
