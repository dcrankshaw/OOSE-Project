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
		super();
		setContents(null);
	}
	
	/**
	 * Creates a new PDFPreviewPanel that displays a preview
	 * of the given file contents
	 * @param c the file contents to preview
	 */
	public PDFPreviewPanel(PDFFileContents c)
	{
		super();
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
			Image thumbnail = contents.getPage(1);
			// scale the image appropriately
			double widthRatio = this.getSize().getWidth() /  (double)thumbnail.getWidth(null);
			double heightRatio = this.getSize().getHeight() /  (double)thumbnail.getHeight(null);
			double ratio = 1;
			if( widthRatio < heightRatio ) {
				ratio = widthRatio;
			}
			else {
				ratio = heightRatio;
			}
			ratio = Math.min(ratio, 1.0);
			
			int width = (int)(thumbnail.getWidth(null)*ratio);
			int height = (int)(thumbnail.getHeight(null) * ratio);
			
			int leftEdge = (this.getSize().width - width) / 2;
			int bottomEdge = (this.getSize().height - height) / 2;
			
			g.drawImage(thumbnail, leftEdge, bottomEdge, leftEdge + width, bottomEdge + height,
					0, 0, thumbnail.getWidth(null), thumbnail.getHeight(null), null);
		}
		catch(PdfException e) {
			// TODO draw something that indicates what went wrong
			g.setColor(Color.RED);
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
		this.getBorder().paintBorder(this, g, 0, 0, this.getSize().width, this.getSize().height);
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
