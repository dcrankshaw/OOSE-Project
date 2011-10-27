package edu.jhu.cs.oose.biblio.gui;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * Interface to display a list of the properties associated with a file
 * 
 */

public class PropertiesPanel extends JPanel {
	
	/**
	 * The list of tags the file is associated with
	 */
	private TagsListPane tagsListPane;

	/**
	 * The file whose properties are being displayed
	 */
	public FileMetadata file;
	
	/**
	 * A label displaying the date and time of the last time this file was opened in Biblio
	 */
	private JLabel lastOpenedLabel;
	
	/**
	 * A label displaying the number of times this file has been opened through Biblio
	 */
	private JLabel openedCountLabel;
	
}
