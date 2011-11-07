package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class MainWindow extends JFrame {

	private JTabbedPane tabs;
	
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
		tabs = new JTabbedPane();
		JPanel searchPanel = makeSearchPanel();
		tabs.add("Search", searchPanel);
		this.getContentPane().add(tabs);
	}
	
	public static void main(String[] args) {
		// Initialize the DB?
		MainWindow win = new MainWindow();
		win.pack();
		win.setVisible(true);
	}
}
