package edu.jhu.cs.oose.biblio.gui.pdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

import org.jpedal.exception.PdfException;

import edu.jhu.cs.oose.biblio.gui.FullFilePanel;
import edu.jhu.cs.oose.biblio.model.pdf.PDFFileContents;

/**
 * Displays an entire PDF document for the user to view and interact with
 */

public class PDFFullFilePanel extends FullFilePanel {
	
	/**
	 * The contents of the PDF file displayed in this panel
	 */
	private PDFFileContents contents;
	
	/** The height, in pixels, of one page of the PDF */
	private int pageHeight;
	
	/**
	 * Creates a new Panel without associating it with a file
	 */
	public PDFFullFilePanel()
	{
		setContents(null);
	}
	
	/**
	 * Creates a new PDFFullFilePanel containing the given file contents
	 * @param c the file contents to display
	 */
	public PDFFullFilePanel(PDFFileContents c)
	{
		setContents(c);
	}
	
	/**
	 * Sets the file contents this panel displays
	 * @param c the file contents to display
	 */
	public void setContents(PDFFileContents c)
	{
		contents = c;
		try {
			if( contents != null ) {
				pageHeight = contents.getPage(1).getHeight(null);
			}
			else {
				pageHeight = 0;
			}
		}
		catch (PdfException e) {
			// this means that there was an error reading / parsing
			// the PDF, so we don't have anything to display
			// TODO we should probably tell the user that something happened
			pageHeight = 0;
		}
	}
	
	/**
	 * The amount of spacing, in pixels, to put between pages
	 * in the pdf document.
	 */
	private static final int PAGE_SPACING = 10;
	
	@Override
	public Dimension getPreferredSize() {
		// for now, assume that all the pdf pages are the same size
		if( null == contents ) {
			return new Dimension(0, 0);
		}
		try {
			Dimension size = new Dimension(0, 0);
			Image firstPage = contents.getPage(1);
			size.width = firstPage.getWidth(null);
			size.height = (pageHeight + PAGE_SPACING) * contents.getPageCount() - PAGE_SPACING;
			return size;
		}
		catch (PdfException e) {
			// this means that there was an error reading / parsing
			// the PDF, so we don't have anything to display
			// TODO we should probably tell the user that something happened
			return new Dimension(0, 0);
		}
	}
	
	/** Returns the first page that should be drawn
	 * after the given vertical pixel
	 * @param pixel the height of the pixel to convert
	 * @return the page number of the next page to draw.
	 * This returns the number of pages + 1 if the pixel is past the end
	 */
	private int pixelsToPageNumber(int pixel) {
		// find the number of pages up to this pixel
		// each of these "pages" includes the image of the page itself
		// and the spacing underneath it
		int pageNum = pixel / (pageHeight + PAGE_SPACING);
		
		// if the pixel is in the space after a page,
		// then we should really return the next page
		if( pixel % (pageHeight + PAGE_SPACING) > pageHeight ) {
			pageNum += 1;
		}
		
		// we just computed a 0-based page number, but we need to start at 1
		pageNum += 1;
		
		return pageNum;
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 1;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return 1;
	}

	/**
	 * Draws one page of a PDF file into the given context.
	 * It is drawn at the vertical location.
	 * @param g the context in which to draw
	 * @param page the page to draw
	 */
	private void drawPage(Graphics g, int page) {
		try {
			Image pic = contents.getPage(page);
			g.drawImage(pic, 0, (pageHeight + PAGE_SPACING) * (page - 1) - PAGE_SPACING, null);
		}
		catch (PdfException e) {
			// TODO handle this exception
		}
	}
	
	@Override
	public void paint(Graphics g, Rectangle region) {
		g.setColor(Color.GRAY);
		g.fillRect(region.x, region.y, region.width, region.height);
		
		int firstPage = pixelsToPageNumber(region.y);
		int lastPage;
		try {
			lastPage = Math.min(contents.getPageCount(), pixelsToPageNumber(region.y + region.height));
		}
		catch( PdfException e) {
			lastPage = 0;
		}
		
		for( int page = firstPage; page <= lastPage; page++ ) {
			drawPage(g, page);
		}
	}
}
