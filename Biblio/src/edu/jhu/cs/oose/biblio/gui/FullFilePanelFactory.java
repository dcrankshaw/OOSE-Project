package edu.jhu.cs.oose.biblio.gui;

import edu.jhu.cs.oose.biblio.gui.epub.EpubFullFilePanel;
import edu.jhu.cs.oose.biblio.gui.pdf.PDFFullFilePanel;
import edu.jhu.cs.oose.biblio.model.FileMetadata;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileContents;
import edu.jhu.cs.oose.biblio.model.epub.EpubFileMetadata;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;

/**
 * Factory class for producing panels that display
 * an entire file.
 */
public class FullFilePanelFactory {
	/**
	 * Creates a Panel that displays the full version of the given file.
	 * @param file the file to display
	 * @return a Panel that displays the file
	 */
	public FullFilePanel newFullFilePanel(FileMetadata file) {
		// TODO Zach says to use the visitor pattern here
		if( file instanceof PDFFileMetadata ) {
			return new PDFFullFilePanel((PDFFileContents)file.getContents());
		}
		else if( file instanceof EpubFileMetadata ) {
			return new EpubFullFilePanel((EpubFileContents)file.getContents());
		}
		return null;
	}
}
