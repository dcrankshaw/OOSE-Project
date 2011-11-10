package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.pdf.PDFFullFilePanel;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

public class FullFilePanelFactory {
	public FullFilePanel newFullFilePanel(FileMetadata file) {
		// TODO Zach says to use the visitor pattern here
		if( file instanceof PDFFileMetadata ) {
			return new PDFFullFilePanel((PDFFileContents)file.getContents());
		}
		return null;
	}
}
