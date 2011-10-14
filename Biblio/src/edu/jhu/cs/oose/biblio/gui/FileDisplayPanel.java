package edu.jhu.cs.oose.biblio.gui;

import javax.swing.JMenu;
import javax.swing.JPanel;

/**
 * A GUI elements that provides a user interface to the underlying
 * file
 */
public abstract class FileDisplayPanel extends JPanel {

	/** The file that this display panel displays */
	public FileMetadata file;
	
	/** A right click menu displaying actions to perform with this
	 * file
	 */
	public JMenu rightClickMenu;
	
	//TODO: Do want to have this to have a FileContents or FileMetadata
	
}
