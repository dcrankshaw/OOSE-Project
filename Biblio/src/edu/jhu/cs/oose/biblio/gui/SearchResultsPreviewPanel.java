package edu.jhu.cs.oose.biblio.gui;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.SearchManager;
import edu.jhu.cs.oose.biblio.model.SearchResultsListener;
import edu.jhu.cs.oose.biblio.model.UnsupportedFiletypeException;

/**
 * This is the body of the full-window search interface. It shows the documents that match the current
 * search criteria. Full text searches are displayed in order of relevance. It displays a preview
 * of each file.
 */
public class SearchResultsPreviewPanel extends JPanel implements SearchResultsListener {
	
	/** All of the files that match the search criteria */
	private List<PreviewPanel> matchingFiles;
	
	/** The object that does all the searching. */
	private SearchManager controller;
	
	/** The number of columns in the grid of search results. */
	private int columnCount;
	
	/** The horizontal gap between rows of previews of search results */
	private static final int HGAP = 5;
	
	/** Creates a new Panel that displays previews of search results. */
	public SearchResultsPreviewPanel() {
		columnCount = 2;
		newLayout(1);
		matchingFiles = new ArrayList<PreviewPanel>();
	}
	
	/**
	 * Creates a new LayoutManager that displays the results
	 * in the given number of rows.
	 * @param rows the number of rows to display results in.
	 */
	private void newLayout(int rows) {
		GridLayout layout = new GridLayout(rows, this.columnCount);
		layout.setHgap(HGAP);
		this.setLayout(layout);
	}
	
	/** Sets the object to use for searching
	 * @param sc the object to use for searching
	 */
	void setSearchController(SearchManager sc) {
		if( null != controller ) {
			this.controller.removeResultsListener(this);
		}
		this.controller = sc;
		if( this.controller != null ) {
			this.controller.addResultsListener(this);
		}
	}
	
	/**
	 * A small panel that indicates an error occurred
	 * creating the preview for this file.
	 */
	private class ErrorPreviewPanel extends PreviewPanel {
		/**
		 * Creates a new small panel to display an error message
		 * @param e the error to display
		 */
		ErrorPreviewPanel(Throwable e) {
			this.add(new JLabel(e.getMessage()));
		}
	}
	
	@Override
	public void displayResults(List<FileMetadata> results) {
		for( int i = 0; i < matchingFiles.size(); i++ ) {
			this.remove(matchingFiles.get(i));
		}
		matchingFiles.clear();
		for( int i = matchingFiles.size(); i < results.size(); i++ ) {
			try {
				matchingFiles.add(FilePreviewFactory.createPreview(results.get(i)));
			}
			catch(UnsupportedFiletypeException e) {
				// though this really shouldn't happen, since we already imported the file...
				matchingFiles.set(i, new ErrorPreviewPanel(e));
			}
		}
		
		int rows;
		if( columnCount > 0 ) {
			rows = results.size() / columnCount;
			if (results.size() % columnCount > 0) {
				rows += 1;
			}
		}
		else {
			rows = 1;
		}

		this.newLayout(rows);
		
		// put all the cells back in, ensuring that they are in the correct order
		for( int i = 0; i < matchingFiles.size(); i++ ) {
			this.add(matchingFiles.get(i));
		}
		this.revalidate();
	}
}
