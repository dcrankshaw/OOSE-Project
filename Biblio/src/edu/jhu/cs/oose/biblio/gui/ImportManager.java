package edu.jhu.cs.oose.biblio.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import edu.jhu.cs.oose.biblio.model.Database;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * This class manages the import process.  That is,
 * it puts up the right gui elements at the right times,
 * and properly processes the results.
 */
public class ImportManager {
	
	/**
	 * Puts up the first stage of the import process -
	 * gets filenames to import.
	 * @param parent the window that the dialogs should be centered on
	 */
	public void startImportProcess(JFrame parent) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setDialogTitle("Select the files to import");
		int choiceResult = fileChooser.showDialog(parent, "Import");
		if( JFileChooser.CANCEL_OPTION == choiceResult ) {
			return;
		}
		else if( JFileChooser.ERROR_OPTION == choiceResult ) {
			// TODO handle the error
			return;
		}
		else if( JFileChooser.APPROVE_OPTION == choiceResult ) {
			List<FileMetadata> files = getMetadataForFiles(fileChooser.getSelectedFiles());
			new ImportDialog(files, parent);
		}
	}
	
	/**
	 * Creates FileMetadata objects for the given filenames
	 * @param paths the filenames to process
	 * @return FileMetadata for the given files
	 */
	private List<FileMetadata> getMetadataForFiles(File[] paths) {
		Database.getNewSession();
		List<FileMetadata> result = new ArrayList<FileMetadata>(paths.length);
		for( File f : paths ) {
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
		Database.commit();
		return result;
	}
}
