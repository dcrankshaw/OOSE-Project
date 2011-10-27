package edu.jhu.cs.oose.biblio.gui;

import java.util.List;

import javax.swing.JPanel;

/**
 * This is the body of the full-window search interface. It shows the documents that match the current
 * search criteria. Full text searches are displayed in order of relevance. It displays a preview
 * of each file.
 */
public class SearchResultsPreviewPanel extends JPanel {
	
	/** All of the files that match the search criteria */
	public List<PreviewPanel> matchingFiles;
	
}
