package edu.jhu.cs.oose.biblio.gui;

import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * A GUI elements that provides a user interface for the underlying
 * file
 */
public abstract class FileDisplayPanel extends JPanel {

	/** The file that this display panel displays */
	private FileMetadata file;
	
	public FileDisplayPanel(FileMetadata f) {
		this.file = f;
	}
	
	/**
	 *  The file that this display panel displays
	 *	@return the file that this display panel displays  
	 */
	public FileMetadata getFile() {
		return this.file;
	}
}
