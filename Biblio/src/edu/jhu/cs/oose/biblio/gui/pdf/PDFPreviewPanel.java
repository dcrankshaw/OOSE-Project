package edu.jhu.cs.oose.biblio.gui.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import org.jpedal.exception.PdfException;

import edu.jhu.cs.oose.biblio.gui.PreviewPanel;
import edu.jhu.cs.oose.biblio.model.Location;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileMetadata;


/**
 * Subclass of a PreviewPanel containing the functionality to view a preview of a PDF file
 */
public class PDFPreviewPanel extends PreviewPanel {
	
	/** The contents of the file we are showing a preview of */
	private PDFFileContents contents;

	private int page;
	
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
	public PDFPreviewPanel(PDFFileMetadata c)
	{
		super(c);
		setContents((PDFFileContents)c.getContents());
	}
	
	public PDFPreviewPanel(PDFFileMetadata c, Location loc) {
		super(c);
		setContents((PDFFileContents)c.getContents());
		setLocation(loc);
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
		// but it only happens when an error occurs, and we were good and logged the error
		return new Dimension(10, 10);
	}
	
	@Override
	public void paint(Graphics g) {
		// Make sure that we get a clean background behind us
		g.setColor(this.getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());
		
		try {
			// Get the picture from the file
			Image thumbnail = contents.getPage(this.page);
			// find the limiting dimension and the best aspect ratio
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
			
			// Find the actual size of the image based on that
			int width = (int)(thumbnail.getWidth(null)*ratio);
			int height = (int)(thumbnail.getHeight(null) * ratio);
			
			// center the image
			int leftEdge = (this.getSize().width - width) / 2;
			int bottomEdge = (this.getSize().height - height) / 2;
			
			// draw it
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
		this.contents = c;
		this.page = 1;
	}
	
	@Override
	public void setLocation(Location loc) {
		if( null == this.contents ) {
			// location doesn't mean anything right now, so don't do anything
			return;
		}
		try {
			page = (int)Math.ceil(loc.getPercentageOfFile() * this.contents.getPageCount());
			if( 0 == page ) {
				page = 1;
			}
			this.revalidate();
		} catch (PdfException e) {
			// TODO There was an error reading the PDF file, which is weird,
			// since it was there before...
		}
	}
}
