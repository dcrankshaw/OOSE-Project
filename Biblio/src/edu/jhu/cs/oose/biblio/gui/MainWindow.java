package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhu.cs.oose.biblio.model.FileMetadata;

public class MainWindow extends JFrame {

	private JTabbedPane tabs;
	private FullFilePanelFactory factory;
	
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
	
	public static void main(String[] args) {
		// Initialize the DB?
		MainWindow win = new MainWindow();
		win.pack();
		win.setVisible(true);
	}
	
	private JTabbedPane getTabbedPane() {
		return this.tabs;
	}
	
	private class FileTabFactory implements FileViewFactory {
		@Override
		public FileView newView(FileMetadata file) {
			FileTab result = new FileTab(file);
			getTabbedPane().add(file.getName(), result);
			return result;
		}
	}
	
	private class FileTab extends JPanel implements FileView {
		@Override
		public void makeVisible() {
			getTabbedPane().setSelectedComponent(this);
		}
		
		public FileTab(FileMetadata data) {
			this.add(factory.newFullFilePanel(data));
		}
	}
}
