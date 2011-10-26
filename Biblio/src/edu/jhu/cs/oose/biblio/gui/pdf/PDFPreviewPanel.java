package edu.jhu.cs.oose.biblio.gui.pdf;

import java.awt.Dimension;
import java.awt.Image;

import org.jpedal.exception.PdfException;

import edu.jhu.cs.oose.biblio.gui.PreviewPanel;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;


/**
 * Subclass of a PreviewPanel containing the functionality to view a preview of a PDF file
 */


public class PDFPreviewPanel extends PreviewPanel {
	
	/** The contents of the file we are showing a preview of */
	public PDFFileContents contents;

	/**
	 * Creates a new PDFPreviewPanel with no file to display.
	 */
	public PDFPreviewPanel()
	{
		contents = null;
	}
	
	/**
	 * Creates a new PDFPreviewPanel that displays a preview
	 * of the given file contents
	 * @param contents the file contents to preview
	 */
	public PDFPreviewPanel(PDFFileContents c) {
		contents = c;
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		try {
			if( contents != null ) {
				Image preview = contents.getThumbnail();
				return new Dimension(preview.getWidth(null), preview.getHeight(null));
			}
		}
		catch(PdfException e) {
			System.err.println("Could not generate the thumbnail needed for a preview: " + e.getLocalizedMessage());
		}
		// TODO this should probably be something sensible, and not magic numbers
		return new Dimension(10, 10);
	}
}
