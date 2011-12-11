package edu.jhu.cs.oose.biblio.gui;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import edu.jhu.cs.oose.biblio.model.Bookmark;
import edu.jhu.cs.oose.biblio.model.BookmarkSearchResultsListener;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.SearchManager;
import edu.jhu.cs.oose.biblio.model.SearchResultsListener;

/**
 * This is the body of the full-window search interface. It shows the documents that match the current
 * search criteria. Full text searches are displayed in order of relevance. It displays a preview
 * of each file.
 */
public class SearchResultsPreviewPanel extends JPanel implements SearchResultsListener, BookmarkSearchResultsListener {
	
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
		this.setOpaque(true);
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
	public void setSearchController(SearchManager sc) {
		if( null != controller ) {
			this.controller.removeResultsListener(this);
			this.controller.removeBookmarkResultsListener(this);
		}
		this.controller = sc;
	}
	
	public void listenForFileResults() {
		if( null != controller ) {
			this.controller.addResultsListener(this);
		}
	}
	
	public void listenForBookmarkResults() {
		if( null != controller ) {
			this.controller.addBookmarkResultsListener(this);
		}
	}
	
	@Override
	public void displayFileResults(List<FileMetadata> results) {
		for( int i = 0; i < matchingFiles.size(); i++ ) {
			this.remove(matchingFiles.get(i));
		}
		matchingFiles.clear();
		for( int i = 0; i < results.size(); i++ ) {
			matchingFiles.add(FilePreviewFactory.getFactory().createPreview(results.get(i)));
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
		this.repaint();
	}

	@Override
	public void displayBookmarkResults(List<Bookmark> results) {
		matchingFiles.clear();
		for( int i = matchingFiles.size(); i < results.size(); i++ ) {
			Bookmark bkmk = results.get(i);
			matchingFiles.add(FilePreviewFactory.getFactory().createPreview(bkmk.getFile(), bkmk));
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
		this.repaint();
	}
	
	@Override
	public void paint(Graphics g) {
		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		super.paint(g);
	}
}
