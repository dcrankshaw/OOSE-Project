package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.Watcher;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * GUI element that present newly created file in 
 * watching directory for user to select and import
 */
public class WatcherSelectorPanel extends JPanel {

	/** owner of this panel */
	private WatcherSelectorDialog owner;
	
	/** directories that is currently watched */
	private List<File> files;
		
	/** A button to open file browser to select a new directory */
	private JButton addButton;
	
	/** A button to remove the selected directory */
	private JButton removeButton;
	
	/** A button to close this dialog */
	private JButton closeButton;
	
	/** The object that holds item for JList to display */
	private DefaultListModel listModel;
	
	/** A list displaying the existing directories */
	private JList dirList;
	
	/** The panel that contain dirList */
	private JPanel listPanel;
	
	private List<File> directoriesToAdd;
	
	private List<File> directoriesToRemove;
	
	public WatcherSelectorPanel(List<File> files, WatcherSelectorDialog currentOwner) {
		directoriesToAdd = new ArrayList<File>();
		directoriesToRemove = new ArrayList<File>();
		this.owner = currentOwner;
		this.files = files;
		Iterator<File> iter = files.iterator();
		listModel = new DefaultListModel();
		while (iter.hasNext()) {
			listModel.addElement(iter.next().toString());
		}
		
		dirList = new JList(listModel);
		dirList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		JScrollPane listpane = new JScrollPane(dirList);
		//dirList.setVisibleRowCount(-1);
		addButton = new JButton("add");
		addButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				addDir();
			}
		});
		
		removeButton = new JButton("remove");
		removeButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				removeDir();
			}
		});
		
		closeButton = new JButton("close");
		closeButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				closeDialog();
			}
		});
		
		listPanel = new JPanel();
		JPanel globalOptionsPanel = new JPanel();
		
		listPanel.setLayout(new GridLayout(files.size(), 1));
		globalOptionsPanel.setLayout(new GridLayout());
		//this.add(listPanel, BorderLayout.CENTER);
		this.add(listpane, BorderLayout.CENTER);
		this.add(globalOptionsPanel, BorderLayout.SOUTH);
		globalOptionsPanel.add(addButton);
		globalOptionsPanel.add(removeButton);
		globalOptionsPanel.add(closeButton);
		globalOptionsPanel.setPreferredSize(new Dimension(300, 30));
		//listPanel.add(dirList);
		//listPanel.setPreferredSize(new Dimension(300, 100));
		listpane.setPreferredSize(new Dimension(400, 100));
		this.setPreferredSize(new Dimension(450, 170));
	}
	
	/**
	 * Close this dialog
	 */
	public void closeDialog() {
		//TODO: save changes to watched directories, then close
		Watcher w = Watcher.getWatcher();
		w.addWatchedDirectories(directoriesToAdd);
		w.removeWatchedDirectories(directoriesToRemove);
		owner.dispose();
	}
	
	/**
	 * Open file browser
	 */
	public void addDir() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setDialogTitle("Select directories to watch");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int choiceResult = fileChooser.showDialog(owner.getParent(), "Import");
		if( JFileChooser.CANCEL_OPTION == choiceResult ) {
			return;
		}
		else if( JFileChooser.ERROR_OPTION == choiceResult ) {
			// TODO handle the error
			return;
		}
		else if( JFileChooser.APPROVE_OPTION == choiceResult ) {
			// get selected files
			File[] fl = fileChooser.getSelectedFiles();
			// add these files into JList
			for (File f : fl) {
				listModel.addElement(f.toString());
			}
			// add these files to watcher
			directoriesToAdd.addAll(Arrays.asList(fl));
		}
	}
	
	/**
	 * Remove the selected directory, if nothing is selected, do nothing.
	 */
	public void removeDir() {
		// get the selected file list
		if (!dirList.isSelectionEmpty()) {
			// remove these files from the JList
			int index[] = (int[]) dirList.getSelectedIndices();
			for (int j=0; j<index.length; j++) {
				File fl = getFile(listModel.get(index[j]).toString());
				if (fl != null) {
					directoriesToRemove.add(fl);
				}
				listModel.remove(index[j]);
			}
		}
	}
	
	/**
	 * Return the file associating with the file name
	 * @param fname the file name
	 * 
	 * @return the file object with the file name
	 */
	private File getFile(String fname) {
		Iterator<File> iter = files.iterator();
		while (iter.hasNext()) {
			File fl = (File) iter.next();
			if (fname.compareTo(fl.toString()) == 0) {
				return fl;
			}
		}
		return null;
	}
}
