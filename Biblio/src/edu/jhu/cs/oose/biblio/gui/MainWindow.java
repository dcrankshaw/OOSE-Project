package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

/**
 * The main window that contains the interface to Biblio 
 */
public class MainWindow extends JFrame {

	/**
	 * The tabs that select between searching and reading documents.
	 */
	private JTabbedPane tabs;
	/** An object that can produce full views of files. */
	private FullFilePanelFactory factory;
	
	/**
	 * Creates the Searching tab of the interface.
	 * @return the searching panel
	 */
	private JPanel makeSearchPanel() {
		JPanel largePanel = new JPanel();
		
		largePanel.setLayout(new BorderLayout());
		SearchPanel searchPanel = new SearchPanel();
		largePanel.add(searchPanel, BorderLayout.WEST);
		
		SearchResultsPreviewPanel previews = new SearchResultsPreviewPanel();
		largePanel.add(previews, BorderLayout.CENTER);
		
		// connect the preview panel to the search panel
		// using the search controller classes
		
		return largePanel;
	}
	
	/**
	 * Creates a new GUI.  This is the reading/searching window.
	 */
	public MainWindow() {
		super();
		this.factory = new FullFilePanelFactory();
		tabs = new JTabbedPane();
		JPanel searchPanel = makeSearchPanel();
		tabs.add("Search", searchPanel);
		this.getContentPane().add(tabs);
		
		FileViewManager.getViewManager().setFactory(new FileTabFactory());
		
		// This is kind of where the main loop is,
		// at least the one that I'm (Paul) writing,
		// so I'll set this up here...
		FileViewManager.getPropertiesManager().setFactory(new PropertiesWindowFactory());
	}
	
	/**
	 * Creates a new window and displays it onscreen.
	 * @param args  Command line arguments - they get ignored.
	 */
	public static void main(String[] args) {
		// Initialize the DB?
		MainWindow win = new MainWindow();
		win.pack();
		win.setVisible(true);
	}
	
	/** The tabs displayed in the interface
	 * @return the tabs displayed in the interface
	 */
	private JTabbedPane getTabbedPane() {
		return this.tabs;
	}
	
	/**
	 * Creates new tabs that display full files.
	 */
	private class FileTabFactory implements FileViewFactory {
		@Override
		public FileView newView(FileMetadata file) {
			FileTab result = new FileTab(file);
			getTabbedPane().add(file.getName(), result);
			return result;
		}
	}
	
	/**
	 * A wrapper for the tabs, so that the FileViewManager can
	 * manage the full views of files.
	 */
	private class FileTab extends JPanel implements FileView {
		@Override
		public void makeVisible() {
			getTabbedPane().setSelectedComponent(this);
		}
		
		/**
		 * Creates a new tab that displays the given file
		 * @param data the file to display
		 */
		public FileTab(FileMetadata data) {
			this.add(factory.newFullFilePanel(data));
		}
	}
}
