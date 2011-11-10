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
	
	/**
	 * Creates a new FileDisplayPanel that displays the given file.
	 * @param f the file to display in this panel
	 */
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
