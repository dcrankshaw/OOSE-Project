package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.hibernate.Session;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * GUI element that present newly created file in 
 * watching directory for user to select and import
 */
public class WatcherImportPanel extends JPanel {
	
	/** files for selecting */
	List<File> files;
	
	/** files that are selected */
	List<File> selectedFiles;
	
	/** A list of checkbox that present the file for selecting */
	private List<JCheckBox> checkList;
	
	/** A button to carry on to the importDialog */
	private JButton importButton;
	
	/** A button to close the current dialog */
	private JButton cancelButton;
	
	/** A panel that contains all the check box */
	JPanel checkBoxPanel;
	
	/**
	 * The parent dialog box containing this watcher import panel.
	 */
	JDialog owner;
	
	/**
	 * WatcherImportPanel constructor, it present a list of new files in the watching directories
	 * and user can choose which one they would like to import into Biblio
	 * 
	 * @param files the additional files added or created in the watching directories
	 * @param currentOwner the dialog that contain this panel
	 */
	public WatcherImportPanel(List<File> files, WatcherImportDialog currentOwner) {
		this.owner = currentOwner;
		this.files = files;
		selectedFiles = new ArrayList<File>();
		checkList = new ArrayList<JCheckBox>();
		importButton = new JButton("Import");
		importButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				getSelectedFiles();
				startImport();
			}
		});
		
		cancelButton = new JButton("Cancel");
		cancelButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				cancelImport();
			}
		});
		
		
		// TODO add checkbox array and listener - add file into selectedFile
		checkBoxPanel = new JPanel();
		JPanel globalOptionsPanel = new JPanel();
		
		checkBoxPanel.setLayout(new GridLayout(files.size(), 1));
		globalOptionsPanel.setLayout(new GridLayout());
		this.add(checkBoxPanel, BorderLayout.CENTER);
		this.add(globalOptionsPanel, BorderLayout.SOUTH);
		globalOptionsPanel.add(cancelButton);
		globalOptionsPanel.add(importButton);
		globalOptionsPanel.setPreferredSize(new Dimension(200, 50));
		for (File f : files) {
			checkBoxPanel.add(new JCheckBox(f.toString()));
		}
	}
	
	private void cancelImport()
	{
		owner.dispose();
	}
	
	public void startImport()
	{
		getSelectedFiles();
		List<File> myfiles = new ArrayList<File>();
		for(File f: selectedFiles)
		{
			myfiles.add(f.getAbsoluteFile());
		}
		new ImportManager().startImportProcess((JFrame) owner.getParent(), myfiles);
		owner.dispose();
	}
	
	
	/**
	 * Go through all the selected JCheckBox and add the associated file into selectedFiles
	 */
	private void getSelectedFiles() {
		Component[] components = checkBoxPanel.getComponents();
		for (Component c : components) {
			if (c instanceof JCheckBox) {
				JCheckBox cb = (JCheckBox) c;
				if (cb.isSelected()) {
					for (File f : files) {
						if (f.toString().compareTo(cb.getText()) == 0) {
							selectedFiles.add(f);
						}
					}
				}
			}
		}
	}
}
