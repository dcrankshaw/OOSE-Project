package edu.jhu.cs.oose.biblio.gui;

import java.awt.BorderLayout;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;

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
	
	/**
	 * The parent dialog box containing this watcher import panel.
	 */
	JDialog owner;
	
	public WatcherImportPanel(List<File> files, WatcherImportDialog currentOwner) {
		this.owner = currentOwner;
		this.files = files;
		checkList = new ArrayList<JCheckBox>();
		importButton = new JButton("Import");
		importButton.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
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
		JPanel globalOptionsPanel = new JPanel();
		globalOptionsPanel.setLayout(new GridLayout());
		this.add(globalOptionsPanel, BorderLayout.SOUTH);
		globalOptionsPanel.add(cancelButton);
		globalOptionsPanel.add(importButton);
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
}
