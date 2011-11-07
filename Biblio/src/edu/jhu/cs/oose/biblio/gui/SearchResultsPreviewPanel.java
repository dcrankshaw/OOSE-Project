package edu.jhu.cs.oose.biblio.gui;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.SearchController;
import edu.jhu.cs.oose.biblio.SearchResultsListener;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.UnsupportedFiletypeException;

/**
 * This is the body of the full-window search interface. It shows the documents that match the current
 * search criteria. Full text searches are displayed in order of relevance. It displays a preview
 * of each file.
 */
public class SearchResultsPreviewPanel extends JPanel implements SearchResultsListener {
	
	/** All of the files that match the search criteria */
	private List<PreviewPanel> matchingFiles;
	
	private SearchController controller;
	
	private GridLayout layout;
	private int columnCount;
	
	private static final int HGAP = 5;
	
	public SearchResultsPreviewPanel() {
		newLayout(1);
	}
	
	private void newLayout(int rows) {
		this.layout = new GridLayout(rows, this.columnCount);
		this.layout.setHgap(HGAP);
		this.setLayout(layout);
	}
	
	void setSearchController(SearchController sc) {
		if( null != controller ) {
			this.controller.removeResultsListener(this);
		}
		this.controller = sc;
		if( this.controller != null ) {
			this.controller.addResultsListener(this);
		}
	}
	
	private class ErrorPreviewPanel extends PreviewPanel {
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
		
		int rows = results.size() / columnCount;
		if (results.size() % columnCount > 0) {
			rows += 1;
		}
		this.newLayout(rows);
		
		// put all the cells back in, ensuring that they are in the correct order
		for( int i = 0; i < matchingFiles.size(); i++ ) {
			this.add(matchingFiles.get(i));
		}
		this.revalidate();
	}
}
