package edu.jhu.cs.oose.biblio.gui.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import org.jpedal.exception.PdfException;

import edu.jhu.cs.oose.biblio.gui.PreviewPanel;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;


/**
 * Subclass of a PreviewPanel containing the functionality to view a preview of a PDF file
 */


public class PDFPreviewPanel extends PreviewPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** The contents of the file we are showing a preview of */
	public PDFFileContents contents;

	/**
	 * Creates a new PDFPreviewPanel with no file to display.
	 */
	public PDFPreviewPanel()
	{
		setContents(null);
	}
	
	/**
	 * Creates a new PDFPreviewPanel that displays a preview
	 * of the given file contents
	 * @param c the file contents to preview
	 */
	public PDFPreviewPanel(PDFFileContents c)
	{
		setContents(c);
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
	
	@Override
	public void paint(Graphics g) {
		try {
			Image thumbnail = contents.getThumbnail();
			if( this.getSize().equals(this.getPreferredSize())) {
				g.drawImage(thumbnail, 0, 0, null);
			}
			else {
				Image preview = thumbnail.getScaledInstance(getSize().width, getSize().height, Image.SCALE_DEFAULT);
				g.drawImage(preview, 0, 0, null);
			}
		}
		catch(PdfException e) {
			// TODO draw something that indicates what went wrong
			g.setColor(Color.RED);
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
	}
	
	/**
	 * Sets the file contents that will be displayed
	 * in this preview.
	 * @param c the file contents to display
	 */
	public void setContents(PDFFileContents c) {
		contents = c;
	}
}
