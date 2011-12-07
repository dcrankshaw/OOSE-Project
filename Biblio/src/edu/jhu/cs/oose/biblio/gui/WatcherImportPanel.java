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
	
	public WatcherImportPanel(List<File> files, WatcherImportDialog currentOwner) {
		this.owner = currentOwner;
		this.files = files;
		selectedFiles = new ArrayList<File>();
		checkList = new ArrayList<JCheckBox>();
		importButton = new JButton("Import");
		importButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Session session = Database.getSessionFactory().getCurrentSession();
				session.beginTransaction();
				getSelectedFiles();
				ImportDialog importer = new ImportDialog(getMetadataForFiles(), (JFrame) owner.getParent());
				cancelImport();
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
	
	/**
	 * Close this dialog
	 */
	public void cancelImport() {
		owner.setVisible(false);
	}
	
	private List<FileMetadata> getMetadataForFiles() {
		List<FileMetadata> result = new ArrayList<FileMetadata>(selectedFiles.size());
		for( File f : selectedFiles ) {
			// TODO use the MIME type library...
			if( f.getName().endsWith(".pdf")) {
				result.add(new PDFFileMetadata(f.getAbsolutePath()));
			}
			else if(f.getName().endsWith(".epub")) {
				result.add(new EpubFileMetadata(f.getAbsolutePath()));
			}
			else {
				// TODO read other kinds of files or give errors
			}
		}
		return result;
	}
	
	private void getSelectedFiles() {
		Component[] components = checkBoxPanel.getComponents();
		for (Component c : components) {
			if (c instanceof JCheckBox) {
				JCheckBox cb = (JCheckBox) c;
				for (File f : files) {
					if (f.toString().compareTo(cb.getText()) == 0) {
						selectedFiles.add(f);
					}
				}
			}
		}
	}
}
