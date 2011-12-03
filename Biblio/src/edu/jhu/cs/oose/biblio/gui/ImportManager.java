package edu.jhu.cs.oose.biblio.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

public class ImportManager {
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
			ImportDialog importer = new ImportDialog(files, parent);
			//importer.
		}
	}
	
	private List<FileMetadata> getMetadataForFiles(File[] paths) {
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
		return result;
	}
}
