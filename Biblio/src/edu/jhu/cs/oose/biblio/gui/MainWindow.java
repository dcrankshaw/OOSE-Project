package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.FilterSearchStrategy;
import edu.jhu.cs.oose.biblio.model.SearchManager;
import edu.jhu.cs.oose.biblio.model.TextSearchStrategy;
import edu.jhu.cs.oose.biblio.model.Watcher;
import edu.jhu.cs.oose.biblio.model.WatcherEventListener;

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
	 * @param manager the object that should actually perform the searches
	 * @return the searching panel
	 */
	private JPanel makeSearchPanel(SearchManager manager) {
		JPanel largePanel = new JPanel();
		
		largePanel.setLayout(new BorderLayout());
		List<TextSearchStrategy> searchMethods = new ArrayList<TextSearchStrategy>();
		searchMethods.add(TextSearchStrategy.getStrategy(SearchMode.TAGS));
		searchMethods.add(TextSearchStrategy.getStrategy(SearchMode.FULLTEXT));
		SearchPanel searchPanel = new SearchPanel(searchMethods, FilterSearchStrategy.getStrategy(SearchMode.FILTER));
		searchPanel.setSearchController(manager);
		largePanel.add(searchPanel, BorderLayout.WEST);
		
		SearchResultsPreviewPanel previews = new SearchResultsPreviewPanel();
		previews.setSearchController(manager);
		previews.listenForFileResults();
		largePanel.add(previews, BorderLayout.CENTER);
		
		// connect the preview panel to the search panel
		// using the search controller classes
		
		return largePanel;
	}
	
	private JPanel makeBookmarkSearchPanel(SearchManager manager) {
		JPanel largePanel = new JPanel();
		
		largePanel.setLayout(new BorderLayout());
		List<TextSearchStrategy> searchMethods = new ArrayList<TextSearchStrategy>();
		searchMethods.add(TextSearchStrategy.getStrategy(SearchMode.TAGS));
		SearchPanel searchPanel = new SearchPanel(searchMethods, FilterSearchStrategy.getStrategy(SearchMode.BOOKMARKS));
		searchPanel.setSearchController(manager);
		largePanel.add(searchPanel, BorderLayout.WEST);
		
		SearchResultsPreviewPanel preview = new SearchResultsPreviewPanel();
		preview.setSearchController(manager);
		preview.listenForBookmarkResults();
		largePanel.add(preview, BorderLayout.CENTER);
		return largePanel;
	}
	
	/**
	 * Creates a new GUI.  This is the reading/searching window.
	 */
	public MainWindow() {
		super();
		this.factory = new FullFilePanelFactory();
		tabs = new JTabbedPane();
		SearchManager sManager = new SearchManager();
		JPanel searchPanel = makeSearchPanel(sManager);
		tabs.add("Search Files", searchPanel);
		
		sManager = new SearchManager();
		searchPanel = makeBookmarkSearchPanel(sManager);
		tabs.add("Search Bookmarks", searchPanel);
		this.getContentPane().add(tabs);
		
		FileViewManager.getViewManager().setFactory(new FileTabFactory());
		
		// This is kind of where the main loop is,
		// at least the one that I'm (Paul) writing,
		// so I'll set this up here...
		FileViewManager.getPropertiesManager().setFactory(new PropertiesWindowFactory());
		
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		JMenuItem item = new JMenuItem("Import files...");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ImportManager().startImportProcess(MainWindow.this);
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Manage Tags");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO there should only really be one of these...
				TagEditorPanel panel = new TagEditorPanel();
				JFrame tagFrame = new JFrame();
				tagFrame.add(panel);
				tagFrame.pack();
				tagFrame.setTitle("Manage Tags");
				tagFrame.setVisible(true);
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Watch Directories");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Watcher w = Watcher.getWatcher();
				List<File> fList = new ArrayList<File>(w.getWatchedDirectories());
				new WatcherSelectorDialog(fList, MainWindow.this);
			}
		});
		menu.add(item);
		
		item = new JMenuItem("Close File");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int openFile = getTabbedPane().getSelectedIndex();
				try {
					// This line will throw an exception when the search panels
					// are open
					FileView view = (FileView)getTabbedPane().getComponent(openFile);
					getTabbedPane().remove(openFile);
					FileViewManager.getViewManager().removeView(view);
				}
				catch(ClassCastException except) {
					// The correct action to take when trying to close a not-file
					// is to do nothing and proceed like nothing happened.
				}
			}
		});
		menu.add(item);
		this.setJMenuBar(menuBar);
		/*Watcher watcher = Watcher.getWatcher();
		
		
		/*----------------------------------------------
		 * 				Testing Watcher Dialog:
		 * 				TODO: delete
		 -----------------------------------------------*
		
		List<File> importedFiles = new ArrayList<File>();
		importedFiles.add(new File("testfiles/test1.pdf"));
		importedFiles.add(new File("testfiles/test2.pdf"));
		WatcherImportDialog watcherDialog = new WatcherDialog*/
		Watcher watcher = Watcher.getWatcher();
		watcher.addListener(new WatcherEventListener() {
			
			@Override
			public void directoryModified(Set<File> addedFiles, Set<File> deletedFiles) {
				//TODO for now ignore deleted files
				List<File> addedFileList = new ArrayList<File>(addedFiles);
				createWatcherImportDialog(addedFileList);
				
			}
		});
		
	}
	
	public void createWatcherImportDialog(List<File> addedFiles)
	{
		
		//TODO: verify that these files aren't already in the library
		WatcherImportDialog watchDialog = new WatcherImportDialog(addedFiles, this);
	}
	
	/**
	 * Creates a new window and displays it on screen.
	 * @param args  Command line arguments - they get ignored.
	 */
	public static void main(String[] args) {
		// Initialize the DB?
		MainWindow win = new MainWindow();
		win.pack();
		win.setVisible(true);
		Watcher watcher = Watcher.getWatcher();
		Thread watch =  new Thread (watcher);
		
		
		
		win.addWindowListener(new WindowListener() {

			@Override
			public void windowClosed(WindowEvent event) {
				Watcher watcher = Watcher.getWatcher();
				watcher.requestStop();
				
			}

			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowClosing(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		watch.start();
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
			pack();
			return result;
		}
	}
	
	/**
	 * A wrapper for the tabs, so that the FileViewManager can
	 * manage the full views of files.
	 */
	private class FileTab extends JPanel implements FileView {
		/** The file displayed by this tab */
		private FileMetadata file;
		@Override
		public void makeVisible() {
			getTabbedPane().setSelectedComponent(this);
		}
		
		/**
		 * Creates a new tab that displays the given file
		 * @param data the file to display
		 */
		public FileTab(FileMetadata data) {
			this.file = data;
			this.setLayout(new BorderLayout());
			ScrollFilePanel scrollPanel = new ScrollFilePanel();
			FullFilePanel panel = factory.newFullFilePanel(data);
			scrollPanel.setContents(panel);
			this.add(scrollPanel, BorderLayout.CENTER);
		}
		
		@Override
		public FileMetadata getFile() {
			return file;
		}
	}
	
}
