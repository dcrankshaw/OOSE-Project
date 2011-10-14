package edu.jhu.cs.oose.biblio.gui.pdf;

import edu.jhu.cs.oose.biblio.gui.PreviewPanel;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;


/**
 * Subclass of a PreviewPanel containing the functionality to view a preview of a PDF file
 */


public class PDFPreviewPanel extends PreviewPanel {
	
	/** The contents of the file we are showing a preview of */
	public PDFFileContents PDFDocument;

	
	/** The method to actually draw the file Preview */
	@Override
	public void displayPreview() {}
	

}
