package edu.jhu.cs.oose.biblio.gui;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * This displays a list of the the filenames that match the search criteria.
 */
public class SearchResultsListPanel extends JPanel {
	
	/** All of the files that match the search criteria */
	public List<FileMetadata> matchingFiles;
	
	/**
	 * Table where the results are displayed
	 */
	private JTable table;
	
	
}
